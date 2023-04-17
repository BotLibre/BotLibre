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
    public String $questionId;
	public String $responseId;
	public String $question;
	public String $response;
	public String $previous;
	public String $onRepeat;
	public String $label;
	public String $topic;
	public String $keywords;
	public String $required;
	public String $emotions;
	public String $actions;
	public String $poses;
	public $noRepeat;
	public $requirePrevious;
	public $requireTopic;
	public $flagged;
	public String $correctness;
	public String $command;

    // public function __construct () {

    // }

    public function toXML() : String {
		$writer .= "";
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
    }

    public function parseXML($xml) : void {
        
    }
}
?>