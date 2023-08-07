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
        count: 84
      },
      {
        keyName: "demo35",
        count: 50
      },
      {
        keyName: "demo1",
        count: 48
      },
      {
        keyName: "demo46",
        count: 48
      },
      {
        keyName: "demo61",
        count: 48
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