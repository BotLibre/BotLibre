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
require_once('./util/Utils.php');
    class ForumPostConfig extends Config {
        public ?String $id;
        public ?string $password;
        public ?String $topic;
        public ?String $summary;
        public ?String $details;
        public ?String $detailsText;
        public ?String $forum;
        public ?String $tags;
        public int $thumbsUp;
        public int $thumbsDown;
        public ?String $stars;
        public $isAdmin;
        public $isFlagged;
        public ?String $flaggedReason;
        public $isFeatured;
        public ?String $creator;
        public ?String $creationDate;
        public ?String $views;
        public ?String $dailyViews;
        public ?String $weeklyViews;
        public ?String $monthlyViews;
        public ?String $replyCount;
        public ?String $parent;
        public ?String $avatar;

        public $replies = array();


        public function toXML() : String {
            $writer = "<";
            $this->writeXML($writer);
            return $writer;
        }

        public function writeXML(&$writer) : void {
            $writer .= "forum-post";
            $this->writeCredentails($writer);
            if(isset($this->password)) {
                $writer .= " password=\"" . $this->password . "\"";
            }
            if(isset($this->id)) {
                $writer .= " id=\"" . $this->id . "\"";
            }
            if(isset($this->parent)) {
                $writer .= " parent=\"" . $this->parent . "\"";
            }
            if (isset($this->forum)) {
                $writer .= " forum=\"" . $this->forum . "\"";
            }
            if (isset($this->isFeatured)) {
                $writer .= " isFeatured=\"true\"";
            }
            if (isset($this->stars) && $this->stars !== "") {
                $writer .= " stars=\"" . $this->stars . "\"";
            }
            $writer .= ">";
            if(isset($this->topic)){
                $writer .= "<topic>";
                $writer .= Utils::escapeHTML($this->topic);
                $writer .= "</topic>";
            }
            if(isset($this->details)) {
                $writer .= "<details>";
                $writer .= Utils::escapeHTML($this->details);
                $writer .= "</details>";
            }
            if(isset($this->tags)) {
                $writer .= "<tags>";
                $writer .= Utils::escapeHTML($this->tags);
                $writer .= "</tags>";
            }
            if(isset($this->flaggedReason)) {
                $writer .= "<flaggedReason>";
                $writer .= Utils::escapeHTML($this->flaggedReason);
                $writer .= "</flaggedReason>";
            }
            $writer .= "</forum-post>";
        }

        public function parseXML($xml) : void {
            $xmlData = Utils::loadXML($xml);
            if ($xmlData === false) {
                return;
            }
            //Attributes
            $this->parent = $xmlData->attributes()->parent;
            $this->forum = $xmlData->attributes()->forum;
            $this->views = $xmlData->attributes()->views;
            $this->dailyViews = $xmlData->attributes()->dailyViews;
            $this->weeklyViews = $xmlData->attributes()->weeklyViews;
            $this->monthlyViews = $xmlData->attributes()->monthlyViews;
            $this->isAdmin = $xmlData->attributes()->isAdmin;
            $this->replyCount = $xmlData->attributes()->replyCount;
            $this->isFlagged = $xmlData->attributes()->isFlagged;
            $this->isFeatured = $xmlData->attributes()->isFeatured;
            $this->creator = $xmlData->attributes()->creator;
            $this->creationDate = $xmlData->attributes()->creationDate;
            if(isset($xmlData->attributes()->thumbsUp) && strlen(trim($xmlData->attributes()->thumbsUp)) > 0) {
                $this->thumbsUp = (int) $xmlData->attributes()->thumbsUp;
            }
            if(isset($xmlData->attributes()->thumbsDown) && strlen(trim($xmlData->attributes()->thumbsDown)) > 0) {
                $this->thumbsDown = (int) $xmlData->attributes()->thumbsDown;
            }
            if(isset($xmlData->attributes()->stars) && strlen(trim($xmlData->attributes()->stars)) > 0) {
                $this->stars = $xmlData->attributes()->stars;
            }
            //Tags
            if(isset($xmlData->summary)) {
                $this->summary = $xmlData->summary;
            }
            if(isset($xmlData->details)) {
                $this->details = $xmlData->details;
            }
            if(isset($xmlData->detailsText)) {
                $this->detailsText = $xmlData->detailsText;
            }
            if(isset($xmlData->topic)) {
                $this->topic = $xmlData->topic;
            }
            if(isset($xmlData->flaggedReason)) {
                $this->flaggedReason = $xmlData->flaggedReason;
            }
            if(isset($xmlData->avatar)) {
                $this->avatar = $xmlData->avatar;
            }
            if(isset($xmlData->replies)) {
                foreach($xmlData->replies as $reply){
                    echo $reply;
                    $config = new ForumPostConfig();
                    $config->parseXML($reply);
                    array_push($this->replies, $config);
                }
            }
        }
        public function credentials() : ForumPostConfig {
            $config = new ForumPostConfig();
            $config->id = $this->id;
            return $config;
        }

        // public function displayCreationDate() : String {
            // try {
            //     SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            //     Date date = formater.parse(creationDate);
            //     return Utils.displayTimestamp(date);
            // } catch (Exception exception) {
            //     return creationDate;
            // }
        // }

    }
?>