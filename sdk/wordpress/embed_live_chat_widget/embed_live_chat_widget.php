<?php 
/*Plugin Name: Bot Libre Live Chat
Description: Add live chat to your website from Bot Libre.
Version: 1.0
Author: Paphus Solutions
Author URI: https://www.botlibre.com
License: GPL2
License URI: https://www.gnu.org/licenses/gpl-2.0.html
*/

class Bot_Libre_Chat_Widget extends WP_Widget {

	/**
	 *	Set up widget id, name, options
	 */
	public function __construct() {
		parent::__construct(
         
        	//widget id
        	'bot_libre_chat_widget',
         
        	//widget name
        	__('Bot Libre Live Chat', 'botlibrechat' ),
         
        	//widget options
        	array (
            	'description' => __( 'Add live chat to your website from Bot Libre.', 'botlibrechat' )
        	)
         
    	);
	}

	/**
	 *	Display widget on page
	 */
	public function widget( $args, $instance ) {
		$user_id = $instance['user_id'];
		$user_pw = $instance['user_pw'];
		$user_tk = $instance['user_tk'];
		$app_id = $instance[ 'app_id' ];
    	$chat_id = $instance[ 'chat_id' ];
    	$style_sheet = $instance[ 'style_sheet' ];
    	$button_style = $instance[ 'button_style' ];
    	$location = $instance[ 'location' ];
    	$contact_info = $instance[ 'contact_info' ];
		$backlink = $instance[ 'backlink' ];
   	
?>
<link rel='stylesheet' href='https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css' type='text/css'>
<link rel='stylesheet' href='https://www.botlibre.com/css/<?php echo $button_style; ?>.css' type='text/css'>
<style>
#botplatformbox {} #botplatformboxbar {} #botplatformboxbarmax {} #botplatformboxmin {} #botplatformboxmax {} #botplatformboxclose {} .botplatformbubble-text {} .botplatformbox-input {}
</style>
<script type='text/javascript' src='https://www.botlibre.com/scripts/sdk.js'></script>
<script type='text/javascript'>
SDK.applicationId = "<?php echo $app_id; ?>";
SDK.lang = "en";
var sdk = new SDKConnection();
var livechat = new WebLiveChatListener();
livechat.sdk = sdk;
livechat.instance = "<?php echo $chat_id; ?>";
livechat.prefix = "botplatformchat";
livechat.caption = "Chat Now";
livechat.chatLogType = "log";
livechat.boxLocation = "<?php echo $location; ?>";
livechat.color = "#009900";
livechat.background = "#fff";
livechat.css = "https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css";
livechat.version = 6.0;
livechat.bubble = false;
livechat.backlink = "<?php echo $backlink; ?>";
livechat.promptContactInfo = "<?php echo $contact_info; ?>";
livechat.chatLog = true;
livechat.showMenubar = true;
livechat.showBoxmax = true;
livechat.showSendImage = true;
livechat.emailChatLog = true;
livechat.online = true;
livechat.linkUsers = false;
livechat.chatroom = true;
livechat.popupURL = "https://www.botlibre.com/livechat?embedded=true&id=<?php echo $chat_id; ?>&chat=true&bubble=false&application=<?php echo $app_id; ?>&loginBanner=false&facebookLogin=false&chatLog=true&menubar=true&sendImage=true&background=%23fff&prompt=You+say&send=&css=https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css&language=en&translate=en";
livechat.createBox();
</script>
<?php
	}

