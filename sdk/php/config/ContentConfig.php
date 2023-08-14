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
class ContentConfig extends Config {
    public ?String $type;
	public ?String $name;
	public ?String $icon;
	public ?String $description;

    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }
        $this->type = $xmlData->attributes()->type;
        $this->name = $xmlData->attributes()->name;
        $this->icon = $xmlData->attributes()->icon;
        if(isset($xmlData->description)) {
            $this->description = $xmlData->description;
        }
    }

    public function toXML() : String {
        $writer = "";
        $writer .= "<content";
        $this->writeCredentails($writer); // WriteCredentials It also sets the type.
        // if (isset($this->type)) {
        //     $writer .= " type=\"" . $this->type . "\"";
        // }
        $writer .= "/>";
        return $writer;
    }
}
?>