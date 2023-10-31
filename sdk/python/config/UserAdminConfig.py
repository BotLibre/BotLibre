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
class UserAdminConfig(Config):
    operation:str
    operationUser:str

    def __init__(self):
        super().__init__()
        self.operation = None
        self.operationUser = None
        
    def parseXML(self, xml: str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return 
        self.operation = root.attrib.get("operation")
        self.operationUser = root.attrib.get("operationUser")
        
    def toXML(self):
        writer = Writer("<user-admin")
        self.writeCredentials(writer)
        if(self.operation != None):
            writer.append(" operation=\"" + self.operation + "\"")
            
        if(self.operationUser!=None):
            writer.append(" operationUser=\"" + self.operationUser + "\"")
        writer.append("/>")
        return writer