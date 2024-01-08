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

import { XMLReader, XMLWriter } from "../util/Utils"
import Config from "./Config"

export default class ScriptSourceConfig extends Config {
    id?:string
    creationDate?:string
    updateDate?:string
    version?:string
    versionName?:string
    creator?:string
    source?:string


    toXML():string {
        let writer = new XMLWriter('<script-source')
        this.writeCredentials(writer)
        writer.appendAttribute('id', this.id)
        writer.appendAttribute('creationDate', this.creationDate)
        writer.appendAttribute('updateDate', this.updateDate)
        writer.appendAttribute('version', this.version)
        writer.appendAttribute('creator', this.creator)
        writer.append('>')
        //Elements
        writer.appendElement('<source>',this.source, true)
        writer.append('</script-source>')
        return writer.toString()
    }

    parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.id = reader.readAttribute('id')
        this.creationDate = reader.readAttribute('creationDate')
        this.updateDate = reader.readAttribute('updateDate')
        this.version = reader.readAttribute('version')
        this.versionName = reader.readAttribute('versionName')
        this.creator = reader.readAttribute('creator')
        //Read Element
        this.source = reader.readElement('source')
    }

    //TODO: getNextVersion()
}