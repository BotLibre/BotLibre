<?php 
class ScriptConfig extends WebMediumConfig {
    public ?String $language = "";
    public ?String $version = "";
    
    public function getType() : String {
        return "script";
    }
    public function credentials() : WebMediumConfig {
		$config = new ScriptConfig();
		$config->id = $this->id;
		return $config;
	}

    public function toXML() : string {
        $writer = "";
        $writer .= "<script";
        if(isset($this->language)) {
            $writer .= " language=\"" . $this->language . "\"";
        }
        if(isset($this->version)) {
            $writer .= " version=\"" . $this->version . "\"";
        }
        $this->writeXML($writer);
        $writer .= "</script>";
        return $writer;
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
        $this->language = $xmlData->attributes()->language;
        $this->version = $xmlData->attributes()->version;
    }

}
?>