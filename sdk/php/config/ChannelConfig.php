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
class ChannelConfig extends WebMediumConfig {
    public ?String $type;
	public ?String $videoAccessMode;
	public ?String $audioAccessMode;
	public ?String $messages;
	public ?String $usersOnline;
	public ?String $adminsOnline;

    public function getType() : ?String {
        return "channel";
    }

    public function stats() : string{
        return $this->usersOnline . " users online, " . $this->adminsOnline . " admins";
    }

    public function credentials() : WebMediumConfig {
        $config = new ChannelConfig();
        $config->id = $this->id;
        return $config;
    }

    public function toXML() : String {
        $writer .= "<channel";
        if(isset($this->type) && !empty($this->type)) {
            $writer .= " type=\"" . $this->type . "\"";
        }
        if(isset($this->videoAccessMode)) {
            $writer .= " videoAccessMode=\"" . $this->videoAccessMode . "\"";
        }
        if(isset($this->audioAccessMode)) {
            $writer .= " audioAccessMode=\"" . $this->audioAccessMode . "\"";
        }
        $this->writeXML($writer);
        $writer .="</channel>";
        return $writer;
    }

    public function parseXML($xml) : void {
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
        //$this->user = $tempXML->user;
        $this->type = $xmlData->attributes()->type;
        $this->videoAccessMode = $xmlData->attributes()->videoAccessMode;
        $this->audioAccessMode = $xmlData->attributes()->audioAccessMode;
        $this->messages = $xmlData->attributes()->messages;
        $this->usersOnline = $xmlData->attributes()->usersOnline;
        $this->adminsOnline = $xmlData->attributes()->adminsOnline;
    }
}
?>