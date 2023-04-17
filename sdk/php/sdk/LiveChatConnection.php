<?php
require_once("ChannelConfig.php");
class LiveChatConnection
{
    protected bool $debug = false;
    protected ?ChannelConfig $channel;
    protected ?UserCOnfig $user;
    protected ?Credentials $credentials;
    // protected ?WebSocketConnection $socket;
    // protected ?LiveChatListener $listener;
    protected bool $keepAlive = false;
    //protected Thread keepAliveThread; //Have to look up threads for php

    /**
     * Create a new connection with the application credentials and the listener.
     * The listener will be notified asynchronously of messages and events.
     */
	public function __construct(Credentials $credentials, LiveChatListener $listener) {
		$this->credentials = credentials;
		$this->listener = listener;
	}
}

?>