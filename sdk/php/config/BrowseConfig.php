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
class BrowseConfig extends Config {
    public ?String $type;
	public ?String $typeFilter;
	public ?String $category;
	public ?String $tag;
	public ?String $filter;
	public ?String $userFilter;
	public ?String $sort;
	public ?String $restrict;
	public ?String $page;
	public ?String $contentRating;

    public function toXML() : string {
        $writer = "";
        $writer .= "<browse";
        $this->writeCredentails($writer);
        if(isset($this->userFilter)) {
            $writer .= " userFilter=\"" . $this->userFilter . "\"";
        }
        if(isset($this->sort)) {
            $writer .= " sort=\"" . $this->sort . "\"";
        }
        if(isset($this->restrict)) {
            $writer .= " restrict=\"" . $this->restrict . "\"";
        }
        if(isset($this->category)) {
            $writer .= " category=\"" . $this->category . "\"";
        }
        if(isset($this->tag)) {
            $writer .= " tag=\"" . $this->tag . "\"";
        }
        if(isset($this->filter)) {
            $writer .= " filter=\"" . $this->filter . "\"";
        }
        if(isset($this->page)) {
            $writer .= " page=\"" . $this->page . "\"";
        }
        if(isset($this->contentRating)) {
            $writer .= " contentRating=\"" . $this->contentRating . "\"";
        }
        $writer .= "/>";
        return $writer;
    }
}
 ?>