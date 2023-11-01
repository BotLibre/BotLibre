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
import datetime
from util.Utils import Utils
class InputConfig(Config):
    id: str
    creationDate: str
    speaker: str
    target: str
    value: str
    
    def __init__(self):
        super().__init__()
        self.id = None
        self.creationDate = None
        self.speaker = None
        self.target = None
        self.value = None
        
    def parseXML(self, xml: str):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = root.attrib.get("id")
        self.creationDate = root.attrib.get("creationDate")
        self.speaker = root.attrib.get("speaker")
        self.target = root.attrib.get("target")
        
        if(root.attrib.get("value") != None):
            self.value = root.attrib.get("value")
            
    def displayCreationDate(self) -> str:
        try:
            date = datetime.strptime(self.joined, '%Y-%m-%d')  #Date string is in 'YYYY-MM-DD' format
            return date.strftime('%B %Y')  #Format as 'Month Year'
        except ValueError:
            return self.creationDate