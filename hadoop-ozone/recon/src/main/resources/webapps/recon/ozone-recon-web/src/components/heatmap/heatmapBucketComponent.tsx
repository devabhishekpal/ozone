/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { useEffect, useMemo, useState } from "react";
import COLOR_SCHEMES from "../../views/heatMap/constants/heatmapConstants";
import { Button, Collapse, Dropdown, Icon } from "antd";
import { byteToSize } from "utils/common";
import { DownOutlined } from "@ant-design/icons";
import { HeatmapDetailComponent } from "./heatmapDetailComponent";

interface IBucketChildren {
  label: string,
  size: number,
  accessCount: number
}

interface IBucket {
  label: string,
  size: number,
  path: string,
  accessCount: number,
  children: IBucketChildren[]
}

interface HeatmapBucketProps {
  data: IBucket;
  theme: string;
}

const CircleIndicator = () => (
  <svg width="15" height="15" viewBox="0 0 15 15">
    <circle cx="7.5" cy="7.5" r="7.5" fill="currentColor" />
  </svg>
)

export const HeatmapBucketComponent: React.FC<HeatmapBucketProps> = ({ data, theme }) => {
  const dummyValues = 
    [
      {
        keyName: "demo2",
        count: 100
      },
      {
        keyName: "demo35",
        count: 50
      },
      {
        keyName: "demo1",
        count: 2
      },
      {
        keyName: "demo46",
        count: 48
      },
      {
        keyName: "demo61",
        count: 10
      }
    ]

  return (
    <>
    {
      data.children.map((child, idx) => {
        const panelHeader = (
          <div className="bucket-content">
                {child.label.toLocaleUpperCase()}
                <div className="bucket-details">
                  <Icon component={CircleIndicator} style={{color: "red", marginRight: "10px"}}/>
                  Access Count:&nbsp;<strong>{child.accessCount}</strong>
                  Size: <strong>&nbsp;{byteToSize(child.size, 4)}</strong>
                </div>
              </div>
        )
        return (
          <Collapse
            key={child.label + idx}
            expandIconPosition="right"
            className="bucket-container">
            <Collapse.Panel
              header={panelHeader}>
                <HeatmapDetailComponent
                  heatmapValues={dummyValues}/>
            </Collapse.Panel>
            </Collapse>
        )
      })
    }
    </>
  )
}