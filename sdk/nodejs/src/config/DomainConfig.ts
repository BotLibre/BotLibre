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

import { XMLReader, XMLWriter } from '../util/Utils'
import WebMediumConfig from './WebMediumConfig'
class DomainConfig extends WebMediumConfig {
    creationMode?: string
    getType(): string {
        return "domain"
    }
    credentials(): WebMediumConfig {
        let config = new DomainConfig()
        config.id = this.id
        return config
    }

    toXML(): string {
       let writer  = new XMLWriter("<domain")
       writer.appendAttribute("creationMode", this.creationMode)
       this.writeXML(writer)
       writer.append("</domain>")
       return writer.toString()
    }

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.creationMode = reader.readAttribute('creationMode')
    }
}
export default DomainConfig