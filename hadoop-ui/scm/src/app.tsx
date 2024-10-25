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

import React from 'react';

import { Layout, Menu } from 'antd';
import {
  BarChartOutlined,
  ClusterOutlined,
  ControlOutlined,
  DatabaseOutlined,
  FileOutlined,
  FileTextOutlined,
  ToolOutlined
} from '@ant-design/icons';
import { Link, Redirect, Route, HashRouter as Router, Switch } from 'react-router-dom';

import NavBar from '@shared/navBar/navBar';

import './app.less';

const {
  Header, Content, Footer
} = Layout;

const menuItems = [(
  <Menu.SubMenu key='metrics'
    title='Metrics'
    icon={<BarChartOutlined />}>
      <Menu.Item key='RPCMetrics'
        icon={<ClusterOutlined />}>
          <span>RPC Metrics</span>
          <Link to='/RpcMetrics' />
      </Menu.Item>
  </Menu.SubMenu>
), (
  <Menu.Item key='configuration'
    icon={<ControlOutlined />}>
      <span>Configuration</span>
      <Link to='/configuration' />
  </Menu.Item>
), (
  <Menu.SubMenu key='commonTools'
    icon={<ToolOutlined />}>
      <Menu.Item key='jmx'
        icon={<FileTextOutlined />}>
          <span>JMX</span>
          <Link to='/jmx' />
      </Menu.Item>
      <Menu.Item key='config'
        icon={<FileTextOutlined />}>
          <span>Configs</span>
          <Link to='/conf' />
      </Menu.Item>
      <Menu.Item key='stacks'
        icon={<DatabaseOutlined />}>
          <span>Stacks</span>
          <Link to='/stacks' />
      </Menu.Item>
      <Menu.Item key='logLevel'
        icon={<FileOutlined />}>
          <span>Log Level</span>
          <Link to='/logLevel' />
      </Menu.Item>
  </Menu.SubMenu>
)]

interface IAppState {
  collapsed: boolean;
}

class App extends React.Component<Record<string, object>, IAppState> {
  constructor(props = {}) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  onCollapse = (collapsed: boolean) => {
    this.setState({ collapsed });
  };

  render() {
    const { collapsed } = this.state;

    return (
      <Router>
        <Layout style={{ minHeight: '100vh' }}>
          <NavBar menuItems={menuItems} collapsed={collapsed} onCollapse={this.onCollapse} />
          <Layout>
            <Content style={{ margin: '0 16px 0', overflow: 'initial' }}>
              <Switch>
                <Route exact path='/#!'>
                  
                </Route>
              </Switch>
            </Content>
            <Footer style={{ textAlign: 'center' }} />
          </Layout>
        </Layout>
      </Router>
    );
  }
}

export default App;