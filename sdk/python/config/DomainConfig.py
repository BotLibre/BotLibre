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
from config.WebMediumConfig import WebMediumConfig
from util.Utils import Writer
from util.Utils import Utils
class DomainConfig(WebMediumConfig):
    def __init__(self):
        self.creationMode
    
    def getType(self):
        return "domain"
    
    def credentials(self) -> WebMediumConfig:
        config = DomainConfig()
        config.id = self.id
        return config
    
    def toXML(self):
        writer = Writer("<domain")
        if(self.creationMode!=None):
            writer.append(" creationMode=\"" + self.creationMode + "\"")
        
        self.writerXML(writer)
        writer.append("</domain>")
        return writer
        
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        print(root)
        self.creationMode = root.attrib.get("creationMode")