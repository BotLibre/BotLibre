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
class ResponseConfig extends Config {
    public ?String $questionId;
	public ?String $responseId;
	public ?String $question;
	public ?String $response;
	public ?String $previous;
	public ?String $onRepeat;
	public ?String $label;
	public ?String $topic;
	public ?String $keywords;
	public ?String $required;
	public ?String $emotions;
	public ?String $actions;
	public ?String $poses;
	public $noRepeat;
	public $requirePrevious;
	public $requireTopic;
	public $flagged;
	public String $correctness;
	public String $command;

    public function toXML() : String {
		$writer = "";
		$this->writeXML($writer);
		return $writer;
	}

    public function writeXML(&$writer) : void {
        $writer .= "<response";
        $this->writeCredentails($writer);
        if(isset($this->questionId)) {
            $writer .= " questionId=\"" . $this->questionId . "\"";
        }
        if(isset($this->responseId)) {
            $writer .= " responseId=\"" . $this->responseId . "\"";
        }
		if(isset($this->label)) {
            $writer .= " label=\"" . $this->label . "\"";
        }
		if(isset($this->topic)) {
            $writer .= " topic=\"" . $this->topic . "\"";
        }
		if(isset($this->keywords)) {
            $writer .= " keywords=\"" . $this->keywords . "\"";
        }
		if(isset($this->required)) {
            $writer .= " required=\"" . $this->required . "\"";
        }
		if(isset($this->emotions)) {
            $writer .= " emotions=\"" . $this->emotions . "\"";
        }
		if(isset($this->actions)) {
            $writer .= " actions=\"" . $this->actions . "\"";
        }
		if(isset($this->poses)) {
            $writer .= " poses=\"" . $this->poses . "\"";
        }
		if(isset($this->correctness)) {
            $writer .= " correctness=\"" . $this->correctness . "\"";
        }
		$writer .= " noRepeat=\"" . $this->noRepeat . "\"";
		$writer .= " requirePrevious=\"" . $this->requirePrevious . "\"";
		$writer .= " requireTopic=\"" . $this->requireTopic . "\"";
		$writer .= " flagged=\"" . $this->flagged . "\"";
		$writer .= ">";
		if(isset($this->question)) {
			$writer .= "<question>";
			$writer .= Utils::escapeHTML($this->question);
			$writer .= "</question>";
		}
		if(isset($this->response)) {
			$writer .= "<response>";
			$writer .= Utils::escapeHTML($this->response);
			$writer .= "</response>";
		}
		if(isset($this->previous)) {
			$writer .= "<previous>";
			$writer .= Utils::escapeHTML($this->previous);
			$writer .= "</previous>";
		}
		if(isset($this->onRepeat)) {
			$writer .= "<onRepeat>";
			$writer .= Utils::escapeHTML($this->onRepeat);
			$writer .= "</onRepeat>";
		}
		if(isset($this->command)) {
			$writer .= "<command>";
			$writer .= Utils::escapeHTML($this->command);
			$writer .= "</command>";
		}
		$writer .= "</response>";
    }

    public function parseXML($xml) : void {
		$xmlData = Utils::loadXML($xml);
		if($xmlData===false) {
			return;
		}
        $this->questionId = $xmlData->attributes()->questionId;
        $this->responseId = $xmlData->attributes()->responseId;
        $this->label = $xmlData->attributes()->label;
		$this->topic = $xmlData->attributes()->topic;
		$this->keywords = $xmlData->attributes()->keywords;
		$this->required = $xmlData->attributes()->required;
		$this->emotions = $xmlData->attributes()->emotions;
        $this->actions = $xmlData->attributes()->actions;
        $this->poses = $xmlData->attributes()->poses;
		$this->type = $xmlData->attributes()->type;
		$this->correctness = $xmlData->attributes()->correctness;
		if(isset($xmlData->attributes()->noRepeat)) {
			$this->noRepeat =(bool) $xmlData->attributes()->noRepeat;
		}
		if(isset($xmlData->attributes()->requireTopic)) {
			$this->requireTopic = (bool) $xmlData->attributes()->requireTopic;
		}

		if(isset($xmlData->attributes()->flagged)) {
			$this->flagged = (bool) $xmlData->attributes()->flagged;
		}

		if(isset($xmlData->attributes()->requirePrevious)) {
			$this->requirePrevious =(bool)  $xmlData->attributes()->requirePrevious;
		}
		if(isset($xmlData->question)) {
			$this->question = $xmlData->question;
		}
		if(isset($xmlData->response)) {
			$this->response = $xmlData->response;
		}
		if(isset($xmlData->question)) {
			$this->question = $xmlData->question;
		}
		if(isset($xmlData->command)) {
			$this->command = $xmlData->command;
			$this->command = str_replace('&#34;', '"', $this->command);
		}

    }
}
?>