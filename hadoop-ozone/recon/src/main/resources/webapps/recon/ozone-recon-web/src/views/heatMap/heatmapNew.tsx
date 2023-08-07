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

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Row, Icon, Button, Input, Dropdown, Menu, DatePicker, Layout } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import moment, { Moment } from 'moment';
import Sider from 'antd/lib/layout/Sider';

import { useTreeResponse } from './hooks/useTreeResponse';
import { HeatmapCalendarComponent } from 'components/heatmap/heatmapCalendarComponent';
import { showDataFetchError } from 'utils/common';
import * as CONSTANTS from './constants/heatmapConstants';
import './heatmapNew.less';
import { HeatmapBucketComponent } from 'components/heatmap/heatmapBucketComponent';

export const HeatmapImproved = () => {
  const { data, isLoading, serverError } = useTreeResponse("/api/v1/finalHeatmap");

  const [date, setDate] = useState<string | number>(CONSTANTS.TIME_PERIODS[0])
  const [inputPath, setInputPath] = useState<string>(CONSTANTS.ROOT_PATH);
  const [selectedVolume, setSelectedVolume] = useState<string>("");
  // const [inputPathValidity, setInputPathValidity] = useState<boolean>(true)
  // const [helpMessage, setHelpMessage] = useState<string>("");

  const volumes = data.children;
  
  useEffect(() => {
    if (Object.keys(volumes).length > 0){
      setSelectedVolume(volumes[0].label)
    }
  }, [data]);

  useEffect(() => {
    console.log(date);
    //TODO: Call API with the updated date to fetch data on the timeperiod
  }, [date])

  const handleCalendarChange = (e: any) => {
    if (CONSTANTS.TIME_PERIODS.includes(e.key)) {
      setDate(e.key)
    }
  };

  const handleVolumeChange = (e: any) => {
    if (Object.keys(volumes).length > 0 
      && volumes.map(child => {return child.label}).includes(e.key)){
        setSelectedVolume(e.key)
      }
  }

  const onDateChange = (date: string[] | number[]) => {
    setDate(moment(date).unix());
  };

  const disabledDate = (current: Moment) => {
    return current > moment() || current < moment().subtract(90, 'day');
  }

  const resetInputpath = (path: string) => {
    if (!path || path === '/') {
      return;
    }
    else {
      setInputPath(
        '/'
      );
    }
  };

  const handleChange = (e: any) => {
    let inputPath = e.target.value;
    let inputValid = true;
    let helpMessage = "";
    // Only allow letters, numbers,underscores and forward slashes
    const regex = /^[a-zA-Z0-9_/]*$/;
    if (!regex.test(inputPath)) {
      helpMessage = "Please enter valid path";
      inputValid = false;
      inputPath = CONSTANTS.ROOT_PATH;
    }
    setInputPath(inputPath)
    setInputPathValidity(inputValid);
    setHelpMessage(helpMessage);
  }

  // const updateTreeMap = (path: string, entityType: string, date: string) => {
  //   this.setState({
  //     isLoading: true,
  //     treeEndpointFailed: false
  //   });

  //   if (date && path && entityType) {
  //     const treeEndpoint = `/api/v1/heatmap/readaccess?startDate=${date}&path=${path}&entityType=${entityType}`;
  //     axios.get(treeEndpoint).then(response => {
  //       minSize = this.minmax(response.data)[0];
  //       maxSize = this.minmax(response.data)[1];
  //       let treeResponse: ITreeResponse = this.updateSize(response.data);
  //       console.log("Final treeResponse--", treeResponse);
  //       this.setState({
  //         isLoading: false,
  //         showPanel: false,
  //         treeResponse
  //       });
  //     }).catch(error => {
  //       this.setState({
  //         isLoading: false,
  //         inputPath: '',
  //         entityType: '',
  //         date: '',
  //         treeEndpointFailed: true
  //       });
  //       showDataFetchError(error.toString());
  //     });
  //   }
  //   else {
  //     this.setState({
  //       isLoading: false
  //     });
  //   }
  // };

  // const handleSubmit = (e: any) => {
  //   // Avoid empty request trigger 400 response
  //   updateTreeMap(inputPath, entityType, date);
  // };
  const menuComponent = (
    // <Menu
    //   defaultSelectedKeys={[date as string]}
    //   onClick={handleCalendarChange}>
    //   <Menu.Item key='24H'>
    //     24 Hour
    //   </Menu.Item>
    //   <Menu.Item key='7D'>
    //     7 Days
    //   </Menu.Item>
    //   <Menu.Item key='90D'>
    //     90 Days
    //   </Menu.Item>
    //   <Menu.SubMenu title="Custom Select Last 90 Days">
    //     <Menu.Item>
    //       <DatePicker
    //         format="YYYY-MM-DD"
    //         onChange={onChange}
    //         disabledDate={disabledDate}
    //       />
    //     </Menu.Item>
    //   </Menu.SubMenu>
    // </Menu>
    <HeatmapCalendarComponent
      date={date as string}
      handleCalendarChange={e => handleCalendarChange(e)}
      onChange={e => onDateChange(e)}
      disabledDate={disabledDate}/>
    
  );
  return (
    <div className='heatmap-container'>
      <div className='page-header'>
        Tree Map for Entities
      </div>
      <div className='content-div'>
        {isLoading ? <span><Icon type='loading' /> Loading...</span> : (
          <div>
            {serverError ? <div className='heatmapinformation'><br />Failed to Load Heatmap.{''}<br /></div> :
              (Object.keys(volumes).length > 0) ?
                <div className="heatmap-content">
                  <div className="heatmap-sidepanel">
                    <div className="path-container">
                      <div className='go-back-button'>
                        <Button type='primary' onClick={e => resetInputpath(inputPath)}><Icon type='undo' /></Button>
                      </div>
                      <div className='path-input-container'>
                        <form className='input' autoComplete="off" id='input-form'>
                          Path
                          <Input placeholder='Path' name="inputPath" value={inputPath} />
                        </form>
                      </div>
                    </div>
                    <div>
                      <Dropdown
                        overlay={menuComponent}
                        placement='bottomLeft'>
                        <Button id='date-dropdown-button'>
                          Last &nbsp;{
                            (date as number) > 100 ?
                              new Date((date as number) * 1000).toLocaleString() : date
                          } <DownOutlined id='date-dropdown-down-icon'/>
                        </Button>
                      </Dropdown>
                    </div>
                    <div id='volume-container'>
                      <h3>Volumes</h3>
                      <Menu
                        defaultSelectedKeys={[volumes[0].label]}
                        onClick={e => handleVolumeChange(e)}>
                        {volumes.map((child) => {return <Menu.Item key={child.label}>/{child.label}</Menu.Item>})}
                      </Menu>
                    </div>
                  </div>
                  <div className="heatmap-mainarea">
                    <h2>Buckets in {selectedVolume.toUpperCase()}</h2>
                    {/* <Dropdown overlay= */}
                    <div className="bucket-list">
                      <HeatmapBucketComponent data={data.children.find(child => child.label === selectedVolume)}/>
                    </div>
                  </div>
                </div>
                :
                <div className='heatmapinformation'><br />
                  No Data Available. {' '}<br />
                </div>
            }

          </div>
        )
        }
      </div>
    </div>
  );
}