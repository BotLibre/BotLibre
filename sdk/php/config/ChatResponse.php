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
class ChatResponse extends Config {
    public string $message;
    public String $question;
	public String $log;
	
	public  $conversation;
	
	public String $emote;
	
	public ?String $action;
	
	public ?String $pose;
	
	public ?String $avatar;
	
	public ?String $avatar2;
	
	public ?String $avatar3;
	
	public ?String $avatar4;
	
	public ?String $avatar5;
	
	public ?String $avatarType;
	
	public ?String $avatarTalk;
	
	public ?String $avatarTalkType;
	
	public ?String $avatarAction;
	
	public ?String $avatarActionType;
	
	public ?String $avatarActionAudio;
	
	public ?String $avatarActionAudioType;
	
	public ?String $avatarAudio;
	
	public ?String $avatarAudioType;
	
	public ?String $avatarBackground;
	
	public ?String $speech;
	
	public ?String $command;

	public function isVideo() : bool {
		return $this->avatarType != null && strpos($this->avatarType, "video") !== false;
	}
	public function isVideoTalk() {
		return $this->avatarTalkType != null && strpos($this->avatarTalkType, "video") !== false;
	}
	public function parseXML($xml) : void {
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
		#All attributes
		$this->conversation = $xmlData->attributes()->conversation;
		$this->emote = $xmlData->attributes()->emote;
		$this->action = $xmlData->attributes()->action;
		$this->pose = $xmlData->attributes()->pose;
		$this->avatar = $xmlData->attributes()->avatar;
		$this->avatar2 = $xmlData->attributes()->avatar2;
		$this->avatar3 = $xmlData->attributes()->avatar3;
		$this->avatar4 = $xmlData->attributes()->avatar4;
		$this->avatar5 = $xmlData->attributes()->avatar5;
		$this->avatarType = $xmlData->attributes()->avatarType;
		$this->avatarTalk = $xmlData->attributes()->avatarTalk;
		$this->avatarTalkType = $xmlData->attributes()->avatarTalkType;
		$this->avatarAction = $xmlData->attributes()->avatarAction;
		$this->avatarActionType = $xmlData->attributes()->avatarActionType;
		$this->avatarActionAudio = $xmlData->attributes()->avatarActionAudio;
		$this->avatarActionAudioType = $xmlData->attributes()->avatarActionAudioType;
		$this->avatarAudio = $xmlData->attributes()->avatarAudio;
		$this->avatarAudioType = $xmlData->attributes()->avatarAudioType;
		$this->avatarBackground = $xmlData->attributes()->avatarBackground;
		$this->speech = $xmlData->attributes()->speech;
		$this->command = $xmlData->attributes()->command;

		#All TagName
		$this->message = $xmlData->message;
        $this->question = $xmlData->question;
	}
	
}
?>