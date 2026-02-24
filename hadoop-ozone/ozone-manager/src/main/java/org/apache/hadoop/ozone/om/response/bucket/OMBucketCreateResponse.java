/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.ozone.om.response.bucket;

import static org.apache.hadoop.ozone.om.codec.OMDBDefinition.BUCKET_TABLE;
import static org.apache.hadoop.ozone.om.codec.OMDBDefinition.VOLUME_TABLE;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.hadoop.hdds.utils.db.BatchOperation;
import org.apache.hadoop.ozone.om.OMMetadataManager;
import org.apache.hadoop.ozone.om.atlas.OzoneAtlasNotification;
import org.apache.hadoop.ozone.om.atlas.OzoneAtlasNotification.OzoneAtlasEntity;
import org.apache.hadoop.ozone.om.atlas.OzoneAtlasNotification.OzoneAtlasObjectId;
import org.apache.hadoop.ozone.om.helpers.OmBucketInfo;
import org.apache.hadoop.ozone.om.helpers.OmVolumeArgs;
import org.apache.hadoop.ozone.om.response.AtlasNotifiableResponse;
import org.apache.hadoop.ozone.om.response.CleanupTableInfo;
import org.apache.hadoop.ozone.om.response.OMClientResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.Status;

/**
 * Response for CreateBucket request.
 */
@CleanupTableInfo(cleanupTables = {BUCKET_TABLE, VOLUME_TABLE})
public final class OMBucketCreateResponse extends OMClientResponse
    implements AtlasNotifiableResponse {

  private final OmBucketInfo omBucketInfo;
  private final OmVolumeArgs omVolumeArgs;

  public OMBucketCreateResponse(@Nonnull OMResponse omResponse,
      @Nonnull OmBucketInfo omBucketInfo, @Nonnull OmVolumeArgs omVolumeArgs) {
    super(omResponse);
    this.omBucketInfo = omBucketInfo;
    this.omVolumeArgs = omVolumeArgs;
  }

  public OMBucketCreateResponse(@Nonnull OMResponse omResponse,
      @Nonnull OmBucketInfo omBucketInfo) {
    super(omResponse);
    this.omBucketInfo = omBucketInfo;
    this.omVolumeArgs = null;
  }

  /**
   * For when the request is not successful.
   * For a successful request, the other constructor should be used.
   */
  public OMBucketCreateResponse(@Nonnull OMResponse omResponse) {
    super(omResponse);
    checkStatusNotOK();
    omBucketInfo = null;
    omVolumeArgs = null;
  }

  @Override
  public void addToDBBatch(OMMetadataManager omMetadataManager,
      BatchOperation batchOperation) throws IOException {

    String dbBucketKey =
        omMetadataManager.getBucketKey(omBucketInfo.getVolumeName(),
            omBucketInfo.getBucketName());
    omMetadataManager.getBucketTable().putWithBatch(batchOperation,
        dbBucketKey, omBucketInfo);
    // update volume usedNamespace
    if (omVolumeArgs != null) {
      omMetadataManager.getVolumeTable().putWithBatch(batchOperation,
              omMetadataManager.getVolumeKey(omVolumeArgs.getVolume()),
              omVolumeArgs);
    }
  }

  @Nullable
  public OmBucketInfo getOmBucketInfo() {
    return omBucketInfo;
  }

  @Override
  public List<OzoneAtlasNotification> buildAtlasNotifications(
      String clusterName) {
    if (getOMResponse().getStatus() != Status.OK || omBucketInfo == null) {
      return Collections.emptyList();
    }
    String volume = omBucketInfo.getVolumeName();
    String bucket = omBucketInfo.getBucketName();
    String volumeQN = volume + "@" + clusterName;
    String bucketQN = bucket + "." + volume + "@" + clusterName;

    OzoneAtlasEntity volumeEntity = OzoneAtlasEntity.builder(
        "ozone_volume", volumeQN)
        .attribute("name", volume)
        .attribute("cluster", clusterName)
        .build();

    OzoneAtlasEntity.Builder bucketBuilder = OzoneAtlasEntity.builder(
        "ozone_bucket", bucketQN)
            .attribute("name", bucket)
            .attribute("volume", volume)
            .attribute("cluster", clusterName)
            .attribute("layout", omBucketInfo.getBucketLayout().name())
            .attribute("versioningEnabled", omBucketInfo.getIsVersionEnabled())
            .attribute("quotaBytes", omBucketInfo.getQuotaInBytes())
            .attribute("quotaNamespace", omBucketInfo.getQuotaInNamespace())
            .relationship("volumeRef",
                new OzoneAtlasObjectId("ozone_volume", volumeQN));

    if (omBucketInfo.getSourceVolume() != null
        && omBucketInfo.getSourceBucket() != null) {
      String srcQN = omBucketInfo.getSourceBucket() + "."
          + omBucketInfo.getSourceVolume() + "@" + clusterName;
      bucketBuilder.relationship("linkedBucket",
          new OzoneAtlasObjectId("ozone_bucket", srcQN))
          .attribute("sourceVolume", omBucketInfo.getSourceVolume())
          .attribute("sourceBucket", omBucketInfo.getSourceBucket());
    }

    OzoneAtlasEntity bucketEntity = bucketBuilder.build();

    return Collections.singletonList(
        OzoneAtlasNotification.create(Arrays.asList(volumeEntity, bucketEntity)));
  }

}

