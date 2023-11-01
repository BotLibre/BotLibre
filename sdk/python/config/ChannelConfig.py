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
from util.Utils import Writer, Utils
from config.WebMediumConfig import WebMediumConfig

class ChannelConfig(WebMediumConfig):
    type: str
    videoAccessMode: str
    audioAccessMode: str
    messages: str
    usersOnline: str
    adminsOnline: str
    
    def __init__(self):
        super().__init__()
        self.type = None
        self.videoAccessMode = None
        self.audioAccessMode = None
        self.messages = None
        self.usersOnline = None
        self.adminsOnline = None

    def getType(self):
        return "channel"
    
    def stats(self):
        return self.usersOnline + " users online, " + self.adminsOnline + " admins"
    
    def credentials(self) -> WebMediumConfig: 
        config = ChannelConfig()
        config.id = self.id
        return config

    def toXML(self):
        writer = Writer("<channel")
        if(self.type != None):
            writer.append(" type=\"" + self.type + "\"")
        if(self.videoAccessMode!=None):
            writer.append(" videoAccessMode=\"" + self.videoAccessMode + "\"")
        if(self.audioAccessMode!=None):
            writer.append(" audioAccessMode=\"" + self.audioAccessMode + "\"")
        self.writeXML(writer)
        writer.append("</channel>")
        return writer
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.type = root.attrib.get("type")
        self.videoAccessMode = root.attrib.get("videoAccessMode")
        self.audioAccessMode = root.attrib.get("audioAccessMode")
        self.messages = root.attrib.get("messages")
        self.usersOnline = root.attrib.get("usersOnline")
        self.adminsOnline = root.attrib.get("adminsOnline")