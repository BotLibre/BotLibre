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
 * DTO for XML user admin config.
 */
export default class UserAdminConfig extends Config {
    operation?:string
    operationUser?:string

    parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.operation = reader.readAttribute('operation')
        this.operationUser = reader.readAttribute('operationUser')
    }

    toXML(): string {
        let writer = new XMLWriter('<user-admin')
        this.writeCredentials(writer)
        writer.appendAttribute('operation', this.operation)
        writer.appendAttribute('operationUser', this.operationUser)
        writer.append('/>')
        return writer.toString()  
    }
}
