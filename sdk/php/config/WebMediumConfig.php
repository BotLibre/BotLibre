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
require_once('Config.php');
require_once('./util/Utils.php');
abstract class WebMediumConfig extends Config
{
    public string $id;
    public string $name;
    public bool $isAdmin;
    public bool $isAdult;
    public bool $isPrivate;
    public bool $isHidden;
    public string $accessMode;
    public bool $isFlagged;
    public bool $isExternal;
    public bool $isPaphus;
    public bool $showAds = true;
    public string $forkAccessMode;
    public string $contentRating;
    public string $description;
    public string $details;
    public string $disclaimer;
    public string $website;
    public string $subdomain;
    public string $tags;
    public string $categories;
    public string $flaggedReason;
    public string $creator;
    public string $creationDate;
    public string $lastConnectedUser;
    public string $license;
    public string $avatar;
    public string $script;
    public string $graphic;
    public int $thumbsUp = 0;
    public int $thumbsDown = 0;
    public string $stars = "0";
    public string $connects;
    public string $dailyConnects;
    public string $weeklyConnects;
    public string $monthlyConnects;



    // abstract public function toXML() : String;
    abstract public function getType(): ?String;
    // abstract public function credentials(): WebMediumConfig;

    public function stats(): string
    {
        return "";
    }

    public function toString() : ?String {
		return $this->name;
	}

    public function getToken(): int
    {
        $token = 0;
        if (isset($this->token) && $this->token !== "") {
            $token = intval($this->token);
        }
        return $token;
    }
    public function writeXML(&$writer): void
    {
        $writer = $this->writeCredentails($writer);
        if ($this->id != null) {
            $writer .= " id=\"" . $this->id . "\"";
        }
        if ($this->name != null) {
            $writer .= " name=\"" . $this->name . "\"";
        }
        if ($this->isPrivate != null) {
            $writer .= " isPrivate=\"true\"";
        }
        if ($this->isHidden != null) {
            $writer .= " isHidden=\"true\"";
        }
        if ($this->accessMode != null && !$this->accessMode === "") {
            $writer .= " accessMode=\"" . $this->accessMode . "\"";
        }
        if ($this->contentRating != null && !$this->contentRating === "") {
            $writer .= " contentRating=\"" . $this->contentRating . "\"";
        }
        if ($this->forkAccessMode != null && !$this->forkAccessMode === "") {
            $writer .= " forkAccessMode=\"" . $this->forkAccessMode . "\"";
        }
        if ($this->stars != null && !$this->stars === "") {
            $writer .= " stars=\"" . $this->stars . "\"";
        }
        if ($this->isAdult != null) {
            $writer .= " isAdult=\"true\"";
        }
        if ($this->isFlagged != null) {
            $writer .= " isFlagged=\"true\"";
        }
        if ($this->isExternal != null) {
            $writer .= " isExternal=\"true\"";
        }
        if ($this->showAds != null) {
            $writer .= " showAds=\"true\"";
        }
        $writer .= ">";
        if ($this->description != null) {
            $writer .= "<description>";
            $writer .= Utils::escapeHTML($this->description);
            $writer .= "</description>";
        }
        if ($this->details != null) {
            $writer .= "<details>";
            $writer .= Utils::escapeHTML($this->details);
            $writer .= "</details>";
        }
        if ($this->disclaimer != null) {
            $writer .= "<disclaimer>";
            $writer .= Utils::escapeHTML($this->disclaimer);
            $writer .= "</disclaimer>";
        }
        if ($this->categories != null) {
            $writer .= "<categories>";
            $writer .= $this->categories;
            $writer .= "</categories>";
        }
        if ($this->tags != null) {
            $writer .= "<tags>";
            $writer .= $this->tags;
            $writer .= "</tags>";
        }
        if ($this->license != null) {
            $writer .= "<license>";
            $writer .= $this->license;
            $writer .= "</license>";
        }
        if ($this->website != null) {
            $writer .= "<website>";
            $writer .= $this->website;
            $writer .= "</website>";
        }
        if ($this->subdomain != null) {
            $writer .= "<subdomain>";
            $writer .= $this->subdomain;
            $writer .= "</subdomain>";
        }
        if ($this->flaggedReason != null) {
            $writer .= "<flaggedReason>";
            $writer .= Utils::escapeHTML($this->flaggedReason);
            $writer .= "</flaggedReason>";
        }
    }

    public function parseXML($xml): void
    {
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: ";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
            return;
        } 
        // else {
        //     print_r($xmlData);
        // }
        $this->id = $xmlData->attributes()->id;
        $this->name = $xmlData->attributes()->name;
        $this->creationDate = $xmlData->attributes()->creationDate;
        $this->isPrivate = $xmlData->attributes()->isPrivate;
        $this->isHidden = $xmlData->attributes()->isHidden;
        $this->accessMode = $this-$xmlData->attributes()->accessMode;
		$this->contentRating = $this-$xmlData->attributes()->contentRating;
		$this->forkAccessMode = $this-$xmlData->attributes()->forkAccessMode;
		$this->isAdmin = $this-$xmlData->attributes()->isAdmin;
		$this->isAdult = $this-$xmlData->attributes()->isAdult;
		$this->isFlagged = $this-$xmlData->attributes()->isFlagged;
		$this->isExternal = $this-$xmlData->attributes()->isExternal;
		$this->creator = $this-$xmlData->attributes()->creator;
		$this->creationDate = $this-$xmlData->attributes()->creationDate;
		$this->connects = $this-$xmlData->attributes()->connects;
		$this->dailyConnects = $this-$xmlData->attributes()->dailyConnects;
		$this->weeklyConnects = $this-$xmlData->attributes()->weeklyConnects;
		$this->showAds = $this-$xmlData->attributes()->showAds;
		$this->monthlyConnects = $this-$xmlData->attributes()->monthlyConnects;
        //Thumbs up and down
        $this->thumbsUp = $this-$xmlData->attributes()->thumbsUp;
        $this->thumbsDown = $this-$xmlData->attributes()->thumbsDown;

        //Tag Names
        $this->description = $xmlData->description;
        $this->details = $xmlData->details;
        $this->disclaimer = $xmlData->disclaimer;
        $this->categories = $xmlData->categories;
        $this->tags = $xmlData->tags;
        $this->flaggedReason = $xmlData->flaggedReason;
        $this->lastConnectedUser = $xmlData->lastConnectedUser;
        $this->license = $xmlData->license;
        $this->website = $xmlData->website;
        $this->subdomain = $xmlData->subdomain;
        $this->avatar = $xmlData->avatar;
    }
}


?>