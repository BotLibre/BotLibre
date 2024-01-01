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

import Config from './Config'
import {XMLWriter} from '../util/Utils'
export default class BrowseConfig extends Config {
   
    typeFilter?:string
    category?:string
    tag?:string
    filter?:string
    userFilter?:string
    sort?:string
    restrict?:string
    page?:string
    contentRating?:string
    toXML():string {
        let writer = new XMLWriter('<browse')
        this.writeCredentials(writer)
        writer.appendAttribute('typeFilter', this.typeFilter)
       
        writer.appendAttribute('userFilter', this.userFilter)
        writer.appendAttribute('sort', this.sort)
        writer.appendAttribute('restrict', this.restrict)
        writer.appendAttribute('category', this.category)
        writer.appendAttribute('tag', this.tag)
        writer.appendAttribute('filter', this.filter)
        writer.appendAttribute('page', this.page)
        writer.appendAttribute('contentRating', this.contentRating)
        writer.append('/>')
        return writer.toString()
    }
}