	/**
	 *	Display form on widget admin page
	 */
	public function form( $instance ) {
		$defaults = array(
			'user_id' => '',
			'user_pw' => '',
			'user_tk' => '',
        	'app_id' => '0',
        	'chat_id' => '0',
        	'style_sheet' => 'chatroom',
        	'button_style' => 'blue_round_button',
        	'location' => 'bottom-right',
        	'contact_info' => false,
			'backlink' => false,
			'signedin' => false
			
    	);
    	$user_id = $instance['user_id'];
		$user_pw = $instance['user_pw'];
		$user_tk = $instance['user_tk'];
    	$app_id = $instance[ 'app_id' ];
    	$chat_id = $instance[ 'chat_id' ];
    	$style_sheet = $instance[ 'style_sheet' ];
    	$button_style = $instance[ 'button_style' ];
    	$location = $instance[ 'location' ];
    	$contact_info = $instance[ 'contact_info' ];
    	$backlink = $instance[ 'backlink' ];
    	$signedin = $instance[ 'signedin' ];
    	
    	if(isset($_POST['bl_signin'])){
 
    		$instance[ 'user_id' ] = $user_id = $_POST[ 'bl_username' ];
    		$bl_pw = $_POST['bl_pw'];
    		
			$bot_libre_url = 'https://www.botlibre.com/rest/api/check-user';
			$request_xml = '<user user=\''.$user_id.'\' password = \''.$bl_pw.'\'></user>';
			$response = wp_remote_post( $bot_libre_url, array(
				'method' => 'POST',
				'timeout' => 45,
				'redirection' => 5,
				'httpversion' => '1.0',
				'blocking' => true,
				'headers' => array( 'Content-Type' => 'application/xml'),
				'body' => $request_xml,
				'cookies' => array()
	    		)
			);
			
			if(substr($response['body'], 0, 5) == "<?xml") {
				if ( !is_wp_error ($response) ) {
		        	$bl_user = new SimpleXMLElement($response['body']);
					$user_tk = $instance[ 'user_tk' ] = $bl_user['token'];
                    $app_id = $instance['app_id'] = $bl_user['applicationId'];
					$signedin = $instance[ 'signedin' ] = true;		        	
		    	} 
			} else {
				$signedin = $instance[ 'signedin' ] = false;
				$user_id = $instance[ 'user_id' ] = "";
				$user_tk = $instance[ 'user_tk' ] = "";
			}
		} else if(isset($_POST['bl_signout'])){		
 			$signedin = $instance[ 'signedin' ] = false;
 			$user_id = $instance[ 'user_id' ] = "";
			$user_tk = $instance[ 'user_tk' ] = "";
 		}
?>	
		<label for="<?php echo $this->get_field_id( 'user_id' ); ?>">Username:</label>  
        <input class="widefat" type="hidden" id="<?php echo $this->get_field_id( 'user_id' ); ?>" name="<?php echo $this->get_field_name( 'user_id' ); ?>" value="<?php echo esc_attr( $user_id ); ?>">
		<input class="widefat" <?php echo ($signedin == true ? 'readonly' : '') ?> type="text" id="<?php echo $this->get_field_id( 'user_id' ); ?>" name="bl_username" value="<?php echo esc_attr( $user_id ); ?>">
		
        <?php if ($signedin == false)  {
        	echo '<label for="<?php echo $this->get_field_id( \'user_pw\' ); ?>">Password:</label>';
        } ?>
        <input class="widefat" <?php echo ($signedin == true ? 'type = "hidden"' : 'type="password"')  ?> id="<?php echo $this->get_field_id( 'user_pw' ); ?>" name="bl_pw" value="<?php echo esc_attr( $user_pw ); ?>">
        <input class="widefat" type="hidden" id="<?php echo $this->get_field_id( 'user_tk' ); ?>" name="<?php echo $this->get_field_name( 'user_tk' ); ?>" value="<?php echo esc_attr( $user_tk ); ?>">
        
        <?php if ($signedin == false) { 
        	echo '<button type="submit" name="bl_signin" value="bl_signin">Sign In</button>';
        	echo '<button formaction="https://www.botlibre.com/login?sign-up=true&affiliate=wordpress">Sign Up</button>';
        } else {
        	echo '<button type="submit" name="bl_signout" value="bl_signout">Sign Out</button>';
        } ?>
        <br>

        <input class="widefat" type="hidden" id="<?php echo $this->get_field_id( 'app_id' ); ?>" name="<?php echo $this->get_field_name( 'app_id' ); ?>" value="<?php echo esc_attr( $app_id ); ?>">
    	
    	<p>
		Create a <a href = "https://www.botlibre.com/browse?browse-type=Bot&create=true&affiliate=wordpress" target="_blank">bot</a>
		<p>
		
    	<label for="<?php echo $this->get_field_id( 'chat_id' ); ?>">Live Chat Select:</label>
        <select class="widefat" id="<?php echo $this->get_field_id( 'chat_id' ); ?>" name="<?php echo $this->get_field_name( 'chat_id' ); ?>" value="<?php echo esc_attr( $chat_id ); ?>">
    		<option value = "default">Select Live Chat</option>
<?php

		$bot_libre_url = 'https://www.botlibre.com/rest/api/get-channels';
		$request_xml = '<browse application=\''.$app_id.'\' user=\''.$user_id.'\' token = \''.$user_tk.'\' typeFilter=\'Personal\' filterPrivate = \'false\'></browse>';
		$response = wp_remote_post( $bot_libre_url, array(
			'method' => 'POST',
			'timeout' => 45,
			'redirection' => 5,
			'httpversion' => '1.0',
			'blocking' => true,
			'headers' => array( 'Content-Type' => 'application/xml'),
			'body' => $request_xml,
			'cookies' => array()
    		)
		);
		
		if(substr($response['body'], 0, 5) == "<?xml") {
			if ( !is_wp_error ($response) ) {
				$signedin = $instance[ 'signedin' ] = true;
	        	$channel_configs = new SimpleXMLElement($response['body']);
				
	        	foreach ($channel_configs->channel as $channel) {
	        		echo '<option ';
	        		if($channel['id'] == $chat_id) {
	        			echo 'selected ';
	        		}
	        		echo 'value = \''.$channel['id'].'\'>'.$channel['name'].'</option>';
	        	}
	    	}
		} else {
			if($user_tk != '') {
				echo '<p>Token has expired, please sign in again.</p>';
				$signedin = $instance[ 'signedin' ] = false;
				$user_id = $instance[ 'user_id' ] = "";
				$user_tk = $instance[ 'user_tk' ] = "";
				echo '<button type="submit" name="bl_signout" value="bl_signout">OK</button>';
			}
		}
?>
		</select>
			
    	<label for="<?php echo $this->get_field_id( 'style_sheet' ); ?>">Style Sheet:</label>
        <select class="widefat" id="<?php echo $this->get_field_id( 'style_sheet' ); ?>" name="<?php echo $this->get_field_name( 'style_sheet' ); ?>" value="<?php echo esc_attr( $style_sheet ); ?>">
    		<option value = "chatlog" <?php echo ($style_sheet == 'chat_log' ? 'selected' : '') ?> >Chat Log</option>
    		<option value = "social_chat" <?php echo ($style_sheet == 'social_chat' ? 'selected' : '') ?> >Social Chat</option>
    		<option value = "chatroom" <?php echo ($style_sheet == 'chatroom' ? 'selected' : '') ?> >Chat Room</option>
    		<option value = "blue_chat" <?php echo ($style_sheet == 'blue_chat' ? 'selected' : '') ?> >Blue Chat</option>
    		<option value = "pink_chat" <?php echo ($style_sheet == 'pink_chat' ? 'selected' : '') ?> >Pink Chat</option>
    	</select>

    	<label for="<?php echo $this->get_field_id( 'button_style' ); ?>">Button Style:</label>
        <select class="widefat" id="<?php echo $this->get_field_id( 'button_style' ); ?>" name="<?php echo $this->get_field_name( 'button_style' ); ?>" value="<?php echo esc_attr( $button_style ); ?>">
    		<option value = "blue_round_button" <?php echo ($button_style == 'blue_round_button' ? 'selected' : '') ?> >Blue Round Button</option>
    		<option value = "red_round_button" <?php echo ($button_style == 'red_round_button' ? 'selected' : '') ?> >Red Round Button</option>
    		<option value = "green_round_button" <?php echo ($button_style == 'green_round_button' ? 'selected' : '') ?> >Green Round Button</option>
    		<option value = "blue_bot_button" <?php echo ($button_style == 'blue_bot_button' ? 'selected' : '') ?> >Blue Bot Button</option>
    		<option value = "red_bot_button" <?php echo ($button_style == 'red_bot_button' ? 'selected' : '') ?> >Red Bot Button</option>
    		<option value = "green_bot_button" <?php echo ($button_style == 'green_bot_button' ? 'selected' : '') ?> >Green Bot Button</option>
    		<option value = "purple_chat_button" <?php echo ($button_style == 'purple_chat_button' ? 'selected' : '') ?> >Purple Chat Button</option>
    		<option value = "red_chat_button" <?php echo ($button_style == 'red_chat_button' ? 'selected' : '') ?> >Red Chat Button</option>
    		<option value = "green_chat_button" <?php echo ($button_style == 'green_chat_button' ? 'selected' : '') ?> >Green Chat Button</option>
    		<option value = "square_chat_button" <?php echo ($button_style == 'square_chat_button' ? 'selected' : '') ?> >Square Chat Button</option>
    		<option value = "round_chat_button" <?php echo ($button_style == 'round_chat_button' ? 'selected' : '') ?> >Round Chat Button</option>
    	</select>
    	
    	<label for="<?php echo $this->get_field_id( 'location' ); ?>">Location:</label>
    	<select class="widefat" id="<?php echo $this->get_field_id( 'location' ); ?>" name="<?php echo $this->get_field_name( 'location' ); ?>" value="<?php echo esc_attr( $location ); ?>">
    		<option value = "bottom-right" <?php echo ($location == 'bottom-right' ? 'selected' : '') ?> >Bottom Right</option>
    		<option value = "bottom-left" <?php echo ($location == 'bottom-left' ? 'selected' : '') ?> >Bottom Left</option>
    		<option value = "top-right" <?php echo ($location == 'top-right' ? 'selected' : '') ?> >Top Right</option>
    		<option value = "top-left" <?php echo ($location == 'top-left' ? 'selected' : '') ?> >Top Left</option>
    	</select>
    	   	
    	<p><label for="<?php echo $this->get_field_id( 'contact_info' ); ?>">Ask For Contact Info:</label>
        <input class="widefat" type="checkbox" id="<?php echo $this->get_field_id( 'contact_info' ); ?>" name="<?php echo $this->get_field_name( 'contact_info' ); ?>" <?php echo ($contact_info == true ? 'checked' : '') ?> ></p>
    	
		<p><label for="<?php echo $this->get_field_id( 'backlink' ); ?>">Backlink to Bot Libre:</label>
        <input class="widefat" type="checkbox" id="<?php echo $this->get_field_id( 'backlink' ); ?>" name="<?php echo $this->get_field_name( 'backlink' ); ?>" <?php echo ($backlink == true ? 'checked' : '') ?> ></p>
    	
    	<input class="widefat" type="checkbox" id="<?php echo $this->get_field_id( 'signedin' ); ?>" name="<?php echo $this->get_field_name( 'signedin' ); ?>" <?php echo ($signedin == true ? 'checked' : '') ?> style="display:none;"></p>
    	
    	</p>
<?php
	}

