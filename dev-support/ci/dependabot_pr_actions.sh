#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
cURL='/usr/bin/curl'
cURL_ARGS='-fsSL'
PNPM_URL="https://get.pnpm.io/install.sh"
PNPM_VERSION=$1
if [[ -n "$PNPM_VERSION" ]]; then
  echo "Fetching pnpm from $PNPM_URL with version $PNPM_VERSION"
  cURL_RESULT="$($cURL $cURL_ARGS | env PNPM_VERSION=$PNPM_VERSION sh -)"
  echo $cURL_RESULT
  # Install pnpm dependencies and re-create lockfile
  pnpm config set store-dir ~/.pnpm-store
  pnpm install --lockfile-only
  git add ./hadoop-ozone/recon/src/main/resources/webapps/recon/ozone-recon-web/pnpm-lock.yaml
  git commit -m \"Updated lockfile using pnpm\"
  git push