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
class ChatConfig extends Config{
    public string $conversation;
    public bool $correction;
    public bool $offensive;
    public bool $disconnect;
    public string $emote;
    public string $action;
    public string $message;
    public bool $speak;
	public bool $includeQuestion;
	public bool $avatarHD;
	public String $avatarFormat;
	public String $avatar;
	public String $language;
	public String $voice;
    public function toXML() : String {
        $writer = "<chat";
        $this->writeCredentails($writer);
        if (isset($this->conversation)) {
            $writer .= " conversation=\"" . $this->conversation . "\"";
        }
        if (isset($this->emote)) {
            $writer .=  " emote=\"" . $this->emote . "\"";
        }
        if (isset($this->action)) {
            $writer .=  " action=\"" . $this->action . "\"";
        }
        if (isset($this->correction)) {
            $writer .=  " correction=\"" . $this->correction . "\"";
        }
        if (isset($this->offensive)) {
            $writer .=  " offensive=\"" . $this->offensive . "\"";
        }
        if (isset($this->speak)) {
            $writer .=  " speak=\"" . $this->speak . "\"";
        }
        if (isset($this->avatar)) {
            $writer .=  " avatar=\"" . $this->avatar . "\"";
        }
        if (isset($this->avatarHD)) {
            $writer .=  " avatarHD=\"" . $this->avatarHD . "\"";
        }
        if (isset($this->avatarFormat)) {
            $writer .=  " avatarFormat=\"" . $this->avatarFormat . "\"";
        }
        if (isset($this->language)) {
            $writer .=  " language=\"" . $this->language . "\"";
        }
        if (isset($this->voice)) {
            $writer .=  " voice=\"" . $this->voice . "\"";
        }
        if (isset($this->includeQuestion)) {
            $writer .=  " includeQuestion=\"" . $this->includeQuestion . "\"";
        }
        if (isset($this->disconnect)) {
            $writer .=  " disconnect=\"" . $this->disconnect . "\"";
        }
        $writer .=  ">";
        if (isset($this->message)) {
            $writer .=  "<message>";
            $writer .=  Utils::escapeHTML($this->message);
            $writer .=  "</message>";
        }
        $writer .=  "</chat>";
        return $writer;
    }
}
?>