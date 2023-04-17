<?php
class Utils {
    /**
	 * Replace reserved HTML character with their HTML escape codes.
	 */
	public static function escapeHTML($html) : String{
		return str_replace(array("&","<",">","\"","`","'"),array("&amp;","&lt;","&gt;","&quot;","&#96;","&#39;"),$html);
	}
}

?>