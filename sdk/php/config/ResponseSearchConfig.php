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
class ResponseSearchConfig extends Config {
    public ?String $responseType;
    public ?String $inputType;
	public ?String $filter;
	public ?String $duration;
	public ?String $restrict;
	public ?String $page;

    public function toXML() : String {
        $writer = "";
        $writer .= "<response-search";
        $this->writeCredentails($writer);
        $writer .= " responseType=\"" . $this->responseType . "\"";
        if(isset($this->inputType)) {
            $writer .= " inputType=\"" . $this->inputType . "\"";
        }
        if(isset($this->filter)) {
            $writer .= " filter=\"" . $this->filter . "\"";
        }
        if(isset($this->duration)) {
            $writer .= " duration=\"" . $this->duration . "\"";
        }
        if(isset($this->restrict)) {
            $writer .= " restrict=\"" . $this->restrict . "\"";
        }
        if(isset($this->page)) {
            $writer .= " page=\"" . $this->page . "\"";
        }
        $writer .= "/>";
        return $writer;
    }
}
 ?>