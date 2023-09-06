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
class ChatConfig(Config):
    def __init__(self):
        super().__init__()
        self.conversation = None
        self.correction = None
        self.offensive = None
        self.disconnect = None
        self.emote = None
        self.action = None
        self.message = None
        self.speak = None
        self.includeQuestion = None
        self.avatarHD = None
        self.avatarFormat = None
        self.avatar = None
        self.language = None
        self.voice = None
    
    def toXML(self):
        writer = Writer("<chat")
        self.writeCredentials(writer)
        if(self.conversation!=None):
            writer.append(" conversation=\"" + self.conversation + "\"")
        if(self.emote!=None):
            writer.append(" emote=\"" + self.emote + "\"")
        if(self.action!=None):
            writer.append(" action=\"" + self.action + "\"")
        if(self.correction!=None):
            writer.append(" correction=\"" + self.correction + "\"")
        if(self.offensive!=None):
            writer.append(" offensive=\"" + self.offensive + "\"")
        if(self.speak!=None):
            writer.append(" speak=\"" + self.speak + "\"")
        if(self.avatar!=None):
            writer.append(" avatar=\"" + self.avatar + "\"")
        if(self.avatarHD!=None):
            writer.append(" avatarHD=\"" + self.avatarHD + "\"")
        if(self.avatarFormat!=None):
            writer.append(" avatarFormat=\"" + self.avatarFormat + "\"")
        if(self.language!=None):
            writer.append(" language=\"" + self.language + "\"")
        if(self.voice!=None):
            writer.append(" voice=\"" + self.voice + "\"")
        if(self.includeQuestion!=None):
            writer.append(" includeQuestion=\"" + self.includeQuestion + "\"")
        if(self.disconnect!=None):
            writer.append(" disconnect=\"" + self.disconnect + "\"")
        writer.append(">")
        if(self.message):
            writer.append("<message>")
            writer.append(Utils.escapeHTML(self.message))
            writer.append("</message>")
        writer.append("</chat>")
        return writer