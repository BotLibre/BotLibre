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
import WebMediumConfig from "./WebMediumConfig"

export default class ScriptConfig extends WebMediumConfig {
    language?:string
    version?:string

    getType() :string {
        return 'script'
    }

    credentials(): WebMediumConfig {
        let config = new ScriptConfig()
        config.id = this.id
        return config
    }

    toXML(): string {
        let writer = new XMLWriter('<script')
        writer.appendAttribute('language', this.language)
        writer.appendAttribute('version', this.version)
        this.writeXML(writer)
        writer.append('</script>')
        return writer.toString()
    }

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.language = reader.readAttribute('name')
        this.version = reader.readAttribute('version')
    }

    toString(): string | undefined {
        return this.name
    }
}
