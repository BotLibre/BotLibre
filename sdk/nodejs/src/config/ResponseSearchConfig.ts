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
import { XMLWriter } from "../util/Utils"
/**
 * DTO for XML response search options.
 */

export default class ResponseSearchConfig extends Config {
    responseType?:string
    inputType?:string
    filter?:string
    duration?:string
    restrict?:string
    page?:string

    toXML():string {
        let writer = new XMLWriter('<response-search')
        this.writeCredentials(writer)
        writer.appendAttribute('responseType', this.responseType)
        writer.appendAttribute('inputType', this.inputType)
        writer.appendAttribute('filter', this.filter)
        writer.appendAttribute('duration', this.duration)
        writer.appendAttribute('restrict', this.restrict)
        writer.appendAttribute('page', this.page)
        writer.append('/>')
        return writer.toString()
    }
}
