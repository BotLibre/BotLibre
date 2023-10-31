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
class TrainingConfig(Config):
    
    operation:str
    question: str
    response: str
    
    def __init__(self):
        super().__init__()
        self.operation = None
        self.question = None
        self.response = None
        
    def toXML(self):
        writer = Writer("<training")
        self.writeCredentials(writer)
        if(self.operation!=None):
            writer.append(" operation=\"" + self.operation + "\"")
        
        writer.append(">")
        if(self.question != None):
            writer.append("<question>")
            writer.append(Utils.escapeHTML(self.question))
            writer.append("</question>")
        
        if(self.response !=None):
            writer.append("<response>")
            writer.append(Utils.escapeHTML(self.response))
            writer.append("</response>")
        
        writer.append("</training>")
        return writer