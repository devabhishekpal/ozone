package org.apache.hadoop.ozone.om.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.fs.FileChecksum;
import org.apache.hadoop.fs.FileEncryptionInfo;
import org.apache.hadoop.hdds.utils.db.Codec;
import org.apache.hadoop.hdds.utils.db.DelegatedCodec;
import org.apache.hadoop.hdds.utils.db.Proto2Codec;
import org.apache.hadoop.ozone.ClientVersion;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.KeyLocationList;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.MultipartPartInfo;
import org.apache.hadoop.ozone.protocolPB.OMPBHelper;

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
  private final List<OmKeyLocationInfoGroup> keyLocationInfos;
  private final FileEncryptionInfo encInfo;
  private final FileChecksum fileChecksum;

  public static final String OPEN_KEY_METADATA_KEY = "multipart.openKey";

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
    this.keyLocationInfos = Collections.unmodifiableList(b.keyLocationInfos);
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
    private List<OmKeyLocationInfoGroup> keyLocationInfos;
    private FileEncryptionInfo encInfo;
    private FileChecksum fileChecksum;

    protected Builder() {
      this.keyLocationInfos = new ArrayList<>();
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
      this.keyLocationInfos = new ArrayList<>(obj.keyLocationInfos);
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

    public Builder setKeyLocationInfos(List<OmKeyLocationInfoGroup> keyLocationInfos) {
      this.keyLocationInfos = new ArrayList<>(keyLocationInfos);
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
        .setKeyLocationInfos(getKeyLocationInfosFromProto(multipartPartInfo))
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
    MultipartPartInfo.Builder builder = MultipartPartInfo.newBuilder()
        .setPartName(partName)
        .setPartNumber(partNumber)
        .setVolumeName(volumeName)
        .setBucketName(bucketName)
        .setKeyName(keyName)
        .addAllKeyLocationList(getKeyLocationInfosAsProto())
        .setDataSize(dataSize)
        .setModificationTime(modificationTime)
        .addAllMetadata(KeyValueUtil.toProtobuf(getMetadata()));
    if (encInfo != null) {
      builder.setFileEncryptionInfo(OMPBHelper.convert(encInfo));
    }
    if (fileChecksum != null) {
      builder.setFileChecksum(OMPBHelper.convert(fileChecksum));
    }
    return builder.build();
  }

  public String getPartName() {
    return partName;
  }

  public int getPartNumber() {
    return partNumber;
  }

  public String getVolumeName() {
    return volumeName;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getKeyName() {
    return keyName;
  }

  public long getDataSize() {
    return dataSize;
  }

  public long getModificationTime() {
    return modificationTime;
  }

  public List<OmKeyLocationInfoGroup> getKeyLocationInfos() {
    return keyLocationInfos;
  }

  public FileEncryptionInfo getEncInfo() {
    return encInfo;
  }

  public FileChecksum getFileChecksum() {
    return fileChecksum;
  }

  public static OmMultipartPartInfo from(
      String volumeName, String bucketName, String keyName, String openKey,
      String partName, int partNumber, OmKeyInfo omKeyInfo) {
    Builder builder = new Builder()
        .setPartName(partName)
        .setPartNumber(partNumber)
        .setVolumeName(volumeName)
        .setBucketName(bucketName)
        .setKeyName(keyName)
        .setDataSize(omKeyInfo.getDataSize())
        .setModificationTime(omKeyInfo.getModificationTime())
        .setKeyLocationInfos(omKeyInfo.getKeyLocationVersions())
        .addMetadata(OPEN_KEY_METADATA_KEY, openKey)
        .addAllMetadata(omKeyInfo.getMetadata());
    return builder.build();
  }

  public String getOpenKey() {
    return getMetadata().get(OPEN_KEY_METADATA_KEY);
  }

  private List<KeyLocationList> getKeyLocationInfosAsProto() {
    List<KeyLocationList> keyLocations = new ArrayList<>();
    for (OmKeyLocationInfoGroup keyLocationInfoGroup : keyLocationInfos) {
      keyLocations.add(keyLocationInfoGroup.getProtobuf(
          true, ClientVersion.CURRENT_VERSION));
    }
    return keyLocations;
  }

  private static List<OmKeyLocationInfoGroup> getKeyLocationInfosFromProto(
      MultipartPartInfo multipartPartInfo) {
    List<OmKeyLocationInfoGroup> keyLocations = new ArrayList<>();
    for (KeyLocationList keyLocationList
        : multipartPartInfo.getKeyLocationListList()) {
      keyLocations.add(OmKeyLocationInfoGroup.getFromProtobuf(keyLocationList));
    }
    return keyLocations;
  }
}
