<?php 

class ScriptSourceConfig extends Config {
    public String $id;
    public String $creationDate;
    public String $updateDate;
    public bool $version;
    public String $versionName;
    public String $creator;
    public String $source;


    public function credentials () : ScriptSourceConfig {
        $config = new ScriptSourceConfig();
		$config->creator = $this->creator;
		return $config;
    }

    public function toXML() : string {
        $writer = "";
        $writer .= "<script-source";
        $this->writeCredentails($writer);
        if (isset($this->id)) {
			$writer .= " id=\"" . $this->id . "\"";
		}
		if (isset($this->creationDate)) {
			$writer .= " creationDate=\"" . $this->creationDate . "\"";
		}
		if (isset($this->updateDate)) {
			$writer .= " updateDate=\"" . $this->updateDate . "\"";
		}
		if (isset($this->version)) {
			$writer .= " version=\"true\"";
		}
		if (isset($this->versionName)) {
			$writer .=" versionName=\"" . $this->versionName . "\"";
		}
		if (isset($this->creator)) {
			$writer .= " creator=\"" . $this->creator . "\"";
		}

        $writer .= ">";

        if(isset($this->source)) {
            $writer .= "<source>";
            $writer .= Utils::escapeHTML($this->source);
            $writer .= "</source>";
        }

        $writer .= "</script-source>";
        return $writer;
    }

    public function parseXML($xml) : void{
        $xmlData = simplexml_load_string($xml);
        if ($xmlData === false) {
            echo "Failed loading XML: ";
            foreach (libxml_get_errors() as $error) {
                echo "<br>", $error->message;
            }
        }
        // else {
        //     print_r($xmlData);
        // }
        $this->id = $xmlData->attributes()->id;
        $this->creationDate = $xmlData->attributes()->creationDate;
        $this->updateDate = $xmlData->attributes()->updateDate;
        $this->version = $xmlData->attributes()->version;
        $this->versionName = $xmlData->attributes()->versionName;
        $this->creator = $xmlData->attributes()->creator;


        if(isset($xmlData->source)) {
            $this->source = $xmlData->source;
        }

    }

    public function getNextVersion(): String {
        if(!isset($this->source) || $this->source === null) {
            return "0.1";
        }
        $version = $this->source;
        $index = strrpos($version, '.');

        if ($index !== false) {
            $major = substr($version, 0, $index);
            $minor = substr($version, $index + 1);
            try {
                $value = intval($minor);
                $version = $major . "." . ($value + 1); 
            } catch (Exception $ignore) {
            }
        }

        return $version;
    } 
}

?>