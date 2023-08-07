import React, { CSSProperties } from 'react';
import { HeatmapCellProps } from './heatmapComponentInterfaces';

const heatmapCellStyle: CSSProperties = {
  //setup basic cell css properties
  display: 'block',
  cursor: 'pointer'
}

export const HeatmapCellComponent = (props: HeatmapCellProps) => {
  const { value, key, ...pos } = props;
  const cellProps: React.SVGProps<SVGRectElement> = {
    ...pos,
    style: {...heatmapCellStyle}
  }

  return <rect {...cellProps}/>
}