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

import * as xml2js from 'xml2js';
import SDKException from '../sdk/SDKException';
export class Utils {
    static async loadXML(xml: string): Promise<any> {
        let response: any
        xml2js.parseString(xml, (err, result) => {
            if (err) {
                return undefined
            }
            response = result
        });
        return response
    }

    static escapeHTML(html: string): string {
        const escapeMap: Record<string, string> = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "`": "&#96;",
            "'": "&#39;"
        };

        for (const [char, entity] of Object.entries(escapeMap)) {
            html = html.replace(new RegExp(char, "g"), entity);
        }

        return html;
    }
    static log(text: string, err: boolean = false): void {
        console.log("----------")
        if (err) {
            console.log("| err: " + text)
        } else {
            console.log("| log: " + text)
        }
        console.log("----------")
    }
}

//Reading XML data 
export class XMLReader {
    private root: any
    constructor(elem: any) {
        this.root = elem
    }
    readAttribute(attrName: string): any {
        return this.root.$?.[attrName];
    }

    readElement(elemName: string): string | undefined {
        return this.root?.[elemName]?.[0] ?? undefined;
    }
}


//Constructing XML data
export class XMLWriter {
    private buffer: string[] = [];

    constructor(tag: string) {
        this.buffer.push(tag);
    }

    append(text: string): void {
        this.buffer.push(text);
    }

    appendAttribute(attrTag: string, value?: string | boolean) {
        if (attrTag && value) {
            Utils.log("Attr tag: " + attrTag + " Value: " + value)
            this.buffer.push(" " + attrTag + "=\"" + value + "\"")
        }
    }

    appendElement(tag: string, value?: string, escapeHTML: boolean = false) {
        if (tag && value) {
            Utils.log("Ele tag: " + tag + " Value: " + value)
            this.buffer.push("<" + tag + ">")
            this.buffer.push(escapeHTML ? Utils.escapeHTML(value) : value)
            this.buffer.push("</" + tag + ">")
        }
    }

    toString(): string {
        return this.buffer.join('');
    }
    clear() {
        this.buffer = []
    }
}
