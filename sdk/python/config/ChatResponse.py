############################################################################
#
#  Copyright 2023 Paphus Solutions Inc.
#
#  Licensed under the Eclipse Public License, Version 1.0 (the "License") = None
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


class ChatResponse(Config):
    def __init__(self):
        super().__init__()
        self.message = None
        self.question = None
        self.log = None
        self.conversation = None
        self.emote = None
        self.action = None
        self.pose = None
        self.avatar = None
        self.avatar2 = None
        self.avatar3 = None
        self.avatar4 = None
        self.avatar5 = None
        self.avatarType = None
        self.avatarTalk = None
        self.avatarTalkType = None
        self.avatarAction = None
        self.avatarActionType = None
        self.avatarActionAudio = None
        self.avatarActionAudioType = None
        self.avatarAudio = None
        self.avatarAudioType = None
        self.avatarBackground = None
        self.speech = None
        self.command = None

    def isVideo(self) -> bool:
        return self.avatarType != None and self.avatarType.find("video") != -1

    def isVideoTalk(self):
        return self.avatarTalkType != None and self.avatarTalkType.find("video") != -1

    def parseXML(self, xml):
        root = Utils.loadXML(xml)
        if(root == None):
            return
    
        self.conversation = root.attrib.get("conversation")
        self.emote = root.attrib.get("emote")
        self.action = root.attrib.get("action")
        self.pose = root.attrib.get("pose")
        self.avatar = root.attrib.get("avatar")
        self.avatar2 = root.attrib.get("avatar2")
        self.avatar3 = root.attrib.get("avatar3")
        self.avatar4 = root.attrib.get("avatar4")
        self.avatar5 = root.attrib.get("avatar5")
        self.avatarType = root.attrib.get("avatarType")
        self.avatarTalk = root.attrib.get("avatarTalk")
        self.avatarTalkType = root.attrib.get("avatarTalkType")
        self.avatarAction = root.attrib.get("avatarAction")
        self.avatarActionType = root.attrib.get("avatarActionType")
        self.avatarActionAudio = root.attrib.get("avatarActionAudio")
        self.avatarActionAudioType = root.attrib.get("avatarActionAudioType")
        self.avatarAudio = root.attrib.get("avatarAudio")
        self.avatarAudioType = root.attrib.get("avatarAudioType")
        self.avatarBackground = root.attrib.get("avatarBackground")
        self.speech = root.attrib.get("speech")
        self.command = root.attrib.get("command")
        #tag name
        if(root.find("message") != None):
            self.message = root.find("message").text
        if(root.find("question")):
            self.question = root.find("question").text
