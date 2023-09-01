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
	public static function includeMessage($message, $img = null, $info = null, $sucess = true)
	{
		$debugComment = $message;
		if ($img != null) {
			$viewable_image = $img;
		}
		if ($info != null) {
			$debugInfo = $info;
		}
		if (!$sucess) {
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
		if (is_string($xml)) {
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
		if ($errors) {
			Utils::includeMessage("Couldn't read xml ->", null, $errors, false);
		}
		return $xmlData;
	}

	//Load an image from url
	// The following code loads an image from a URL and sends an API request using cURL.
	// This is used for testing purposes, such as testing the saveAvatarBackgroundImage API.
	public static function PostImageFromURL(string $url, $callBackFunction)
	{
		$imageContents = file_get_contents($url);
		$tempFilePath = tempnam(sys_get_temp_dir(), 'image_');
		file_put_contents($tempFilePath, $imageContents);
		$imageFile = new CURLFile($tempFilePath);
		//get image info
		$imageInfo = getimagesize($imageFile->getFilename());
		$imageFile->mime = $imageInfo['mime'];
		$imageFile->postname = "name";
		Utils::includeMessage("IMAGE INFO: ", null, $imageInfo);
		//Request
		$res = $callBackFunction($imageFile);
		//clean up
		unlink($tempFilePath);
		return $res;
	}
}

?>