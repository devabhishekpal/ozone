package org.apache.hadoop.ozone.om.helpers;

import org.apache.hadoop.fs.FileChecksum;
import org.apache.hadoop.fs.FileEncryptionInfo;
import org.apache.hadoop.hdds.utils.db.Codec;
import org.apache.hadoop.hdds.utils.db.DelegatedCodec;
import org.apache.hadoop.hdds.utils.db.Proto2Codec;
import org.apache.hadoop.ozone.OzoneAcl;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.MultipartPartInfo;
import org.apache.hadoop.ozone.protocolPB.OMPBHelper;

import java.util.List;
import java.util.Map;

/**
 * This class represents a part of a multipart upload key.
 */
public final class OmMultipartPartInfo extends WithMetadata{
  private static final Codec<OmMultipartPartInfo> CODEC = new DelegatedCodec<>(
      Proto2Codec.get(MultipartPartInfo.getDefaultInstance()),
      OmMultipartPartInfo::getFromProto,
      OmMultipartPartInfo::getProto,
      OmMultipartPartInfo.class);

  private final String partName;
  private final int partNumber;
  private final String volumeName;
  private final String bucketName;
  private final String keyName;
  private final long dataSize;
  private final long modificationTime;
  private final FileEncryptionInfo encInfo;
  private final FileChecksum fileChecksum;


  public static Codec<OmMultipartPartInfo> getCodec() {
    return CODEC;
  }

  private OmMultipartPartInfo(Builder b) {
    super(b);
    this.partName = b.partName;
    this.partNumber = b.partNumber;
    this.volumeName = b.volumeName;
    this.bucketName = b.bucketName;
    this.keyName = b.keyName;
    this.dataSize = b.dataSize;
    this.modificationTime = b.modificationTime;
    this.encInfo = b.encInfo;
    this.fileChecksum = b.fileChecksum;
  }

  public static class Builder extends WithMetadata.Builder {
    private String partName;
    private int partNumber;
    private String volumeName;
    private String bucketName;
    private String keyName;
    private long dataSize;
    private long modificationTime;
    private FileEncryptionInfo encInfo;
    private FileChecksum fileChecksum;

    protected Builder() {
    }

    public Builder(OmMultipartPartInfo obj) {
      super(obj);
      this.partName = obj.partName;
      this.partNumber = obj.partNumber;
      this.volumeName = obj.volumeName;
      this.bucketName = obj.bucketName;
      this.keyName = obj.keyName;
      this.dataSize = obj.dataSize;
      this.modificationTime = obj.modificationTime;
      this.encInfo = obj.encInfo;
      this.fileChecksum = obj.fileChecksum;
    }

    public Builder setPartName(String partName) {
      this.partName = partName;
      return this;
    }

    public Builder setPartNumber(int partNumber) {
      this.partNumber = partNumber;
      return this;
    }

    public Builder setVolumeName(String volumeName) {
      this.volumeName = volumeName;
      return this;
    }

    public Builder setBucketName(String bucketName) {
      this.bucketName = bucketName;
      return this;
    }

    public Builder setKeyName(String keyName) {
      this.keyName = keyName;
      return this;
    }

    public Builder setDataSize(long dataSize) {
      this.dataSize = dataSize;
      return this;
    }

    public Builder setModificationTime(long modificationTime) {
      this.modificationTime = modificationTime;
      return this;
    }

    public Builder setEncInfo(FileEncryptionInfo encInfo) {
      this.encInfo = encInfo;
      return this;
    }

    public Builder setFileChecksum(FileChecksum fileChecksum) {
      this.fileChecksum = fileChecksum;
      return this;
    }

    @Override
    public Builder setMetadata(Map<String, String> metadata) {
      super.setMetadata(metadata);
      return this;
    }

    @Override
    public Builder addMetadata(String key, String value) {
      super.addMetadata(key, value);
      return this;
    }

    @Override
    public Builder addAllMetadata(Map<String, String> additionalMetadata) {
      super.addAllMetadata(additionalMetadata);
      return this;
    }

    public OmMultipartPartInfo build() {
      return new OmMultipartPartInfo(this);
    }
  }

  public static OmMultipartPartInfo getFromProto(
      MultipartPartInfo multipartPartInfo) {
    Builder builder = new Builder()
        .setPartName(multipartPartInfo.getPartName())
        .setPartNumber(multipartPartInfo.getPartNumber())
        .setVolumeName(multipartPartInfo.getVolumeName())
        .setBucketName(multipartPartInfo.getBucketName())
        .setKeyName(multipartPartInfo.getKeyName())
        .setDataSize(multipartPartInfo.getDataSize())
        .setModificationTime(multipartPartInfo.getModificationTime())
        .setEncInfo(null);

    if (multipartPartInfo.hasFileEncryptionInfo()) {
      builder.setEncInfo(
          OMPBHelper.convert(multipartPartInfo.getFileEncryptionInfo()));
    }

    if (multipartPartInfo.hasFileChecksum()) {
      builder.setFileChecksum(
          OMPBHelper.convert(multipartPartInfo.getFileChecksum()));
    }

    if (multipartPartInfo.getMetadataCount() > 0) {
      builder.addAllMetadata(
          KeyValueUtil.getFromProtobuf(multipartPartInfo.getMetadataList()));
    }

    return builder.build();
  }

  public MultipartPartInfo getProto() {
    return MultipartPartInfo.newBuilder()
        .setPartName(partName)
        .setPartNumber(partNumber)
        .setVolumeName(volumeName)
        .setBucketName(bucketName)
        .setKeyName(keyName)
        .setDataSize(dataSize)
        .setModificationTime(modificationTime)
        .setFileEncryptionInfo(OMPBHelper.convert(encInfo))
        .setFileChecksum(OMPBHelper.convert(fileChecksum))
        .build();
  }
}
