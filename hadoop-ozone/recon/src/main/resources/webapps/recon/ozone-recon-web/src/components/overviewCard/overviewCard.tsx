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

import React, {ReactElement} from 'react';
import { Card, Row, Col} from 'antd';
import { Link } from 'react-router-dom';
import StorageBar from '../storageBar/storageBar';
import {IStorageReport} from 'types/datanode.types';
import './overviewCard.less';

const {Meta} = Card;

interface IOverviewCardProps {
  icon: React.ReactElement;
  data: string | ReactElement;
  title: string;
  hoverable?: boolean;
  loading?: boolean;
  linkToUrl?: string;
  storageReport?: IStorageReport;
  error?: boolean;
}

const defaultProps: object = {
  hoverable: false,
  loading: false,
  linkToUrl: '',
  error: false
};

interface IOverviewCardWrapperProps {
  linkToUrl: string;
  children: React.ReactNode
}

function OverviewCardWrapper(props: IOverviewCardWrapperProps) {
  const {linkToUrl, children} = props;
    if (linkToUrl) {
      return (
        <Link to={linkToUrl}>
          {children}
        </Link>
      );
    }

    return children;
}

function OverviewCard(props: IOverviewCardProps) {
  let {icon, data, title, loading, hoverable, storageReport, linkToUrl, error} = props;
    let meta = <Meta title={data} description={title}/>;
    const errorClass = error ? 'card-error' : '';
    if (storageReport) {
      meta = (
        <div className='ant-card-percentage'>
          {meta}
          <div className='storage-bar'>
            <StorageBar total={storageReport.capacity} used={storageReport.used} remaining={storageReport.remaining} showMeta={false}/>
          </div>
        </div>
      );
    }

    linkToUrl = linkToUrl ? linkToUrl : '';

    return (
      <OverviewCardWrapper linkToUrl={linkToUrl}>
        <Card className={`overview-card ${errorClass}`} loading={loading} hoverable={hoverable}>
          <Row type='flex' justify='space-between'>
            <Col span={18}>
              <Row>
                {meta}
              </Row>
            </Col>
            <Col span={6}>
              {icon}
            </Col>
          </Row>
        </Card>
      </OverviewCardWrapper>
    );

}

export default OverviewCard;
