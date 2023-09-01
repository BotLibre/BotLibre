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
class LearningConfig extends Config
{
    public ?string $learningMode;
    public ?string $learningRate;
    public ?string $correctionMode;
    public $enableComprehension;
    public $enableEmoting;
    public $enableEmotions;
    public $enableConsciousness;
    public $enableWiktionary;
    public $enableResponseMatch;
    public $learnGrammar;
    public $synthesizeResponse;
    public $fixFormulaCase;
    public $checkExactMatchFirst;
    public int $scriptTimeout;
    public int $responseMatchTimeout;
    public ?string $conversationMatchPercentage;
    public ?string $discussionMatchPercentage;

    public ?string $nlp;
    public ?string $language;
    public ?bool $disableFlag =false;
    public ?bool $reduceQuestions=false;
	public ?bool $trackCase=false;

    public function parseXML($xml): void
    {
        parent::parseXML($xml);
        $xmlData = Utils::loadXML($xml);
        if($xmlData===false) {
            return;
        }
        $this->learningMode = $xmlData->attributes()->learningMode;
        $this->learningRate = $xmlData->attributes()->correctionMode;
        $this->enableComprehension = $xmlData->attributes()->enableComprehension;
        $this->enableEmoting = $xmlData->attributes()->enableEmoting;
        $this->enableEmotions = $xmlData->attributes()->enableEmotions;
        $this->enableConsciousness = $xmlData->attributes()->enableConsciousness;
        $this->enableWiktionary = $xmlData->attributes()->enableWiktionary;
        $this->enableResponseMatch = $xmlData->attributes()->enableResponseMatch;
        $this->learnGrammar = $xmlData->attributes()->learnGrammar;
        $this->synthesizeResponse = $xmlData->attributes()->synthesizeResponse;
        $this->fixFormulaCase = $xmlData->attributes()->fixFormulaCase;
        $this->checkExactMatchFirst = $xmlData->attributes()->checkExactMatchFirst;
        $this->nlp = $xmlData->attributes()->nlp;
        $this->language = $xmlData->attributes()->language;
        $this->disableFlag= $xmlData->attributes()->disableFlag;
        $this->reduceQuestions= $xmlData->attributes()->reduceQuestions;
        $this->trackCase= $xmlData->attributes()->trackCase;


        $value = $xmlData->attributes()->scriptTimeout;
        if (isset($value) && strlen($value) > 0) {
            $this->scriptTimeout = (int) $value;
        }
        $value = $xmlData->attributes()->responseMatchTimeout;
        if (isset($value) && strlen($value) > 0) {
            $this->responseMatchTimeout = (int) $value;
        }
        $this->conversationMatchPercentage = $xmlData->attributes()->conversationMatchPercentage;
        $this->discussionMatchPercentage = $xmlData->attributes()->discussionMatchPercentage;
    }

    public function toXML(): string
    {
        $writer ="";
        $writer .= "<learning";
        $this->writeCredentails($writer);
        if (isset($this->learningMode)) {
            $writer .= " learningMode=\"" . $this->learningMode . "\"";
        }
        if (isset($this->learningMode)) {
            $writer .= " correctionMode=\"" . $this->correctionMode . "\"";
        }
        $writer .= " enableComprehension=\"" . $this->enableComprehension . "\"";
        $writer .= " enableEmoting=\"" . $this->enableEmoting . "\"";
        $writer .= " enableEmotions=\"" . $this->enableEmotions . "\"";
        $writer .= " enableConsciousness=\"" . $this->enableConsciousness . "\"";
        $writer .= " enableWiktionary=\"" . $this->enableWiktionary . "\"";
        $writer .= " enableResponseMatch=\"" . $this->enableResponseMatch . "\"";
        $writer .= " learnGrammar=\"" . $this->learnGrammar . "\"";
        $writer .= " synthesizeResponse=\"" . $this->synthesizeResponse . "\"";
        $writer .= " fixFormulaCase=\"" . $this->fixFormulaCase . "\"";
        $writer .= " checkExactMatchFirst=\"" . $this->checkExactMatchFirst . "\"";
        $writer .= " nlp=\"" . $this->nlp . "\"";
        $writer .= " language=\"" . $this->language . "\"";
        $writer .= " disableFlag=\"" . $this->disableFlag . "\"";
        $writer .= " reduceQuestions=\"" . $this->reduceQuestions . "\"";
        $writer .= " trackCase=\"" . $this->trackCase . "\"";
        if ($this->scriptTimeout != 0) {
            $writer .= " scriptTimeout=\"" . $this->scriptTimeout . "\"";
        }
        if (isset($this->responseMatchTimeout)) {
            $writer .= " responseMatchTimeout=\"" . $this->responseMatchTimeout . "\"";
        }
        if (isset($this->conversationMatchPercentage)) {
            $writer .= " conversationMatchPercentage=\"" . $this->conversationMatchPercentage . "\"";
        }
        if (isset($this->learningRate)) {
            $writer .= " learningRate=\"" . $this->learningRate . "\"";
        }
        $writer .= "/>";
        return $writer;
    }

}
?>