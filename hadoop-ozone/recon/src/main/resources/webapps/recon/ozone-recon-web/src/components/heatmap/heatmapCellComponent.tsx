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
import React, { useState, CSSProperties } from 'react';
import { createPortal } from 'react-dom';
import { HeatmapCellProps } from '../../types/heatmap.types';
import { getSVGMousePoint } from 'utils/svgPointHelper';
import { getContrastRatioForColor } from 'utils/contrastHelper';

const heatmapCellStyle: CSSProperties = {
  //setup basic cell css properties
  display: 'block',
  marginRight: '10px'
}

export const HeatmapCellComponent = (props: HeatmapCellProps) => {
  const { key, tooltipRef, value, ...pos } = props;
  console.log(pos)
  const cellProps: React.SVGProps<SVGRectElement> = {
    ...pos,
    style: {...heatmapCellStyle}
  }
  const headingTextColor = (getContrastRatioForColor(pos.fill!, "#FFFFFF") < 0.14285)
                            ? "#FFFFFF"
                            : "#242424"
  const [ tooltipType, setTooltipType ] = useState<string>("Hidden");
  const [ mouseX, setMouseX ] = useState<number | undefined>(undefined);
  const [ mouseY, setMouseY ] = useState<number | undefined>(undefined);

  const updateTooltip = (element: SVGSVGElement | null, e: React.MouseEvent) => {
      if (element !== null){
        const mousePos = getSVGMousePoint(element, e.nativeEvent);
        setMouseX(mousePos[0]);
        setMouseY(mousePos[1]);
        setTooltipType("Visible");
      }
  }

  const hideTooltip = () => {
    setTooltipType("Hidden");
  }

  const tooltip = (
    <g
      className="tooltip"
      transform={`translate(${mouseX}, ${mouseY})`}
      pointerEvents="none">
          {/* <rect x={0} y={0} width={150} height={75} rx={5} ry={5} fill={"#FFFFFF"} stroke='#D1D1D1'/>
          <g>
            <rect x={0} y={0} width={150} height={15} rx={5} ry={5} fill={`${pos.fill}`}/>
            
          </g> */}
          {/* <text x={10} y={30} fontSize={12} fill="#424242">{value!.count}</text> */}
          <rect width={150} height={75} rx={14} fill="white" stroke="#D1D1D1"/>
          <g>
            <path d="M0 11C0 4.92487 4.92487 0 11 0H139C145.075 0 150 4.92487 150 11V24H0V11Z"
              fill={`${pos.fill}`}
              stroke={`${pos.fill}`}/>
            <text x={10} y={15} fontSize={15} fill={headingTextColor} fontWeight={500}>{value!.keyName}</text>
          </g>
    </g>
  )
  
  return (
    <>
      <rect {...cellProps}
        onMouseOver={(e) => updateTooltip(tooltipRef.current, e)}
        onMouseMove={(e) => updateTooltip(tooltipRef.current, e)}
        onMouseLeave={hideTooltip}/>
        {tooltipType === "Hidden" ? <g/> : createPortal(tooltip, tooltipRef.current!)}
    </>
  )
}