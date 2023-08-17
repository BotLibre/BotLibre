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
class ForumConfig extends WebMediumConfig
{
    public ?string $replyAccessMode;
    public ?string $postAccessMode;
    public ?string $posts;

    public function getType(): string
    {
        return "forum";
    }

    public function stats(): string
    {
        return $this->posts . " posts";
    }

    public function credentials(): ?WebMediumConfig
    {
        $config = new ForumConfig();
        $config->id = $this->id;
        return $config;
    }

    public function toXML() : string
    {
        $writer = "";
        $writer .= "<forum";
        if (isset($this->replyAccessMode)) {
            $writer .= " replyAccessMode=\"" . $this->replyAccessMode . "\"";
        }
        if (isset($this->postAccessMode)) {
            $writer .= " postAccessMode=\"" . $this->postAccessMode . "\"";
        }
        $this->writeXML($writer);
        $writer .= "</forum>";
        return $writer;
    }

    public function parseXML($xml) :void
    {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }
        $this->replyAccessMode = $xmlData->attributes()->replyAccessMode;
        $this->postAccessMode = $xmlData->attributes()->postAccessMode;
        $this->posts = $xmlData->attributes()->posts;
    }
}


?>