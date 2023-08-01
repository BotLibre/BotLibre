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
class GraphicConfig extends WebMediumConfig {
    public ?String $media;
    public ?String $fileName;
    public ?String $fileType;




    public function toXML() : String {
        $writer .= "<graphic";
        if(isset($this->media)) {
            $writer .= " media=\"" . $this->media . "\"";
        }
        if(isset($this->fileName)) {
            $writer .= " fileName=\"" . $this->fileName . "\"";
        }
        if(isset($this->fileType)) {
            $writer .= " fileType=\"" . $this->fileType . "\"";
        }
        $this->writeXML($writer);
        $writer .= "</graphic>";
        return $writer;
    }



    
	public function getType() : ?String {
		return "graphic";
	}

    public function credentials() : ?WebMediumConfig {
		$config = new GraphicConfig();
		$config->id = $this->id;
		return $config;
	}

    public function parseXML($xml) : void {
        parent::parseXML($xml);
        //loading xml
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
        $this->media = $xmlData->attributes()->media;
        $this->fileName = $xmlData->attributes()->fileName;
        $this->fileType = $xmlData->attributes()->fileType;
    }

    public function isVideo () : bool {
        return isset($this->fileType) && strpos($this->fileType, 'video') !== false;
    }

    public function isAudio () : bool {
        return isset($this->fileType) && strpos($this->fileType, 'audio') !== false;
    }
}
 ?>