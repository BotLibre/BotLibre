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
	public static function includeMessage($message, $img = null, $info = null)
	{
		$debugComment = $message;
		if ($img != null) {
			$viewable_image = $img;
		}
		if ($info != null) {
			$debugInfo = $info;
		}
		include "views/debug.php";
	}

	/*
	 *	Load xml
	 */
	public static function loadXML($xml)
	{
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
			Utils::includeMessage("Failed loading XML", null, $errorMessage);
		} else {
			Utils::includeMessage("Passed", null, $xmlData);
		}
		return $xmlData;
	}
}

?>