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

class AvatarConfig(WebMediumConfig):
    
    def __init__(self):
        super().__init__()
    
    def getType(self) -> str:
        return "avatar"
    
    def toXML(self):
        writer = Writer("<avatar")
        self.writeXML(writer)
        writer.append("</avatar>")
        return writer


    def parseXML(self, xml):
        super().parseXML(xml)        