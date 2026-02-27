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

package org.apache.hadoop.ozone.om.helpers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.hadoop.hdds.utils.db.Codec;

/**
 * Typed key for multipart parts table.
 *
 * Key encoding:
 * <pre>
 *   uploadId(utf8) + '/' + partNumber(int32, big-endian)
 * </pre>
 * Prefix encoding for iteration:
 * <pre>
 *   uploadId(utf8) + '/'
 * </pre>
 */
public final class OmMultipartPartKey {
  private static final byte SEPARATOR = (byte) '/';
  private static final Codec<OmMultipartPartKey> CODEC =
      new OmMultipartPartKeyCodec();

  private final String uploadId;
  private final Integer partNumber;

  private OmMultipartPartKey(String uploadId, Integer partNumber) {
    this.uploadId = Objects.requireNonNull(uploadId, "uploadId is null");
    this.partNumber = partNumber;
  }

  public static OmMultipartPartKey of(String uploadId, int partNumber) {
    return new OmMultipartPartKey(uploadId, partNumber);
  }

  public static OmMultipartPartKey prefix(String uploadId) {
    return new OmMultipartPartKey(uploadId, null);
  }

  public static Codec<OmMultipartPartKey> getCodec() {
    return CODEC;
  }

  public String getUploadId() {
    return uploadId;
  }

  public Integer getPartNumber() {
    return partNumber;
  }

  public boolean hasPartNumber() {
    return partNumber != null;
  }

  @Override
  public String toString() {
    return hasPartNumber() ? uploadId + "/" + partNumber : uploadId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OmMultipartPartKey)) {
      return false;
    }
    OmMultipartPartKey that = (OmMultipartPartKey) o;
    return Objects.equals(uploadId, that.uploadId)
        && Objects.equals(partNumber, that.partNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uploadId, partNumber);
  }

  private static final class OmMultipartPartKeyCodec
      implements Codec<OmMultipartPartKey> {

    @Override
    public Class<OmMultipartPartKey> getTypeClass() {
      return OmMultipartPartKey.class;
    }

    @Override
    public byte[] toPersistedFormat(OmMultipartPartKey key) {
      byte[] uploadBytes = key.uploadId.getBytes(StandardCharsets.UTF_8);
      int size = uploadBytes.length + 1
          + (key.hasPartNumber() ? Integer.BYTES : 0);
      ByteBuffer buffer = ByteBuffer.allocate(size);
      buffer.put(uploadBytes);
      buffer.put(SEPARATOR);
      if (key.hasPartNumber()) {
        buffer.putInt(key.partNumber);
      }
      return buffer.array();
    }

    @Override
    public OmMultipartPartKey fromPersistedFormat(byte[] rawData) {
      if (rawData.length == 0) {
        throw new IllegalArgumentException(
            "Invalid multipart part key: empty key");
      }

      //   prefix key: uploadId + '/'
      //   full key:   uploadId + '/' + int32(partNumber)
      int suffixLength;
      if (rawData[rawData.length - 1] == SEPARATOR) {
        suffixLength = 0;
      } else if (rawData.length > Integer.BYTES
          && rawData[rawData.length - Integer.BYTES - 1] == SEPARATOR) {
        suffixLength = Integer.BYTES;
      } else {
        throw new IllegalArgumentException(
            "Invalid multipart part key: missing separator");
      }

      int separatorIndex = rawData.length - suffixLength - 1;
      String uploadId = new String(rawData, 0, separatorIndex,
          StandardCharsets.UTF_8);
      if (suffixLength == 0) {
        return new OmMultipartPartKey(uploadId, null);
      }

      int part = ByteBuffer.wrap(
          rawData, separatorIndex + 1, Integer.BYTES).getInt();
      return of(uploadId, part);
    }

    @Override
    public OmMultipartPartKey copyObject(OmMultipartPartKey object) {
      return object;
    }
  }
}
