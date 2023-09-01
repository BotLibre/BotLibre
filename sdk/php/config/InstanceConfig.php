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
class InstanceConfig extends WebMediumConfig {
    public ?string $size;
    public ?string $instanceAvatar;
    public $allowForking;
    public $hasAPI;
    public ?string $template;
    public int $rank;
    public int $wins;
    public int $losses;



    public function getType() : String {
        return "instance";
    }

    public function stats() : String {
        return $this->connects . " connects, " . $this->dailyConnects . " today, " . $this->weeklyConnects . " weeks, " . $this->monthlyConnects . " month";
    }

    public function credentials() : InstanceConfig {
        $config = new InstanceConfig();
        $config->id =  $this->id;
        return $config;
    }

    public function toXML() : string {
        $writer = "";
        $writer .= "<instance";
        if($this->allowForking) {
            $writer .= " allowForking=\"true\"";
        }
        if(isset($this->instanceAvatar)){
            $writer .= " instanceAvatar=\"" . $this->instanceAvatar . "\"";
        }
        /*
        * When used together, the $ and & signs are used to pass variables by reference in PHP functions.
        * writeXML paramater is using both.
        */
        $this->writeXML($writer);
        if(isset($this->template)) {
            $writer .= "<template>";
            $writer .= $this->template;
            $writer .= "</template>";
        }
        $writer .= "</instance>";
        return $writer;
    }

    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if($xmlData===false) {
            return;
        }
        $this->allowForking = $xmlData->attributes()->allowForking;
        $this->hasAPI = $xmlData->attributes()->hasAPI;
        $this->size = $xmlData->attributes()->size;
        $this->instanceAvatar = $xmlData->attributes()->instanceAvatar;
        if(isset($xmlData->attributes()->rank)) {
            $this->rank = (int)$xmlData->attributes()->rank;
        }
        if(isset($xmlData->attributes()->wins)) {
            $this->wins = (int)$xmlData->attributes()->wins;
        }
        if(isset($xmlData->attributes()->losses)) {
            $this->losses = (int)$xmlData->attributes()->losses;
        }
        if(isset($xmlData->template)) {
            $this->template = (string) $xmlData->template[0];
        }
    }
}
?>