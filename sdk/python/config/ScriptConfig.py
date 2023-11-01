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
from util.Utils import Writer, Utils
class ScriptConfig(WebMediumConfig):
    language: str
    version:str
    
    def __init__(self):
        super().__init__()
        self.language = None
        self.version = None
        
    def getType() -> str:
        return "script"
    
    def credentials(self):
        config = ScriptConfig()
        config.id = self.id
        return config
    
    def toXML(self):
        writer = Writer("<script")
        if(self.language != None):
            writer.append(" language=\"" + self.language + "\"")
        if(self.version != None):
            writer.append(" version=\"" + self.version + "\"")  
            
        self.writeXML(writer)
        writer.append("</script>")
        return writer
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.language = root.attrib.get("language")
        self.version = root.attrib.get("version")
        
    def __str__(self) -> str:
        return self.name