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
 * DTO for XML voice config.
 */

export default class LearningConfig extends Config {
    learningMode?:string
	learningRate?:string
	correctionMode?:string
	enableComprehension?:boolean
	enableEmoting?:boolean
	enableEmotions?:boolean
	enableConsciousness?:boolean
	enableWiktionary?:boolean
	enableResponseMatch?:boolean
	learnGrammar?:boolean
	synthesizeResponse?:boolean
	fixFormulaCase?:boolean
	checkExactMatchFirst?:boolean
	scriptTimeout?:number
	responseMatchTimeout?:number
	conversationMatchPercentage?:string
	discussionMatchPercentage?:string

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.learningMode = reader.readAttribute('learningMode')
        this.learningRate = reader.readAttribute("learningRate")
		this.correctionMode = reader.readAttribute("correctionMode")
		this.enableComprehension = reader.readAttribute("enableComprehension")
		this.enableEmoting = reader.readAttribute("enableEmoting")
		this.enableEmotions = reader.readAttribute("enableEmotions")
		this.enableConsciousness = reader.readAttribute("enableConsciousness")
		this.enableWiktionary = reader.readAttribute("enableWiktionary")
		this.enableResponseMatch = reader.readAttribute("enableResponseMatch")
		this.learnGrammar = reader.readAttribute("learnGrammar")
		this.synthesizeResponse = reader.readAttribute("synthesizeResponse")
		this.fixFormulaCase = reader.readAttribute("fixFormulaCase")
		this.checkExactMatchFirst = reader.readAttribute("checkExactMatchFirst")
        this.scriptTimeout = reader.readAttribute('scriptTimeout')
        this.responseMatchTimeout = reader.readAttribute('responseMatchTimeout')
        this.conversationMatchPercentage = reader.readAttribute('conversationMatchPercentage')
        this.discussionMatchPercentage = reader.readAttribute('discussionMatchPercentage')
    }

    toXML():string {
        let writer = new XMLWriter('<learning')
        this.writeCredentials(writer)
        writer.appendAttribute('learningMode',this.learningMode)
        writer.appendAttribute('correctionMode', this.correctionMode)
        writer.appendAttribute('enableComprehension', this.enableComprehension)
        writer.appendAttribute('enableEmoting', this.enableEmoting)
        writer.appendAttribute('enableEmotions', this.enableEmotions)
        writer.appendAttribute('enableConsciousness', this.enableComprehension)
        writer.appendAttribute('enableWiktionary', this.enableWiktionary)
        writer.appendAttribute('enableResponseMatch', this.enableResponseMatch)
        writer.appendAttribute('learnGrammar', this.learnGrammar)
        writer.appendAttribute('synthesizeResponse', this.synthesizeResponse)
        writer.appendAttribute('fixFormulaCase', this.fixFormulaCase)
        writer.appendAttribute('checkExactMatchFirst', this.checkExactMatchFirst)
        writer.appendAttribute('scriptTimeout', this.scriptTimeout?.toString())
        writer.appendAttribute('responseMatchTimeout', this.responseMatchTimeout?.toString())
        writer.appendAttribute('conversationMatchPercentage',this.conversationMatchPercentage)
        writer.appendAttribute('discussionMatchPercentage', this.discussionMatchPercentage)
        writer.appendAttribute('learningRate',this.learningRate)
        writer.append('/>')
        return writer.toString()
    }
}