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
class VoiceConfig extends Config
{

    public ?string $voice;
    public $nativeVoice;
    public ?string $language;
    public ?string $pitch;
    public ?string $speechRate;
    public ?string $mod;

    public function parseXML($xml): void
    {
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
        $this->voice = $xmlData->attributes()->voice;
        $this->nativeVoice = $xmlData->attributes()->nativeVoice;
        $this->language = $xmlData->attributes()->language;
        $this->pitch = $xmlData->attributes()->pitch;
        $this->mod = $xmlData->attributes()->mod;
        $this->speechRate = $xmlData->attributes()->speechRate;
    }

    public function toXML(): string
    {
        $writer .= "<voice";
        $this->writeCredentails($writer);
        if (isset($this->voice)) {
            $writer .= " voice=\"" . $this->voice . "\"";
        }
        if (isset($this->nativeVoice)) {
            $writer .= " nativeVoice=\"" . $this->nativeVoice . "\"";
        }
        if (isset($this->language)) {
            $writer .= " language=\"" . $this->language . "\"";
        }
        if (isset($this->pitch)) {
            $writer .= " pitch=\"" . $this->pitch . "\"";
        }
        if (isset($this->speechRate)) {
            $writer .= " speechRate=\"" . $this->speechRate . "\"";
        }
        if (isset($this->mod)) {
            $writer .= " mod=\"" . strtolower($this->mod) . "\"";
        }
        $writer .= "/>";
        return $writer;
    }
}
?>