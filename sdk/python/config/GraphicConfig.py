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
class GraphicConfig(WebMediumConfig):
    media: str
    fileName: str
    fileType: str
    
    def __init__(self):
        super().__init__()
        self.media = None
        self.fileName = None
        self.fileType = None
        
    def getType(self):
        return "graphic"
    
    def credentials(self) -> WebMediumConfig:
        config = GraphicConfig()
        config.id = self.id
        return config
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.media = root.attrib.get("media")
        self.fileName = root.attrib.get("fileName")
        self.fileType = root.attrib.get("fileType")
        
    def toXML(self):
        writer = Writer("<graphic")
        if(self.media != None and self.media != ""):
            writer.append(" media=\"" + self.media + "\"")
        if(self.fileName != None and self.fileName != ""):
            writer.append(" fileName=\"" + self.fileName + "\"")
        if(self.fileType != None and self.fileType != ""):
            writer.append(" fileType=\"" + self.fileType + "\"")
        self.writeXML(writer)
        writer.append("</graphic>")
        return writer


    def isVideo(self) -> bool:
        return self.fileType != None and self.fileType.find("video") != -1
    
    def isAudio(self) -> bool:
        return self.fileType != None and self.fileType.find("audio") != -1