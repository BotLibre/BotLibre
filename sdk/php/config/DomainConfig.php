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
require_once('WebMediumConfig.php');
class DomainConfig extends WebMediumConfig {
    public ?String $creationMode;

    public function getType() : String {
        return "domain";
    }

    public function credentials() : WebMediumConfig {
        $config = new DomainConfig();
        $config->id = $this->id;
        return $config;
    }

    public function toXML() : String {
        $writer = "<domain";
        if(isset($this->creationMode)) {
            $writer.= " creationMode=\"" . $this->creationMode . "\"";
        }
        $this->writeXML($writer);
        $writer.= "</domain>";
        return $writer;
    }
    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if($xmlData === false) {
            return;
        }
        $this->creationMode = $xmlData->attributes()->creationMode;
    }
}
?>