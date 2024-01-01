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

import { XMLWriter } from "../util/Utils"
import Config from "./Config"
/**
 * A speech object can be used to invoke server-side text-to-speech.
 * The object takes a message text, and a voice.
 */
export default class Speech extends Config {
    text?:string
    voice?:string
    mod?:string

    toXML() :string {
        let writer = new XMLWriter('<speech')
        this.writeCredentials(writer)
        writer.appendAttribute('text', this.text)
        writer.appendAttribute('voice', this.voice)
        writer.appendAttribute('mod', this.mod?.toLocaleLowerCase())
        writer.append('/>')
        return writer.toString()
    }
}