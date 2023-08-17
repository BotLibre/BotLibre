<?php
class ScriptConfig extends WebMediumConfig
{
    public ?string $language = "";
    public ?string $version = "";

    public function getType(): string
    {
        return "script";
    }
    public function credentials(): WebMediumConfig
    {
        $config = new ScriptConfig();
        $config->id = $this->id;
        return $config;
    }

    public function toXML(): string
    {
        $writer = "";
        $writer .= "<script";
        if (isset($this->language)) {
            $writer .= " language=\"" . $this->language . "\"";
        }
        if (isset($this->version)) {
            $writer .= " version=\"" . $this->version . "\"";
        }
        $this->writeXML($writer);
        $writer .= "</script>";
        return $writer;
    }

    public function parseXML($xml): void
    {
        $xmlData = Utils::loadXML($xml);
        if ($xmlData === false) {
            return;
        }
        $this->language = $xmlData->attributes()->language;
        $this->version = $xmlData->attributes()->version;
    }

}
?>