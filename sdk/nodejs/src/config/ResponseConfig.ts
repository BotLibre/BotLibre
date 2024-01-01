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

import Config from "./Config"
import { XMLReader, XMLWriter } from "../util/Utils"
/**
 * DTO for XML response config.
 */

export default class ResponseConfig extends Config {
    questionId?:string
    responseId?:string
    question?:string
    response?:string
    previous?:string
    onRepeat?:string
    label?:string
    topic?:string
    keywords?:string
    required?:string
    emotions?:string
    actions?:string
    poses?:string
    noRepeat?:boolean
    requirePrevious?:boolean
    requireTopic?:boolean
    flagged?:boolean
    correctness?:string
    command?:string

    toXML(): string {
        let writer = new XMLWriter('<response')
        this.writeXML(writer)
        return writer.toString()
    }

    writeXML(writer: XMLWriter) {
        this.writeCredentials(writer)
        writer.appendAttribute('questionId', this.questionId)
        writer.appendAttribute('responseId', this.responseId)
        writer.appendAttribute('label', this.label)
        writer.appendAttribute('topic', this.topic)
        writer.appendAttribute('keywords', this.keywords)
        writer.appendAttribute('required', this.required)
        writer.appendAttribute('emotions', this.emotions)
        writer.appendAttribute('actions', this.actions)
        writer.appendAttribute('poses', this.poses)
        writer.appendAttribute('correctness', this.correctness)
        writer.appendAttribute('noRepeat', this.noRepeat)
        writer.appendAttribute('requirePrevious', this.requirePrevious)
        writer.appendAttribute('requireTopic', this.requireTopic)
        writer.appendAttribute('flagged', this.flagged)
        writer.append('>')
        //Elements
        writer.appendElement('question', this.question, true)
        writer.appendElement('response', this.response, true)
        writer.appendElement('previous', this.previous, true)
        writer.appendElement('onRepeat', this.onRepeat, true)
        writer.appendElement('command', this.command, true)
        writer.append('</response>')
    }

    parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.questionId = reader.readAttribute('id')
        this.responseId = reader.readAttribute('responseId')
        this.label = reader.readAttribute('label')
        this.topic = reader.readAttribute('topic')
        this.keywords = reader.readAttribute('keywords')
        this.required = reader.readAttribute('required')
        this.emotions = reader.readAttribute('emotion')
        this.actions = reader.readAttribute('actions')
        this.poses = reader.readAttribute('poses')
        this.type = reader.readAttribute('type')
        this.correctness = reader.readAttribute('correctness')
        this.noRepeat = reader.readAttribute('noRepeat')
        this.flagged = reader.readAttribute('flagged')
        this.requireTopic = reader.readAttribute('requireTopic')
        this.requirePrevious = reader.readAttribute('requirePrevious')
        //Elements
        this.question = reader.readElement('question')
        this.response = reader.readElement('response')
        this.command = reader.readElement('command')
    }
}