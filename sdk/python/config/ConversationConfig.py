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
from config.InputConfig import InputConfig
import datetime
from util.Utils import Utils, Writer
class ConversationConfig(Config):
    id: str
    creationDate: str
    type: str
    inputs = []
    
    def __init__(self):
        super().__init__()
        self.id = None
        self.creationDate = None
        self.type = None
        
    def parseXML(self, xml: str):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = root.attrib.get("id")
        self.creationDate = root.attrib.get("creationDate")
        self.type = root.attrib.get("type")
        
        for item in root:
            config = InputConfig()
            config.parseXML(item)
            self.inputs.append(config)

    def displayCreationDate(self):
        try:
            date = datetime.strptime(self.joined, '%Y-%m-%d')  #Date string is in 'YYYY-MM-DD' format
            return date.strftime('%B %Y')  #Format as 'Month Year'
        except ValueError:
            return self.creationDate