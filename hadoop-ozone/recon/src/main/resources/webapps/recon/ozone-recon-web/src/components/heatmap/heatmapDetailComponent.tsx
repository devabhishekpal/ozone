import React, { CSSProperties, useMemo } from 'react';
import { HeatmapDetailProps, HeatmapCellProps, HeatMapValue } from './heatmapComponentInterfaces';
import { HeatmapCellComponent } from './heatmapCellComponent';

const formatHeatmapData = ( data: HeatmapDetailProps['heatmapValues'] = []) => {
  const formattedData: Record<string, HeatMapValue> = {}
  data.forEach((item: HeatMapValue) => {
    formattedData[item.keyName] = item
  });
  return formattedData;
}

const getHeatmapKeys = (data: HeatmapDetailProps['heatmapValues'] = []) => {
  let keys: string[] = [];
  data.forEach((item: HeatMapValue) => {
    keys = [...keys, item.keyName]
  });
  return keys;
}

const mapHeatmapColour = (count: number, thresholds: number[], panelColours: Record<number, string>) => {
  //take the panel colours available, and map the count to the nearest threshold
  let colour: string = '';
  for(let x = 0; x < thresholds.length; x += 1){
    if (thresholds[x] >= count){
      colour = panelColours[thresholds[x]];
      break;
    }
    colour = panelColours[thresholds[x]];
  }
  return colour;
}

export const HeatmapDetailComponent = (props: HeatmapDetailProps) => {
  const {
    heatmapValues = [],
    panelColours = {0: '#EBEDF0', 4: '#C6E48B', 8: '#7BC96F', 12: '#239A3B', 32: '#196127' },
  } = props || {};

  const svgRef = React.createRef<SVGSVGElement>();
  const keys = useMemo(() => getHeatmapKeys(heatmapValues), [heatmapValues])
  const thresholds = useMemo(() => (Object.keys(panelColours).map((item) => parseInt(item, 10))), [panelColours]);
  const data = useMemo(() => formatHeatmapData(heatmapValues), [heatmapValues]);
  const numRows = Math.floor(heatmapValues.length / 10); // We want to have max 10 blocks per row
  const numCols = heatmapValues.length - numRows; // Store the final number of colums left over
  const cellSize = 11;
  const cellSpacing = 2;

  const heatmapStyle = {
    color: '#24292e',
    userSelect: 'none',
    display: 'block',
    fontSize: 10,
  } as CSSProperties;

  return (
    <svg ref={svgRef} style={{ ...heatmapStyle }}>
      <g transform={"translate(5, 5)"}>
        {[...Array(numRows)].map((_, idx) => {
            return (
              <g key={idx} data-column={idx}>
                {[...Array(10)].map((_, cidx) => {
                  const keyIdx = idx * 10 + cidx;
                  const dataProps: HeatmapCellProps['value'] = {
                    ...data[keys[keyIdx]],
                    row: cidx,
                    column: idx,
                    index: keyIdx,
                  };
                  const heatmapCellProps: HeatmapCellProps = {
                    key: cidx,
                    fill: mapHeatmapColour(data[keys[keyIdx]].count || 0, thresholds, panelColours),
                    width: cellSize + 10,
                    height: cellSize,
                    x: idx * ( cellSize + cellSpacing ),
                    y: ( cellSpacing + cellSize ) * cidx,
                    value: dataProps
                  }

                  return (
                    <HeatmapCellComponent
                      {...heatmapCellProps}
                      value={dataProps}
                      data-name={keys[keyIdx]}
                      data-index={dataProps.index}
                      data-row={dataProps.row}
                      data-column={dataProps.column}
                    />
                  );
                })}
              </g>
            );
          })}
        {[...Array(numCols)].map((_, cidx) => {
                  const keyIdx = numRows * 10 + cidx;
                  const dataProps: HeatmapCellProps['value'] = {
                    ...data[keys[keyIdx]],
                    row: cidx,
                    column: numRows,
                    index: keyIdx,
                  };
                  const heatmapCellProps: HeatmapCellProps = {
                    key: cidx,
                    fill: mapHeatmapColour(data[keys[keyIdx]].count || 0, thresholds, panelColours),
                    width: cellSize + 10,
                    height: cellSize,
                    x: numRows * ( cellSize + cellSpacing ),
                    y: ( cellSpacing + cellSize ) * cidx,
                    value: dataProps
                  }

                  return (
                    <HeatmapCellComponent
                      {...heatmapCellProps}
                      value={dataProps}
                      data-name={keys[keyIdx]}
                      data-index={dataProps.index}
                      data-row={dataProps.row}
                      data-column={dataProps.column}
                    />
                  );
                })}
      </g>
    </svg>
  );
}