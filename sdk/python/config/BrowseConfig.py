############################################################################
#
#  Copyright 2023 Paphus Solutions Inc.
#
#  Licensed under the Eclipse Public License, Version 1.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.eclipse.org/legal/epl-v10.html
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
############################################################################

from config.Config import Config
from util.Utils import Writer

class BrowseConfig(Config):
    type: str = None
    typeFilter: str = None
    category: str = None
    tag: str = None
    filter: str = None
    userFilter: str = None
    sort: str = None
    restrict: str = None
    page: str = None
    contentRating: str = None
    
    def __init__(self):
        super().__init__()
    
    
    def toXML(self):
        writer = Writer("<browse")
        self.writeCredentials(writer)
        if(self.userFilter!=None):
            writer.append(" userFilter=\"" + self.userFilter + "\"")
        if(self.sort!=None):
            writer.append(" sort=\"" + self.sort + "\"")
        if(self.restrict!=None):
            writer.append(" restrict=\"" + self.restrict + "\"")
        if(self.category!=None):
            writer.append(" category=\"" + self.category + "\"")
        if(self.tag!=None):
            writer.append(" tag=\"" + self.tag + "\"")
        if(self.filter!=None):
            writer.append(" filter=\"" + self.filter + "\"")
        if(self.page!=None):
            writer.append(" page=\"" + self.page + "\"")
        if(self.contentRating!=None):
            writer.append(" contentRating=\"" + self.contentRating + "\"")
        writer.append("/>")
        return writer
    