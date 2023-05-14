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
    class AvatarMessage extends Config {
        public ?string $message;
        public ?string $avatar;
        public ?string $emote;
        public ?string $action;
        public ?string $pose;
        public bool $speak;
        public ?string $voice;
        public ?string $format;
        public bool $hd;

        public function toXML() :?String {
            $writer .= "<avatar-message";
            $this->writeCredentails($writer);
            if(isset($this->avatar)) {
                $writer .= " avatar=\"" . $this->avatar . "\"";
            }
            if(isset($this->emote)) {
                $writer .= " emote=\"" . $this->emote . "\"";
            }
            if(isset($this->action)) {
                $writer .= " action=\"" . $this->action . "\"";
            }
            if(isset($this->pose)) {
                $writer .= " pose=\"" . $this->pose . "\"";
            }
            if(isset($this->format)) {
                $writer .= " format=\"" . $this->format . "\"";
            }
            if(isset($this->voice)) {
                $writer .= " voice=\"" . $this->voice . "\"";
            }
            if(isset($this->speak)) {
                $writer .= " speak=\"" . $this->speak . "\"";
            }
            if(isset($this->hd)) {
                $writer .= " hd=\"" . $this->hd . "\"";
            }
            $writer .= ">";

            if(isset($this->message)) {
                $writer .= "<message>";
                $writer .= Utils::escapeHTML($this->message);
                $writer .= "</message>";
            }
            $writer .= "</avatar-message>";
            return $writer;
        } 

    }
?>