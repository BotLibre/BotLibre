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
    class ForumConfig extends WebMediumConfig {
        public ?String $replyAccessMode;
        public ?String $postAccessMode;
        public ?String $posts;

        public function getType() : String {
            return "forum";
        }

        public function stats() : String {
            return $this->posts . " posts";
        }

        public function credentials(): ?WebMediumConfig {
            $config = new ForumConfig();
            $config->id = $this->id;
            return $config;
        }
    }

    
?>