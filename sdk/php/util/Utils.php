<?php
/******************************************************************************
 *
 *  Copyright 2023 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
class Utils
{
	/**
	 * Replace reserved HTML character with their HTML escape codes.
	 */
	public static function escapeHTML($html): string
	{
		return str_replace(array("&", "<", ">", "\"", "`", "'"), array("&amp;", "&lt;", "&gt;", "&quot;", "&#96;", "&#39;"), $html);
	}

	/*
	 *	Used for debugging
	 */
	public static function includeMessage($message, $img = null, $info = null, $sucess=true)
	{
		$debugComment = $message;
		if ($img != null) {
			$viewable_image = $img;
		}
		if ($info != null) {
			$debugInfo = $info;
		}
		if(!$sucess) {
			$borderColor = "border-color: red;";
		}
		include "views/debug.php";
	}

	/*
	 *	Load xml
	 */
	public static function loadXML($xml)
	{
		$XMLLogsOff = libxml_use_internal_errors(true);
		$xmlData = $xml;
		if(is_string($xml)) {
			$xmlData = simplexml_load_string($xml);
		}
		if ($xmlData === false) {
			$errorMessage = "";
			foreach (libxml_get_errors() as $error) {
				$errorMessage .= $error->message;
			}
			$errorMessage .= "<strong>Response: </strong>" . $xml;
			Utils::includeMessage("Couldn't read xml", null, $errorMessage, false);
		} else {
			Utils::includeMessage("Passed", null, $xmlData);
		}
		$errors = libxml_get_errors();
		libxml_clear_errors();
		libxml_use_internal_errors($XMLLogsOff);
		if($errors) {
			Utils::includeMessage("Couldn't read xml ->", null, $errors, false);
		}
		return $xmlData;
	}
}

?>