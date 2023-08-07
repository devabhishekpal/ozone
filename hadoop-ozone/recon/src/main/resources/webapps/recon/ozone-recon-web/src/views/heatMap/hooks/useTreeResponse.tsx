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

import axios from 'axios';
import { useEffect, useState } from 'react';
import { showDataFetchError } from 'utils/common';

interface IBucketChildren {
    label: string,
    size: number,
    accessCount: number
}

interface IBucket {
    label: string,
    size: number,
    path: string,
    accessCount: number,
    children: IBucketChildren[]
}

interface IVolume {
    label: string,
    size: number,
    path: string,
    accessCount: number,
    children: IBucket[]
}

interface ITreeResponse {
    label: string,
    children: IVolume[]
}

export const useTreeResponse = (treeEndpoint: string) => {
    const [ data, setData ] = useState<ITreeResponse>({label: "", children: []});
    const [ isLoading, setIsLoading ] = useState<boolean>(false);
    const [ serverError, setServerError ] = useState<boolean>(false);


    useEffect(() => {
        const fetchTreeResponse = () => {
            setIsLoading(true);
            axios.get(treeEndpoint).then(response => {
                let treeResponse: ITreeResponse = response.data;
                console.log("treeResponse --> ", treeResponse);
                setData(treeResponse);
                setIsLoading(false);
            }).catch(error => {
                setIsLoading(false);
                setServerError(true);
                showDataFetchError(error.toString());
            });
        };

        fetchTreeResponse();
    }, [treeEndpoint])

    return { data, isLoading, serverError };
}