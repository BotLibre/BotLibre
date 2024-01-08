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

import { XMLReader, XMLWriter } from "../util/Utils";
import Config from "./Config";


export default class AvatarMedia extends Config {
    mediaId?:string
    name?:string
    media?:string
    emotions?:string
    actions?:string
    poses?:string
    hd?:boolean
    talking?:boolean

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.mediaId = reader.readAttribute('mediaId')
        this.name = reader.readAttribute('name')
        this.media = reader.readAttribute('media')
        this.emotions = reader.readAttribute('emotion')
        this.actions = reader.readAttribute('actions')
        this.poses = reader.readAttribute('poses')
        this.hd = reader.readAttribute('hd')
        this.talking = reader.readAttribute('talking')
    }

    toXML(): string {
        let writer = new XMLWriter("<avatar-media")
        this.writeCredentials(writer)
        writer.appendAttribute('mediaId', this.media)
        writer.appendAttribute('name', this.name)
        writer.appendAttribute('emotions', this.emotions)
        writer.appendAttribute('actions', this.actions)
        writer.appendAttribute('poses', this.poses)
        writer.appendAttribute('hd', this.hd)
        writer.appendAttribute('talking', this.talking)
        writer.append('/>')
        return writer.toString()
    }

    isVideo(): boolean {
        return this.type?.indexOf("video") !== -1
    }

    isAudio(): boolean {
        return this.type?.indexOf("audio") !== -1
    }

}