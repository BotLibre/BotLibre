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
class ResponseSearchConfig(Config):
    responseType: str
    inputType: str
    filter: str
    sort: str
    duration: str
    restrict: str
    page: str
    
    def __init__(self):
        super().__init__()
        self.responseType = None
        self.inputType = None
        self.filter = None
        self.sort = None
        self.duration = None
        self.restrict = None
        self.page = None
    
    def toXML(self):
        writer = Writer("<response-search")
        self.writeCredentials(writer)
        writer.append(" responseType=\"" + self.responseType + "\"")
        if(self.inputType != None):
            writer.append(" inputType=\"" + self.inputType + "\"")
        if(self.filter != None):
            writer.append(" filter=\"" + self.filter + "\"")
        if(self.duration != None):
            writer.append(" duration=\"" + self.duration + "\"")
        if(self.sort != None):
            writer.append(" sort=\"" + self.sort + "\"")
        if(self.restrict != None):
            writer.append(" restrict=\"" + self.restrict + "\"")
        if(self.page != None):
            writer.append(" page=\"" + self.page + "\"")
        writer.append("/>")
        return writer