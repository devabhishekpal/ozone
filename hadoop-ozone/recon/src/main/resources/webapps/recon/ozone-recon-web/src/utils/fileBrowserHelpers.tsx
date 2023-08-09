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

import { BaseFileComponent } from "components/filebrowser/fileComponent/baseFile";
import { BaseFolderComponent } from "components/filebrowser/folderComponent/baseFolder";

interface IFileTreeProps {
    contents: any[];
    children: BaseFileComponent | BaseFolderComponent | {}
}

function isFolder(file: BaseFileComponent | BaseFolderComponent) {
    console.log(`Type of ${file.key} is ${typeof (file)}`)
    return file.key.endsWith('/')
}

function groupByFolder(files: any, root: any) {
    const fileTree: IFileTreeProps = {
        contents: [],
        children: {}
    }
    console.log("groupByFolder:: typeof(files): " + typeof (files))
    console.log("groupByFolder:: typeof(root): " + typeof (root))

    files.map((file: any) => {
        file.relativeKey = (file.newKey || file.key).substr(root.length)
        let currentFolder = fileTree
        const folders = file.relativeKey.split('/')
        folders.map((folder: any, folderIndex: number) => {
            console.log("groupByFolder:: typeof(file): " + typeof (file))
            console.log("groupByFolder:: typeof(folder): " + typeof (folder))
            if (folderIndex === folders.length - 1 && isFolder(file)) {
                for (const key in file) {
                    currentFolder[key] = file[key]
                }
            }
            if (folder === '') {
                return
            }
            const isAFile = (!isFolder(file) && (folderIndex === folders.length - 1))
            if (isAFile) {
                currentFolder.contents.push({
                    ...file,
                    keyDerived: true,
                })
            } else {
                if (folder in currentFolder.children === false) {
                    currentFolder.children[folder] = {
                        contents: [],
                        children: {},
                    }
                }
                currentFolder = currentFolder.children[folder]
            }
        })
    })

    function addAllChildren(level: IFileTreeProps, prefix: string) {
        if (prefix !== '') {
            prefix += '/'
        }
        let files = []
        for (const folder in level.children) {
            files.push({
                ...level.children[folder],
                contents: undefined,
                keyDerived: true,
                key: root + prefix + folder + '/',
                relativeKey: prefix + folder + '/',
                children: addAllChildren(level.children[folder], prefix + folder),
                size: 0,
            })
        }
        files = files.concat(level.contents)
        return files
    }

    files = addAllChildren(fileTree, '')
    return files
}

export { isFolder, groupByFolder }