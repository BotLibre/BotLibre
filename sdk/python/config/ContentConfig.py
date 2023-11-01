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
from util.Utils import Writer, Utils
class ContentConfig(Config):
    type: str
    name: str
    icon: str
    description: str
    def __init__(self):
        super().__init__()
        self.type = None
        self.name = None
        self.icon = None
        self.description = None
        
    
    def parseXML(self, xml: str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        
        self.type = root.attrib.get("type")
        self.name = root.attrib.get("name")
        self.icon = root.attrib.get("icon")
        
        if(root.find("description") != None):
            self.description = root.find("description").text

    def toXML(self):
        writer = Writer("<content")
        self.writeCredentials(writer)
        writer.append("/>")
        return writer