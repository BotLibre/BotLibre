<?php 
require_once('WebMediumConfig.php');
class DomainConfig extends WebMediumConfig {
    public String $creationMode;

    public function getType() : String {
        return "domain";
    }

    public function credentials() : WebMediumConfig {
        $config = new DomainConfig();
        $config->id = $this->id;
        return $config;
    }

    public function toXML() : String {
        $writer = "<domain";
        if($this->creationMode !=null && !$this->creationMode === "") {
            $writer.= " creationMode=\"" . $this->creationMode . "\"";
        }
        $this->writeXML($writer);
        $writer.= "</domain>";
        return $writer;
    }
    public function parseXML($xml) : void {
        parent::parseXML($xml);
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: ";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
        } else {
            //Print
            print_r($xmlData);
        }
        $this->creationMode = $xmlData->attributes()->creationMode;
    }
}
?>