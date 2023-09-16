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