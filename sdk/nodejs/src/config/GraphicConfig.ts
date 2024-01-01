/*
*  Copyright 2023 Paphus Solutions Inc.
*
*  Licensed under the Eclipse Public License, Version 1.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.eclipse.org/legal/epl-v10.html
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

import WebMediumConfig from "./WebMediumConfig";
import {XMLReader, XMLWriter} from "../util/Utils"


export default class GraphicConfig extends WebMediumConfig {
    media?:string
    fileName?:string
    fileType?:string

    toXML(): string {
        let writer = new XMLWriter('<graphic')
        writer.appendAttribute('media', this.media)
        writer.appendAttribute('fileName', this.fileName)
        writer.appendAttribute('fileType', this.fileType)
        this.writeXML(writer)
        writer.append('</graphic>')
        return writer.toString()
    }

    getType():string {
        return "graphic"
    }

    credentials(): WebMediumConfig {
        let config = new GraphicConfig()
        config.id = this.id
        return config
    }

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.media = reader.readAttribute('media')
        this.fileName = reader.readAttribute('fileName')
        this.fileType = reader.readAttribute('fileType')
    }

    isVideo(): boolean {
        return this.fileType?.indexOf("video") !== -1
    }

    isAudio(): boolean {
        return this.fileType?.indexOf("audio") !== -1
    }
}