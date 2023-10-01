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
class AvatarMessage(Config):
    message: str
    avatar: str
    emote: str
    action: str
    pose: str
    speak: bool
    voice: str
    format: str
    hd: bool
    
    def __init__(self):
        super().__init__()
        self.message = None
        self.avatar = None
        self.emote = None
        self.action = None
        self.pose = None
        self.speak = False
        self.voice = None
        self.format = None
        self.hd = False
        
        
    def toXML(self):
        writer = Writer("<avatar-message")
        writer = self.writeCredentials(writer)
        if(self.avatar != None):
            writer.append(" avatar=\"" + self.avatar + "\"")
        
        if(self.emote != None):
            writer.append(" emote=\"" + self.emote + "\"")
            
        if(self.action != None):
            writer.append(" action=\"" + self.action + "\"")
        
        if(self.pose != None):
            writer.append(" pose=\"" + self.pose + "\"")
        
        if(self.format != None):
            writer.append(" format=\"" + self.format + "\"")
        
        if(self.voice != None):
            writer.append(" voice=\"" + self.voice + "\"")
        
        if(self.speak != False):
            writer.append(" speak=\"true\"")
            
        if(self.hd != False):
            writer.append(" hd=\"true\"")
            
        writer.append(">")
        
        if(self.message): 
            writer.append("<message>")
            writer.append(Utils.escapeHTML(self.message))
            writer.append("</message>")
            
        writer.append("</avatar-message>")
        return writer