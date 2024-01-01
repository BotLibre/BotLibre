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

import { Utils, XMLReader, XMLWriter } from '../util/Utils'
import Config from './Config'
export class ForumPostConfig extends Config {
    id?: string
    topic?: string
    summary?: string
    details?: string
    detailsText?: string
    forum?: string
    tags?: string
    thumbsUp: number = 0
    thumbsDown: number = 0
    stars: string = "0"
    isAdmin?: string
    isFlagged?: string
    flaggedReason?: string
    isFeatured?: string
    creator?: string
    creationDate?: string
    views?: string
    dailyViews?: string
    weeklyViews?: string
    monthlyViews?: string
    replyCount?: string
    parent?:string
    avatar?:string
    replies: Array<ForumPostConfig> = new Array<ForumPostConfig>()

    constructor() {
        super()
    }
    public toXML(): string {
        let writer = new XMLWriter("<forum-post")
        this.writeXML(writer)
        return writer.toString()
    }

    public writeXML(writer: XMLWriter):void {
        this.writeCredentials(writer)
        writer.appendAttribute('id', this.id)
        writer.appendAttribute('parent', this.parent)
        writer.appendAttribute('forum', this.forum)
        writer.appendAttribute('isFeatured', this.isFeatured)
        writer.appendAttribute('stars', this.stars)
        writer.append('>')
        writer.appendElement('topic', this.topic, true)
        writer.appendElement('details', this.details, true)
        writer.appendElement('tags', this.tags)
        writer.appendElement('flaggedReason', this.flaggedReason, true)
        writer.append("</forum-post>");
    }

    public credentials(): ForumPostConfig {
        let config = new ForumPostConfig()
        config.id = this.id
        return config
    }

    public parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.id = reader.readAttribute("id")
        this.parent = reader.readAttribute("parent")
        this.forum = reader.readAttribute("forum")
        this.views = reader.readAttribute("views")
        this.dailyViews = reader.readAttribute("dailyViews")
        this.weeklyViews = reader.readAttribute("weeklyViews")
        this.monthlyViews = reader.readAttribute("monthlyViews")
        this.isAdmin = reader.readAttribute("isAdmin")
        this.replyCount = reader.readAttribute("replyCount")
        this.isFlagged = reader.readAttribute("isFlagged")
        this.isFeatured = reader.readAttribute("isFeatured")
        this.creator = reader.readAttribute("creator")
        this.creationDate = reader.readAttribute("creationDate")
        this.thumbsUp = reader.readAttribute('thumbsUp')
        this.thumbsDown = reader.readAttribute('thumbsDown')
        this.stars = reader.readAttribute('stars')

        //Elements
        this.summary = reader.readElement('summary')
        this.details = reader.readElement('details')
        this.detailsText = reader.readElement('detailsText')
        this.topic = reader.readElement('topic')
        this.tags = reader.readElement('tags')
        this.flaggedReason = reader.readElement('flaggedReason')
        this.avatar = reader.readElement('avatar')
        //All Elements in replies
        const elements = reader.readElements('replies')
        elements.forEach((element) => {
            let config = new ForumPostConfig()
            config.parseXML(element)
            this.replies.push(config)
        })
    }
}