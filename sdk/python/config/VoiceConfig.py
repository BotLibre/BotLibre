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
class VoiceConfig(Config):
    
    voice: str
    nativeVoice: bool
    language: str
    pitch: str
    speechRate: str
    mod:str
    
    def __init__(self):
        super().__init__()
        self.voice = None
        self.nativeVoice = False
        self.language = None
        self.pitch = None
        self.speechRate = None
        self.mod = None
        
    def parseXML(self, xml: str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        
        self.voice = root.attrib.get("voice")
        self.nativeVoice = bool(root.attrib.get("nativeVoice"))
        self.language = root.attrib.get("language")
        self.pitch = root.attrib.get("pitch")
        self.speechRate = root.attrib.get("speechRate")
        self.mod = root.attrib.get("mod")
        
    def toXML(self):
        writer = Writer("<voice")
        self.writeCredentials(writer)
        if(self.voice != None):
            writer.append(" voice=\"" + self.voice + "\"")
        if(self.nativeVoice != None):
            writer.append(" nativeVoice=\"" + self.nativeVoice + "\"")
        if(self.language != None):
            writer.append(" language=\"" + self.language + "\"")
        if(self.pitch != None):
            writer.append(" pitch=\"" + self.pitch + "\"")
        if(self.speechRate != None):
            writer.append(" speechRate=\"" + self.speechRate + "\"")
        if(self.mod != None):
            writer.append(" mod=\"" + self.mod.lower() + "\"")
        
        writer.append("\>")
        return writer