<?php
/******************************************************************************
 *
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
 *
 ******************************************************************************/
class BotModeConfig extends Config {
    public ?String $mode;
    public ?String $bot;

    public function parseXML($xml):void {
        parent::parseXML($xml);
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: ";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
        }
        // else {
        //     print_r($xmlData);
        // }
        $this->mode = $xmlData->attributes()->mode;
        $this->bot = $xmlData->attributes()->bot;
    }

    public function toXML() : string {
        $writer .= "<bot-mode";
        $this->writeCredentails($writer);
        if(isset($this->mode)){
            $writer .= " mode=\"" + $this->mode + "\"";
        }
        if(isset($this->bot)){
            $writer .= " bot=\"" + $this->bot + "\"";
        }
        $writer .= "/>";
        return $writer;
    }
}
 ?>