	/**
	 *	Update widget settings
	 */
	public function update( $new_instance, $old_instance ) {
		$instance = $old_instance;
		$instance[ 'user_id' ] = strip_tags( $new_instance[ 'user_id' ] );
		$instance[ 'user_tk' ] = strip_tags( $new_instance[ 'user_tk' ] );
    	$instance[ 'app_id' ] = strip_tags( $new_instance[ 'app_id' ] );
    	$instance[ 'chat_id' ] = strip_tags( $new_instance[ 'chat_id' ] );
    	$instance[ 'style_sheet' ] = strip_tags( $new_instance[ 'style_sheet' ] );
    	$instance[ 'button_style' ] = strip_tags( $new_instance[ 'button_style' ] );
    	$instance[ 'location' ] = strip_tags( $new_instance[ 'location' ] );
    	$instance[ 'contact_info' ] = $new_instance[ 'contact_info' ];
		$instance[ 'backlink' ] = $new_instance[ 'backlink' ];
		$instance[ 'signedin' ] = $new_instance[ 'signedin' ];
    	return $instance;
	}
}
?>
<?php
function bot_libre_chat_register_widget() {
 
    register_widget('Bot_Libre_Chat_Widget');
 
}
add_action( 'widgets_init', 'bot_libre_chat_register_widget' );
?>
