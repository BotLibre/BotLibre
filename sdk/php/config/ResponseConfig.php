<?php
class ResponseConfig extends Config {
    public String $questionId;
	public String $responseId;
	public String $question;
	public String $response;
	public String $previous;
	public String $onRepeat;
	public String $label;
	public String $topic;
	public String $keywords;
	public String $required;
	public String $emotions;
	public String $actions;
	public String $poses;
	public $noRepeat;
	public $requirePrevious;
	public $requireTopic;
	public $flagged;
	public String $correctness;
	public String $command;

    // public function __construct () {

    // }

    public function toXML() : String {
		$writer .= "";
		$this->writeXML($writer);
		return $writer;
	}

    public function writeXML(&$writer) : void {
        $writer .= "<response";
        $this->writeCredentails($writer);
        if(isset($this->questionId)) {
            $writer .= " questionId=\"" . $this->questionId . "\"";
        }
        if(isset($this->responseId)) {
            $writer .= " responseId=\"" . $this->responseId . "\"";
        }
    }

    public function parseXML($xml) : void {
        
    }
}
?>