<?php 
/*Plugin Name: Bot Libre Chatbot
Description: Add a chatbot to your website from Bot Libre.
Version: 1.0
Author: Paphus Solutions
Author URI: https://www.botlibre.com
License: GPL2
License URI: https://www.gnu.org/licenses/gpl-2.0.html
*/

class Bot_Libre_Widget extends WP_Widget {

	/**
	 *	Set up widget id, name, options
	 */
	public function __construct() {
		parent::__construct(
         
        	//widget id
        	'bot_libre_widget',
         
        	//widget name
        	__('Bot Libre Chatbot', 'botlibre' ),
         
        	//widget options
        	array (
            	'description' => __( 'Add a chatbot to your website from Bot Libre.', 'botlibre' )
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
    	$bot_id = $instance[ 'bot_id' ];
    	$style_sheet = $instance[ 'style_sheet' ];
    	$button_style = $instance[ 'button_style' ];
    	$location = $instance[ 'location' ];
    	$contact_info = $instance[ 'contact_info' ];
    	$show_avatar = $instance[ 'show_avatar' ];
		$backlink = $instance[ 'backlink' ];
   	
?>
<link rel='stylesheet' href='https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css' type='text/css'>
<link rel='stylesheet' href='https://www.botlibre.com/css/<?php echo $button_style; ?>.css' type='text/css'>
<style>
#botplatformbox {} #botplatformboxbar {} #botplatformboxbarmax {} #botplatformboxmin {} #botplatformboxmax {} #botplatformboxclose {} #botplatformbubble-text {} #botplatformbox-input {}
</style>
<script type='text/javascript' src='https://www.botlibre.com/scripts/sdk.js'></script>
<script type='text/javascript'>
SDK.applicationId = "<?php echo $app_id; ?>";
SDK.backlinkURL = "https://www.botlibre.com/";
var sdk = new SDKConnection();
var web = new WebChatbotListener();
web.connection = sdk;
web.instance = "<?php echo $bot_id; ?>";
web.prefix = "botplatform";
web.caption = "Chat Now";
web.boxLocation = "<?php echo $location; ?>";
web.color = "#009900";
web.background = "#fff";
web.css = "https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css";
web.version = 6.0;
web.bubble = true;
web.backlink = "<?php echo $backlink; ?>";
web.promptContactInfo = "<?php echo $contact_info; ?>";
web.showMenubar = true;
web.showBoxmax = true;
web.showSendImage = true;
web.showChooseLanguage = true;
web.nativeVoice = true;
web.nativeVoiceName = "";
web.avatar = "<?php echo $show_avatar; ?>";
web.chatLog = true;
web.popupURL = "https://www.botlibre.com/chat?&id=<?php echo $bot_id; ?>&embedded=true&avatar=<?php echo $show_avatar; ?>&chatLog=true&facebookLogin=false&application=<?php echo $app_id; ?>&bubble=true&menubar=true&chooseLanguage=true&sendImage=true&background=%23fff&prompt=You+say&send=Send&css=https://www.botlibre.com/css/<?php echo $style_sheet; ?>.css";
web.createBox();
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
        	'bot_id' => '0',
        	'style_sheet' => 'chatlog',
        	'button_style' => 'blue_round_button',
        	'location' => 'bottom-right',
        	'contact_info' => false,
        	'show_avatar' => true,
			'backlink' => false,
			'signedin' => false
			
    	);
    	$user_id = $instance['user_id'];
		$user_pw = $instance['user_pw'];
		$user_tk = $instance['user_tk'];
    	$app_id = $instance[ 'app_id' ];
    	$bot_id = $instance[ 'bot_id' ];
    	$style_sheet = $instance[ 'style_sheet' ];
    	$button_style = $instance[ 'button_style' ];
    	$location = $instance[ 'location' ];
    	$contact_info = $instance[ 'contact_info' ];
    	$show_avatar = $instance[ 'show_avatar' ];
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
		
    	<label for="<?php echo $this->get_field_id( 'bot_id' ); ?>">Bot Select:</label>
        <select class="widefat" id="<?php echo $this->get_field_id( 'bot_id' ); ?>" name="<?php echo $this->get_field_name( 'bot_id' ); ?>" value="<?php echo esc_attr( $bot_id ); ?>">
    		<option value = "default">Select Bot</option>
<?php

		$bot_libre_url = 'https://www.botlibre.com/rest/api/get-all-instances';
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
	        	$instance_configs = new SimpleXMLElement($response['body']);
				
	        	foreach ($instance_configs->instance as $instance) {
	        		echo '<option '; 
	        		if($instance['id'] == $bot_id) {
	        			echo 'selected ';
	        		}
	        		echo 'value = \''.$instance['id'].'\'>'.$instance['name'].'</option>'; 
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
    	
    	<p><label for="<?php echo $this->get_field_id( 'show_avatar' ); ?>">Show Avatar:</label>
        <input class="widefat" type="checkbox" id="<?php echo $this->get_field_id( 'show_avatar' ); ?>" name="<?php echo $this->get_field_name( 'show_avatar' ); ?>" <?php echo ($show_avatar == true ? 'checked' : '') ?> ></p>
    	
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
    	$instance[ 'bot_id' ] = strip_tags( $new_instance[ 'bot_id' ] );
    	$instance[ 'style_sheet' ] = strip_tags( $new_instance[ 'style_sheet' ] );
    	$instance[ 'button_style' ] = strip_tags( $new_instance[ 'button_style' ] );
    	$instance[ 'location' ] = strip_tags( $new_instance[ 'location' ] );
    	$instance[ 'contact_info' ] = $new_instance[ 'contact_info' ];
    	$instance[ 'show_avatar' ] = $new_instance[ 'show_avatar' ];
		$instance[ 'backlink' ] = $new_instance[ 'backlink' ];
		$instance[ 'signedin' ] = $new_instance[ 'signedin' ];
    	return $instance;
	}
}
?>
<?php
function bot_libre_register_widget() {
 
    register_widget('Bot_Libre_Widget');
 
}
add_action( 'widgets_init', 'bot_libre_register_widget' );
?>
