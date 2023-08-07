import React from "react";
import { DatePicker, Menu } from "antd";

interface HeatmapCalendarProps {
  date: string;
  handleCalendarChange: (_e: any) => void;
  onChange: (_e: any) => void;
  disabledDate: (_current: any) => boolean;
}

export const HeatmapCalendarComponent: React.FC<HeatmapCalendarProps> = ({
  date,
  handleCalendarChange,
  onChange,
  disabledDate
}) => {

  return (
    <Menu
      defaultSelectedKeys={[date]}
      onClick={handleCalendarChange}>
      <Menu.Item key='24H'>
        24 Hour
      </Menu.Item>
      <Menu.Item key='7D'>
        7 Days
      </Menu.Item>
      <Menu.Item key='90D'>
        90 Days
      </Menu.Item>
      <Menu.SubMenu title="Custom Select Last 90 Days">
        <Menu.Item>
          <DatePicker
            format="YYYY-MM-DD"
            onChange={onChange}
            disabledDate={disabledDate}
          />
        </Menu.Item>
      </Menu.SubMenu>
    </Menu>
  )
}