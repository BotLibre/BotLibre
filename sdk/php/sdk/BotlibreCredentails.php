<?php 
require_once('Credentials.php');
class BotlibreCredentails extends Credentials {
    public ?string $DOMAIN = "www.botlibre.com";
    public ?string $APP = "";

    public ?string $PATH = "/rest/api";

    public function __construct(string $applicationId){
        $this->host = $this->DOMAIN;
        $this->app = $this->APP; 
        $this->url = "https://" . $this->DOMAIN . $this->APP . $this->PATH;
        $this->applicationId = $applicationId;
    }
}
?>