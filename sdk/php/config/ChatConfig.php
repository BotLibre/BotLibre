<?php 
class ChatConfig extends Config{
    public string $conversation;
    public bool $correction;
    public bool $offensive;
    public bool $disconnect;
    public string $emote;
    public string $action;
    public string $message;
    public bool $speak;
	public bool $includeQuestion;
	public bool $avatarHD;
	public String $avatarFormat;
	public String $avatar;
	public String $language;
	public String $voice;
    public function toXML() : String {
        $writer = "<chat";
        $this->writeCredentails($writer);
        if (isset($this->conversation)) {
            $writer .= " conversation=\"" . $this->conversation . "\"";
        }
        if (isset($this->emote)) {
            $writer .=  " emote=\"" . $this->emote . "\"";
        }
        if (isset($this->action)) {
            $writer .=  " action=\"" . $this->action . "\"";
        }
        if (isset($this->correction)) {
            $writer .=  " correction=\"" . $this->correction . "\"";
        }
        if (isset($this->offensive)) {
            $writer .=  " offensive=\"" . $this->offensive . "\"";
        }
        if (isset($this->speak)) {
            $writer .=  " speak=\"" . $this->speak . "\"";
        }
        if (isset($this->avatar)) {
            $writer .=  " avatar=\"" . $this->avatar . "\"";
        }
        if (isset($this->avatarHD)) {
            $writer .=  " avatarHD=\"" . $this->avatarHD . "\"";
        }
        if (isset($this->avatarFormat)) {
            $writer .=  " avatarFormat=\"" . $this->avatarFormat . "\"";
        }
        if (isset($this->language)) {
            $writer .=  " language=\"" . $this->language . "\"";
        }
        if (isset($this->voice)) {
            $writer .=  " voice=\"" . $this->voice . "\"";
        }
        if (isset($this->includeQuestion)) {
            $writer .=  " includeQuestion=\"" . $this->includeQuestion . "\"";
        }
        if (isset($this->disconnect)) {
            $writer .=  " disconnect=\"" . $this->disconnect . "\"";
        }
        $writer .=  ">";
        if (isset($this->message)) {
            $writer .=  "<message>";
            $writer .=  Utils::escapeHTML($this->message);
            $writer .=  "</message>";
        }
        $writer .=  "</chat>";
        return $writer;
    }
}
?>