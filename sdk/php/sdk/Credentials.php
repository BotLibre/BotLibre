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

 
/**
 * Credential used to establish a connection.
 * Requires the url, and an application id.
 * You can obtain your application id from your user details page on the hosting website.
 */
class Credentials
{
    public String $host = "";
    public String $app = "";
    public String $url = "";
    /**
     * Your application's unique identifier.
     * You can obtain your application id from your user details page on the hosting website.
     */
    public String $applicationId = "";

    /**
     * Creates a new credentials for the service host url, and the application id.
     */
    function Credentials($url, $applicationId)
    {
        $this->$url = $url;
        $this->$applicationId = $applicationId;
    }
    function getUrl()
    {
        return $this->url;
    }
    /**
     * Sets the server host name, i.e. www.paphuslivechat.com
     */
    function getHost()
    {
        return $this->host;
    }
    function getApp()
    {
        return $this->app;
    }
    /**
     * Sets the hosted service server url, i.e. http://www.paphuslivechat.com
     */
    function setUrl($url)
    {
        return $this->$url = $url;
    }
    /**
     * Sets the server host name, i.e. www.paphuslivechat.com
     */
    function setHost($host)
    {
        return $this->$host = $host;
    }
    /**
     * Sets an app url postfix, this is normally not required, i.e. "".
     */
    function setApp($app)
    {
        return $this->$app = $app;
    }
    /**
     * Returns your application's unique identifier.
     */
    function getApplicationId()
    {
        return $this->applicationId;
    }
    /**
     * Sets your application's unique identifier.
     * You can obtain your application id from your user details page on the hosting website.
     */
    function setApplicationId($applicationId)
    {
        $this->applicationId = $applicationId;
    }
}
?>