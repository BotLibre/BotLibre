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

class AvatarMedia(Config):
    mediaId: str
    name: str
    type: str
    media: str
    emotions: str
    actions: str
    poses: str
    hd: bool
    talking: bool
    def __init__(self):
        super().__init__()
        self.mediaId = None
        self.name = None
        self.type = None
        self.media = None
        self.emotions = None
        self.actions = None
        self.poses = None
        self.hd = False
        self.talking = False
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.mediaId = root.attrib.get("mediaId")
        self.name = root.attrib.get("name")
        self.type = root.attrib.get("type")
        self.media = root.attrib.get("media")
        self.emotions = root.attrib.get("emotions")
        self.actions = root.attrib.get("actions")
        self.poses = root.attrib.get("poses")
        self.hd = root.attrib.get("hd")
        self.talking = root.attrib.get("talking")
        
    def toXML(self):
        writer = Writer("<avatar-media")
        self.writeCredentials(writer)
        if(self.mediaId!=None):
            writer.append(" mediaId=\"" + self.mediaId + "\"")
        if(self.name !=None):
            writer.append(" name=\"" + self.name + "\"")
        if(self.emotions !=None):
            writer.append(" emotions=\"" + self.emotions + "\"")
        if(self.actions !=None):
            writer.append(" actions=\"" + self.actions + "\"")
        if(self.poses !=None):
            writer.append(" poses=\"" + self.poses + "\"")
        
        writer.append(" hd=\"true\"") if self.hd==True else writer.append(" hd=\"false\"")
        writer.append(" talking=\"true\"") if self.talking==True else writer.append(" talking=\"false\"")
        writer.append("/>")
        return writer

    def isVideo(self) -> bool:
        return self.type!=None and self.type.find("video")!=-1
    def isAudio(self) -> bool:
        return self.type !=None and self.type.find("audio")!=-1