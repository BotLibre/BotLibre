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
class Speech extends Config {
    public ?string $text;
    public ?string $voice;

    public function toXML() : ?String {
        $writer .= "<speech";
        $this->writeCredentails($writer);
        if(isset($this->text)) {
            $writer .= " text=\"" .  $this->text . "\"";
        }
        if(isset($this->voice)) {
            $writer .= " voice=\"" .  $this->voice . "\"";
        }
        $writer .= "/>";
        return $writer;
    } 
}
?>