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
class UserMessageConfig extends Config {
    public ?String $id;
	public ?String $creationDate;
	public ?String $owner;
	public ?String $creator;
	public ?String $target;
	public ?String $parent;
	public ?String $subject;
	public ?String $message;

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
        $this->id = $xmlData->attributes()->id;
        $this->creationDate = $xmlData->attributes()->creationDate;
        $this->owner = $xmlData->attributes()->owner;
        $this->creator = $xmlData->attributes()->creator;
        $this->target = $xmlData->attributes()->target;
        $this->parent = $xmlData->attributes()->parent;
        if(isset($xmlData->subject)) {
            $this->subject = $xmlData->subject;
        }
        if(isset($xmlData->message)) {
            $this->message = $xmlData->message;
        }
    }

    public function toXML() : String {
        $writer .= "<user-message ";
        $this->writeCredentails($writer);
        if(isset($this->id)) {
            $writer .= " id=\"" . $this->id . "\"";
        }
        if(isset($this->creationDate)) {
            $writer .= " creationDate=\"" . $this->creationDate . "\"";
        }
        if(isset($this->owner)) {
            $writer .= " owner=\"" . $this->owner . "\"";
        }
        if (isset($this->creator)) {
            $writer .= " creator=\"" . $this->creator . "\"";
        }
        if (isset($this->target)) {
            $writer .= " target=\"" . $this->target . "\"";
        }
        if (isset($this->parent) ) {
            $writer .= " parent=\"" . $this->parent . "\"";
        }
        $writer .= ">";
        if(isset($this->subject)){
            $writer .= "<subject>";
            $writer .= Utils::escapeHTML($this->subject);
            $writer .= "</subject>";
        }
        if(isset($this->message)) {
            $writer .= "<message>";
            $writer .= Utils::escapeHTML($this->message);
            $writer .= "</message>";
        }
        $writer .= "</user-message>";
        return $writer;
    }
}
?>