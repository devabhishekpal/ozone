export type HeatMapValue = {
  keyName: string;
  count: number;
};

export interface HeatmapDetailProps{
  heatmapValues?: Array<HeatMapValue>;
  panelColours?: Record<number, string>;
}

export interface HeatmapCellProps extends React.SVGProps<SVGRectElement> {
  value?: HeatMapValue & {
    column: number;
    row: number;
    index: number;
  };
}