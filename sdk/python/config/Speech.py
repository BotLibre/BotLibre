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
class Speech(Config):
    text: str
    voice: str
    mod: str
    def __init__(self):
        super().__init__()
        self.text = None
        self.voice = None
        self.mod = None
    
    
    def toXML(self):
        writer = Writer("<speech")
        self.writeCredentials(writer)
        if(self.text!=None):
            writer.append(" text=\"" + self.text + "\"")
        if(self.voice !=None):
            writer.append(" voice=\"" + self.voice + "\"")
        if(self.mod !=None):
            writer.append(" mod=\"" + self.mod + "\"")
        writer.append("/>")
        return writer