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
class InputConfig extends Config
{
    public ?string $id;
    public ?string $creationDate;
    public ?string $speaker;
    public ?string $target;
    public ?string $value;

    public function parseXML($xml): void
    {
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }
        $this->id = $xmlData->attributes()->id;
        $this->creationDate = $xmlData->attributes()->creationDate;
        $this->speaker = $xmlData->attributes()->speaker;
        $this->target = $xmlData->attributes()->target;

        if (isset($xmlData->value)) {
            $this->value = $xmlData->value;
        }

    }

    public function displayCreationDate(): string
    {
        try {
            $pattern = "D M d H:i:s e Y";
            $dateTime = DateTime::createFromFormat($pattern, $this->creationDate);
            if($dateTime !== false) {
                return $dateTime->format('Y-m-d H:i:s');
            }else {
                return "Failed to parse the date.";
            }
        } catch (Exception $exception) {
            return $this->creationDate;
        }
    }
}
?>