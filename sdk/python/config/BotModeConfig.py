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
class BotModeConfig(Config):
    mode: str
    bot: str
    def __init__(self):
        self.mode = None
        self.bot = None
        super().__init__()
        
    def parseXML(self, xml: str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.mode = root.attrib.get("mode")
        self.bot = root.attrib.get("bot")
    
    def toXML(self):
        writer = Writer("<bot-mode")
        self.writeCredentials(writer)
        if(self.mode!= None):
            writer.append(" mode=\"" + self.mode + "\"")
        if(self.bot != None):
            writer.append(" bot=\"" + self.bot + "\"")
        writer.append("/>")
        return writer
        
        