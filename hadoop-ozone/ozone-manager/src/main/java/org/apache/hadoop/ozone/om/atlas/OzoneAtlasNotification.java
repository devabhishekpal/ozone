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

package org.apache.hadoop.ozone.om.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lightweight Atlas notification model to avoid introducing Atlas runtime
 * dependencies in OM. This can be transformed to Atlas HookNotification or REST
 * payload by a publisher.
 */
public final class OzoneAtlasNotification {

  public enum Action {
    CREATE,
    DELETE
  }

  private final Action action;
  private final List<OzoneAtlasEntity> entities;

  private OzoneAtlasNotification(Action action,
      List<OzoneAtlasEntity> entities) {
    this.action = action;
    this.entities = entities;
  }

  public static OzoneAtlasNotification create(List<OzoneAtlasEntity> entities) {
    return new OzoneAtlasNotification(Action.CREATE,
        Collections.unmodifiableList(new ArrayList<>(entities)));
  }

  public static OzoneAtlasNotification delete(List<OzoneAtlasEntity> entities) {
    return new OzoneAtlasNotification(Action.DELETE,
        Collections.unmodifiableList(new ArrayList<>(entities)));
  }

  public Action getAction() {
    return action;
  }

  public List<OzoneAtlasEntity> getEntities() {
    return entities;
  }

  /**
   * Minimal Atlas entity representation.
   */
  public static final class OzoneAtlasEntity {
    private final String typeName;
    private final String qualifiedName;
    private final Map<String, Object> attributes;
    private final Map<String, OzoneAtlasObjectId> relationships;

    private OzoneAtlasEntity(Builder builder) {
      this.typeName = builder.typeName;
      this.qualifiedName = builder.qualifiedName;
      this.attributes = Collections.unmodifiableMap(
          new HashMap<>(builder.attributes));
      this.relationships = Collections.unmodifiableMap(
          new HashMap<>(builder.relationships));
    }

    public String getTypeName() {
      return typeName;
    }

    public String getQualifiedName() {
      return qualifiedName;
    }

    public Map<String, Object> getAttributes() {
      return attributes;
    }

    public Map<String, OzoneAtlasObjectId> getRelationships() {
      return relationships;
    }

    public static Builder builder(String typeName, String qualifiedName) {
      return new Builder(typeName, qualifiedName);
    }

    public static final class Builder {
      private final String typeName;
      private final String qualifiedName;
      private final Map<String, Object> attributes = new HashMap<>();
      private final Map<String, OzoneAtlasObjectId> relationships =
          new HashMap<>();

      private Builder(String typeName, String qualifiedName) {
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.qualifiedName =
            Objects.requireNonNull(qualifiedName, "qualifiedName");
      }

      public Builder attribute(String key, Object value) {
        if (value != null) {
          attributes.put(key, value);
        }
        return this;
      }

      public Builder relationship(String key, OzoneAtlasObjectId value) {
        if (value != null) {
          relationships.put(key, value);
        }
        return this;
      }

      public OzoneAtlasEntity build() {
        return new OzoneAtlasEntity(this);
      }
    }
  }

  /**
   * Minimal Atlas object id representation.
   */
  public static final class OzoneAtlasObjectId {
    private final String typeName;
    private final String qualifiedName;

    public OzoneAtlasObjectId(String typeName, String qualifiedName) {
      this.typeName = Objects.requireNonNull(typeName, "typeName");
      this.qualifiedName =
          Objects.requireNonNull(qualifiedName, "qualifiedName");
    }

    public String getTypeName() {
      return typeName;
    }

    public String getQualifiedName() {
      return qualifiedName;
    }
  }
}
