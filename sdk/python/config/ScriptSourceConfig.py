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
from util.Utils import Utils, Writer


class ScriptSourceConfig(Config):
    id: str
    creationDate: str
    updateDate: str
    version:str
    versionName: str
    creator: str
    source: str
    
    
    def __init__(self):
        super().__init__()
        self.id = None
        self.creationDate = None
        self.updateDate = None
        self.version = None
        self.versionName = None
        self.creator = None
        self.source = None
        
    def credentials(self):
        config = ScriptSourceConfig()
        config.creator = self.creator
        return config
    
    def toXML(self) -> str:
        writer = Writer("<script-source")
        self.writeCredentials(writer)
        if(self.id != None):
            writer.append(" id=\"" + self.id + "\"")
        if(self.creationDate != None):
            writer.append(" creationDate=\"" + self.creationDate + "\"")
        if(self.updateDate != None):
            writer.append(" updateDate=\"" + self.updateDate + "\"")
        if(self.version != None):
            writer.append(" version=\"" + self.version + "\"")
        if(self.versionName != None):
            writer.append(" versionName=\"" + self.versionName + "\"")
        if(self.creator != None):
            writer.append(" creator=\"" + self.creator + "\"")
        writer.append(">")
        if(self.source!=None):
            writer.append("<source>")
            writer.append(Utils.escapeHTML(self.source))
            writer.append("</source>")
            
        writer.append("</script-source>")
        return writer
    
    def parseXML(self, xml: str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = root.attrib.get("id")
        self.creationDate = root.attrib.get("creationDate")
        self.updateDate = root.attrib.get("updateDate")
        self.version = root.attrib.get("version")
        self.versionName = root.attrib.get("versionName")
        self.creator = root.attrib.get("creator")
        
        if(root.find("source")!=None):
            self.source = root.find("source").text
            
    
    def getNextVersion(self) -> str:
        if(self.source != None):
            return "0.1"
        version = self.source
        index = version.rfind(".")
        if index != -1:
            major = version[:index]
            minor = self.version[index + 1:]
            try:
                value = int(minor)
                self.version = f"{major}.{value + 1}"
            except Exception as e:
                pass
        return self.version