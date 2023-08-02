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
        if (isset($this->id)) {
            $writer .= " id=\"" . $this->id . "\"";
        }
        if (isset($this->name)) {
            $writer .= " name=\"" . $this->name . "\"";
        }
        if (isset($this->isPrivate)) {
            $writer .= " isPrivate=\"true\"";
        }
        if (isset($this->isHidden)) {
            $writer .= " isHidden=\"true\"";
        }
        if (isset($this->accessMode)) {
            $writer .= " accessMode=\"" . $this->accessMode . "\"";
        }
        if (isset($this->contentRating)) {
            $writer .= " contentRating=\"" . $this->contentRating . "\"";
        }
        if (isset($this->forkAccessMode)) {
            $writer .= " forkAccessMode=\"" . $this->forkAccessMode . "\"";
        }
        if (isset($this->stars)) {
            $writer .= " stars=\"" . $this->stars . "\"";
        }
        if (isset($this->isAdult)) {
            $writer .= " isAdult=\"true\"";
        }
        if (isset($this->isFlagged)) {
            $writer .= " isFlagged=\"true\"";
        }
        if (isset($this->isExternal)) {
            $writer .= " isExternal=\"true\"";
        }
        if (isset($this->showAds)) {
            $writer .= " showAds=\"true\"";
        }
        $writer .= ">";
        if (isset($this->description)) {
            $writer .= "<description>";
            $writer .= Utils::escapeHTML($this->description);
            $writer .= "</description>";
        }
        if (isset($this->details)) {
            $writer .= "<details>";
            $writer .= Utils::escapeHTML($this->details);
            $writer .= "</details>";
        }
        if (isset($this->disclaimer)) {
            $writer .= "<disclaimer>";
            $writer .= Utils::escapeHTML($this->disclaimer);
            $writer .= "</disclaimer>";
        }
        if (isset($this->categories)) {
            $writer .= "<categories>";
            $writer .= $this->categories;
            $writer .= "</categories>";
        }
        if (isset($this->tags)) {
            $writer .= "<tags>";
            $writer .= $this->tags;
            $writer .= "</tags>";
        }
        if (isset($this->license)) {
            $writer .= "<license>";
            $writer .= $this->license;
            $writer .= "</license>";
        }
        if (isset($this->website)) {
            $writer .= "<website>";
            $writer .= $this->website;
            $writer .= "</website>";
        }
        if (isset($this->subdomain)) {
            $writer .= "<subdomain>";
            $writer .= $this->subdomain;
            $writer .= "</subdomain>";
        }
        if (isset($this->flaggedReason)) {
            $writer .= "<flaggedReason>";
            $writer .= Utils::escapeHTML($this->flaggedReason);
            $writer .= "</flaggedReason>";
        }
    }

    public function parseXML($xml): void
    {
        $xmlData = Utils::loadXML($xml);
        if($xmlData===false) {
            return;
        }
        $this->id = $xmlData->attributes()->id;
        $this->name = $xmlData->attributes()->name;
        $this->creationDate = $xmlData->attributes()->creationDate;
        $this->isPrivate = (bool) $xmlData->attributes()->isPrivate;
        $this->isHidden = (bool) $xmlData->attributes()->isHidden;
        $this->accessMode = $xmlData->attributes()->accessMode;
		$this->contentRating = $xmlData->attributes()->contentRating;
		$this->forkAccessMode = $xmlData->attributes()->forkAccessMode;
		$this->isAdmin =(bool) $xmlData->attributes()->isAdmin;
		$this->isAdult =(bool) $xmlData->attributes()->isAdult;
		$this->isFlagged = (bool)$xmlData->attributes()->isFlagged;
		$this->isExternal = (bool) $xmlData->attributes()->isExternal;
		$this->creator = $xmlData->attributes()->creator;
		$this->creationDate = $xmlData->attributes()->creationDate;
		$this->connects = $xmlData->attributes()->connects;
		$this->dailyConnects = $xmlData->attributes()->dailyConnects;
		$this->weeklyConnects = $xmlData->attributes()->weeklyConnects;
		$this->showAds = (bool)$xmlData->attributes()->showAds;
		$this->monthlyConnects = $xmlData->attributes()->monthlyConnects;
        //Thumbs up and down
        $this->thumbsUp = (int)$xmlData->attributes()->thumbsUp;
        $this->thumbsDown = (int)$xmlData->attributes()->thumbsDown;

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