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

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosResponse } from 'axios';
import { Layout, Menu, Spin } from 'antd';
import {
  BarChartOutlined,
  ClusterOutlined,
  ContainerOutlined,
  ControlOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  DeploymentUnitOutlined,
  FileOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  InboxOutlined,
  LayoutOutlined,
  PieChartOutlined,
  ToolOutlined
} from '@ant-design/icons';
import { useLocation, Link } from 'react-router-dom';

import './navBar.less';


// ------------- Types -------------- //
type NavBarProps = {
  collapsed: boolean;
  onCollapse: (arg0: boolean) => void;
  menuItems: JSX.Element[]
}

const NavBar: React.FC<NavBarProps> = ({
  collapsed = false,
  onCollapse = () => { },
  menuItems = []
}) => {
  const location = useLocation();

  return (
    <Layout.Sider
      collapsible
      collapsed={collapsed}
      collapsedWidth={50}
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0
      }}
      onCollapse={onCollapse}
    >
      <div className='logo-v2'>
        <span className='logo-text-v2'>HDDS SCM</span>
      </div>
      <Menu
        theme='dark'
        defaultSelectedKeys={['/Dashboard']}
        mode='inline'
        selectedKeys={[location.pathname]} >
        {...menuItems}
      </Menu>
    </Layout.Sider>
  );
}

export default NavBar;
