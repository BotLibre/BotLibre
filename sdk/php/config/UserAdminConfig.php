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
class UserAdminConfig extends Config {
    public String $operation;
    public String $operationUser;

    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }
        $this->operation = $xmlData->attributes()->operation;
        $this->operationUser = $xmlData->attributes()->operationUser;
    }

    public function toXML() : String {
        $writer = "<user-admin";
        $this->writeCredentails($writer);

        if(isset($this->operation)) {
            $writer.= " operation=\"" . $this->operation . "\"";
        }
        if(isset($this->operationUser)) {
            $writer.= " operationUser=\"" . $this->operationUser . "\"";
        }
        $writer.= "/>";
        return $writer;
    }
}
?>