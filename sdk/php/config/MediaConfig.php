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

 class MediaConfig extends Config {
    public $id;
	public ?String $name;
	public ?String $type;
	public ?String $file;
	public ?String $key;

    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }

        $this->id = $xmlData->attributes()->id;
        $this->name = $xmlData->attributes()->name;
        $this->type = $xmlData->attributes()->type;
        $this->file = $xmlData->attributes()->file;
        $this->key = $xmlData->attributes()->key;
    }

    public function toXML():  String {
        $writer = "";
        $writer .= "<media";
        $this->writeCredentails($writer);
        if(isset($this->name)) {
            $writer .= " name=\"" . $this->name . "\"";
        }
        if(isset($this->file)) {
            $writer .= " file=\"" . $this->file . "\"";
        }
        if(isset($this->key)) {
            $writer .= " key=\"" . $this->key . "\"";
        }
        $writer .= "/>";
        return $writer;
    }
 }
 ?>