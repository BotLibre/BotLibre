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
require_once('./sdk/SDKConnection.php');
require_once('./sdk/Credentials.php');
class Config
{
    public ?string $application;
    public ?string $domain;
    public ?string $user;
    public ?string $token;
    public ?string $instance;
    public ?string $type;

    public function addCredentials(SDKConnection $connection) :void
    {
        $this->application = $connection->getCredentials()->getApplicationId();
        if (!isset($this->user) && $connection->getUser() != null) {
            $this->user = $connection->getUser()->user;
            $this->token = $connection->getUser()->token;
        }
        if ($connection->getDomain() != null && $this->domain == null) {
			$this->domain = $connection->getDomain()->id;
		}
    }

    public function toXML() : string
    {
        return "<config/>";
    }

    public function parseXML($xml) : void
    {
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: <br>";
            var_dump($xml);
            echo "<br>";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
            return;
        } else {
            print_r($xmlData);
        }
        //$this->user = $tempXML->user;
        
        $this->application = $xmlData->attributes()->application;
        $this->domain = $xmlData->attributes()->domain;
        $this->user = $xmlData->attributes()->user;
        $this->token = $xmlData->attributes()->token;
        $this->instance = $xmlData->attributes()->type;
    }
    public function writeCredentails(&$writer) : string
    {
        if ($this->user != null && strlen($this->user > 0)) {
            $writer .= " user=\"" . $this->user . "\"";
        }
        if (isset($this->token) && strlen($this->token) > 0) {
            $writer .= " token=\"" . $this->token . "\"";
        }
        if (isset($this->type) ) {
            $writer .= " type=\"" . $this->type . "\"";
        }
        if (isset($this->instance)) {
            $writer .= " instance=\"" . $this->instance . "\"";
        }
        if (isset($this->application)) {
            $writer .= " application=\"" . $this->application . "\"";
        }
        if (isset($this->domain) ) {
            $writer .= " domain=\"" . $this->domain . "\"";
        }
        return $writer;
    }
}
?>