package org.apache.hadoop.ozone.container.ec.reconstruction;

import org.apache.hadoop.hdds.client.ECReplicationConfig;
import org.apache.hadoop.hdds.protocol.datanode.proto.ContainerProtos;
import org.apache.hadoop.hdds.scm.OzoneClientConfig;
import org.apache.hadoop.hdds.scm.storage.ECBlockOutputStream;
import org.apache.hadoop.ozone.common.OzoneChecksumException;
import org.apache.hadoop.ozone.container.common.helpers.BlockData;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.List;

public class ECValidator {

  private static final Logger LOG =
      LoggerFactory.getLogger(ECValidator.class);
  private final boolean isValidationEnabled;
  private int ecChunkSize;

  ECValidator(OzoneClientConfig config) {
    // We fetch the configuration value beforehand to avoid re-fetching on every validation call
    isValidationEnabled = config.getEcReconstructionValidationEnabled();
  }

  public void setEcConfigData(ECReplicationConfig ecReplicationConfig) {
    this.ecChunkSize = ecReplicationConfig.getEcChunkSize();
  }

  /**
   * Validate the expected checksum data for a chunk with the corresponding checksum in original stripe checksum
   * Note: The stripe checksum is a combination of all the checksums of all the chunks in the stripe
   * @param recreatedChunkChecksum Stores the {@link ContainerProtos.ChecksumData} of the recreated chunk to verify
   * @param stripeChecksum         Stores the {@link ByteBuffer} of stripe checksum
   * @param chunkIndex             Stores the index of the recreated chunk we are comparing
   * @param bytesPerCrc            Bytes per checksum for the recreated chunk
   * @param chunkSize              Stores the size of the recreated chunk
   * @param stripeChecksumSize     Stores the size of the stripe checksum
   * @throws OzoneChecksumException If there is a mismatch in the recreated chunk vs stripe checksum, or if there is any
   *                                internal error while performing {@link ByteBuffer} operations
   */
  private void validateChecksumInStripe(ContainerProtos.ChecksumData recreatedChunkChecksum,
      ByteBuffer stripeChecksum, int chunkIndex, int bytesPerCrc, int chunkSize, int stripeChecksumSize)
      throws OzoneChecksumException {

    // Calculate the number of checksums per chunk
    int numChecksumsPerChunk = (int) Math.ceil((double) chunkSize / bytesPerCrc);

    // Calculate the starting position of the checksum for the given chunk index
    int checksumOffset = chunkIndex * numChecksumsPerChunk * bytesPerCrc;

    // Ensure the stripe checksum has enough data for validation
    if (stripeChecksumSize < checksumOffset + (numChecksumsPerChunk * bytesPerCrc)) {
      throw new OzoneChecksumException(String.format(
        "Stripe checksum is too short to validate chunk checksum at index %d. Expected at least %d bytes, but got %d.",
        chunkIndex, checksumOffset + (numChecksumsPerChunk * bytesPerCrc), stripeChecksumSize));
    }

    // Extract the expected checksum for the chunk from the stripe checksum
    ByteBuffer expectedChecksum = stripeChecksum.duplicate();
    expectedChecksum.position(checksumOffset);
    expectedChecksum.limit(checksumOffset + (numChecksumsPerChunk * bytesPerCrc));

    // Compare the recreated chunk checksum with the expected checksum
    ByteBuffer recreatedChecksum = recreatedChunkChecksum.getChecksums(0).asReadOnlyByteBuffer();
    while (recreatedChecksum.hasRemaining()) {
      try {
        int recreatedChecksumByte = recreatedChecksum.getInt();
        int expectedChecksumByte = expectedChecksum.getInt();
        if (recreatedChecksumByte != expectedChecksumByte) {
          throw new OzoneChecksumException(String.format(
            "Checksum mismatch for chunk at index %d. Recreated checksum: %d, Expected checksum: %d.",
            chunkIndex, recreatedChecksumByte, expectedChecksumByte));
        }
      } catch (BufferUnderflowException e) {
        throw new OzoneChecksumException(String.format(
          "Buffer underflow while validating checksum for chunk at index %d. Position: %d.",
          chunkIndex, expectedChecksum.position()));
      }
    }
  }

  /**
   * Get the block from the BlockData which contains the checksum information
   * @param blockDataGroup An array of {@link BlockData} which contains all the blocks in a Datanode
   * @return The block which contains the checksum information
   */
  private BlockData getChecksumBlockData(BlockData[] blockDataGroup) {
    BlockData checksumBlockData = null;
    // Reverse traversal as all parity bits will have checksumBytes
    for (int i = blockDataGroup.length - 1; i >= 0; i--) {
      BlockData blockData = blockDataGroup[i];
      if (null == blockData) {
        continue;
      }

      List<ContainerProtos.ChunkInfo> chunks = blockData.getChunks();
      if (null != chunks && !(chunks.isEmpty())) {
        if (chunks.get(0).hasStripeChecksum()) {
          checksumBlockData = blockData;
          break;
        }
      }
    }

    return checksumBlockData;
  }

  /**
   * Helper function to validate the checksum between recreated data and
   * @param ecBlockOutputStream A {@link ECBlockOutputStream} instance that stores
   *                            the reconstructed index ECBlockOutputStream
   * @throws OzoneChecksumException if the recreated checksum and the block checksum doesn't match
   */
  public void validateChecksum(ECBlockOutputStream ecBlockOutputStream, BlockData[] blockDataGroup)
      throws OzoneChecksumException {
    if (isValidationEnabled) {
      // Get the recreated chunks and the block containing the stripe checksum
      List<ContainerProtos.ChunkInfo> recreatedChunks = ecBlockOutputStream.getContainerBlockData().getChunksList();
      BlockData checksumBlockData = getChecksumBlockData(blockDataGroup);
      if (checksumBlockData == null) {
        throw new OzoneChecksumException("Could not find checksum data in any index for blockDataGroup while validating.");
      }
      List<ContainerProtos.ChunkInfo> checksumBlockChunks = checksumBlockData.getChunks();

      // Validate each recreated chunk against the stripe checksum
      for (int chunkIdx = 0; chunkIdx < recreatedChunks.size(); chunkIdx++) {
        ContainerProtos.ChunkInfo recreatedChunk = recreatedChunks.get(chunkIdx);
        ContainerProtos.ChunkInfo checksumChunk = checksumBlockChunks.get(chunkIdx);

        ByteString stripeChecksum = checksumChunk.getStripeChecksum();
        validateChecksumInStripe(
          recreatedChunk.getChecksumData(),
          stripeChecksum.asReadOnlyByteBuffer(),
          chunkIdx,
          recreatedChunk.getChecksumData().getBytesPerChecksum(),
          ecChunkSize,
          stripeChecksum.size());
      }
    } else {
      LOG.debug("Checksum validation is disabled. Skipping validation.");
    }
  }
}
