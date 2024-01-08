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

import { XMLWriter, XMLReader } from "../util/Utils";
import WebMediumConfig from "./WebMediumConfig";

/**
 * DTO for XML domain config.
 */

export default class ForumConfig extends WebMediumConfig {

    replyAccessMode?: string
    postAccessMode?:string
    posts?:string


    stats(): string {
        return this.posts + " posts"
    }
    getType(): string {
        return "forum"
    }
    credentials(): WebMediumConfig {
        let config = new ForumConfig()
        config.id = this.id
        return config
    }

    toXML(): string {
        let writer = new XMLWriter('<forum')
        writer.appendAttribute('replyAccessMode', this.replyAccessMode)
        writer.appendAttribute('postAccessMode', this.postAccessMode)
        this.writeXML(writer)
        writer.append('</forum>')
        return writer.toString()
    }


    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.replyAccessMode = reader.readAttribute('replyAccessMode')
        this.postAccessMode = reader.readAttribute('postAccessMode')
        this.posts = reader.readAttribute('posts')
    }

}