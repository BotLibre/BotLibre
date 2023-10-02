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


class MediaConfig(Config):
    id: str
    name: str
    type: str
    file: str
    key: str
    
    def __init__(self):
        super().__init__()
        self.id = None
        self.name = None
        self.type = None
        self.file = None
        self.key = None

    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = int(root.attrib.get("id"))
        self.name = root.attrib.get("name")
        self.type = root.attrib.get("type")
        self.file = root.attrib.get("file")
        self.key = root.attrib.get("key")
        
    def toXML(self):
        writer = Writer("<media")
        self.writeCredentials(writer)
        if(self.id != 0):
            writer.append(" id=\"" + self.id + "\"")
        if(self.name != None):
            writer.append(" name=\"" + self.name + "\"")
        if(self.type != None):
            writer.append(" type=\"" + self.type + "\"")
        if(self.file != None):
            writer.append(" file=\"" + self.file + "\"")
        if(self.key != None):
            writer.append(" key=\"" + self.key + "\"")
            
        writer.append("/>")
        return writer