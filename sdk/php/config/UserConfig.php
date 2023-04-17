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
require_once('./sdk/SDKConnection.php');
require_once('Config.php');
require_once('./util/Utils.php');
class UserConfig extends Config
{
    public ?string $password;
    public ?string $newPassword;
    public ?string $hint;
    public ?string $name;
    public $showName;
    public ?string $email;
    public ?string $website;
    public ?string $bio;
    public $over18;
    public ?string $avatar;

    public ?string $connects;
    public ?string $bots;
    public ?string $posts;
    public ?string $messages;
    public ?string $forums;
    public ?string $scripts;
    public ?string $graphics;
    public ?string $avatars;
    public ?string $domains;
    public ?string $channels;

    public ?string $joined;
    public ?string $lastConnect;
    public ?string $type;
    public $isFlagged;
    public ?string $flaggedReason;



    public function displayJoined()
    {
        try {
            $date = new DateTime($this->joined);
            return $date->format('F Y');
        } catch (Exception $ignore) {
            return $this->joined;
        }
    }

    public function displayLastConnect()
    {
        try {
            $date = new DateTime($this->lastConnect);
            return $date->format('F Y');
        } catch (Exception $ignore) {
            return $this->joined;
        }
    }

    public function equals($object)
    {
        if ($this == $object) {
            return true;
        }
        if (!($object instanceof UserConfig)) {
            return false;
        }
        return true;
    }

    public function addCredentials(SDKConnection $connection): void
    {
        $this->application = $connection->getCredentials()->getApplicationId();
        if ($connection->getDomain() !== null) {
            $this->domain = $connection->getDomain()->id;
        }
    }


    public function parseXML($xml): void
    {
        try {
            $xmlData = simplexml_load_string($xml);
            if ($xmlData === false) {
                echo "Failed loading XML: ";
                foreach (libxml_get_errors() as $error) {
                    echo "<br>", $error->message;
                }
            } 
            // else {
            //     //Print
            //     print_r($xmlData);
            // }
        }catch (Exception $exception){
            echo "Error: " . $exception->getMessage();
        }
        $this->user = $xmlData->attributes()->user;
        $this->name = $xmlData->attributes()->name;
        $this->showName = $xmlData->attributes()->showName;
        $this->token = $xmlData->attributes()->token;
        $this->email = $xmlData->attributes()->email;
        $this->hint = $xmlData->attributes()->hint;
        $this->website = $xmlData->attributes()->website;
        $this->connects = $xmlData->attributes()->connects;
        $this->bots = $xmlData->attributes()->bots;
        $this->posts = $xmlData->attributes()->posts;
        $this->messages = $xmlData->attributes()->messages;
        $this->forums = $xmlData->attributes()->forums;
        $this->channels = $xmlData->attributes()->channels;
        $this->avatars = $xmlData->attributes()->avatars;
        $this->scripts = $xmlData->attributes()->scripts;
        $this->graphics = $xmlData->attributes()->graphics;
        $this->domains = $xmlData->attributes()->domains;
        $this->joined = $xmlData->attributes()->joined;
        $this->lastConnect = $xmlData->attributes()->lastConnect;
        $this->type = $xmlData->attributes()->type;
        $this->isFlagged = $xmlData->attributes()->isFlagged;


        //These are tag names
        $this->bio = $xmlData->bio;
        $this->avatar = $xmlData->avatar;
        $this->flaggedReason = $xmlData->flaggedReason;
    }

    public function toXML(): string
    {
        $writer = "";
        $writer .= "<user";
        $this->writeCredentails($writer);
        if (isset($this->password)) {
            $writer .= " password=\"" . $this->password . "\"";
        }
        if (isset($this->newPassword)) {
            $writer .= " newPassword=\"" . $this->newPassword . "\"";
        }
        if (isset($this->hint)) {
            $writer .= " hint=\"" . $this->hint . "\"";
        }
        if (isset($this->name)) {
            $writer .= " name=\"" . $this->name . "\"";
        }
        if (isset($this->showName)) {
            $writer .= " showName=\"" . $this->showName . "\"";
        }
        if (isset($this->email)) {
            $writer .= " email=\"" . $this->email . "\"";
        }
        if (isset($this->website)) {
            $writer .= " website=\"" . $this->website . "\"";
        }
        if (isset($this->over18)) {
            $writer .= " over18=\"" . $this->over18 . "\"";
        }
        $writer .= ">";
        if (isset($this->bio)) {
            $writer .= "<bio>";
            $writer .= Utils::escapeHTML($this->bio);
            $writer .= "</bio>";
        }
        $writer .= "</user>";

        return $writer;
    }
}
?>