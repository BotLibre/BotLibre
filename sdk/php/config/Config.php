<?php
require_once('./sdk/SDKConnection.php');
require_once('./sdk/Credentials.php');
class Config
{
    public ?string $application;
    public ?string $domain;
    public ?string $user;
    public ?string $token;
    public ?string $instance;
    public ?string $type;

    public function addCredentials(SDKConnection $connection) :void
    {
        $this->application = $connection->getCredentials()->getApplicationId();
        if (!isset($this->user) && $connection->getUser() != null) {
            $this->user = $connection->getUser()->user;
            $this->token = $connection->getUser()->token;
        }
        if ($connection->getDomain() != null && $this->domain == null) {
			$this->domain = $connection->getDomain()->id;
		}
    }

    public function toXML() : string
    {
        return "<config/>";
    }

    public function parseXML($xml) : void
    {
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: ";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
        } else {
            print_r($xmlData);
        }
        //$this->user = $tempXML->user;
        $this->application = $xmlData->attributes()->application;
        $this->domain = $xmlData->attributes()->domain;
        $this->user = $xmlData->attributes()->user;
        $this->token = $xmlData->attributes()->token;
        $this->instance = $xmlData->attributes()->type;
    }
    public function writeCredentails(&$writer) : string
    {
        if ($this->user != null && strlen($this->user > 0)) {
            $writer .= " user=\"" . $this->user . "\"";
        }
        if (isset($this->token) && strlen($this->token) > 0) {
            $writer .= " token=\"" . $this->token . "\"";
        }
        if (isset($this->type) ) {
            $writer .= " type=\"" . $this->type . "\"";
        }
        if (isset($this->instance)) {
            $writer .= " instance=\"" . $this->instance . "\"";
        }
        if (isset($this->application)) {
            $writer .= " application=\"" . $this->application . "\"";
        }
        if (isset($this->domain) ) {
            $writer .= " domain=\"" . $this->domain . "\"";
        }
        return $writer;
    }
}
?>