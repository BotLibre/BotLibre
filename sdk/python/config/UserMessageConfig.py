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


class UserMessageConfig(Config):
    id: str
    creationDate: str
    owner: str
    creator: str
    target: str
    parent: str
    subject: str
    message:str 
    def __init__(self):
        super().__init__()
        self.id = None
        self.creationDate = None
        self.owner = None
        self.creator = None
        self.target = None
        self.parent = None
        self.subject = None
        self.message = None

    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = root.attrib.get("id")
        self.creationDate = root.attrib.get("creationDate")
        self.owner = root.attrib.get("owner")
        self.creator = root.attrib.get("creator")
        self.target = root.attrib.get("target")
        self.parent = root.attrib.get("parent")

        # tag name
        if(root.find("subject") != None):
            self.subject = root.find("subject")
        if(root.find("message") != None):
            self.message = root.find("message")

    def toXML(self):
        writer = Writer("<user-message")
        self.writeCredentials(writer)

        if (self.id != None):
            writer.append(" id=\"" + self.id + "\"")

        if (self.creationDate != None):
            writer.append(" creationDate=\"" + self.creationDate + "\"")

        if (self.owner != None):
            writer.append(" owner=\"" + self.owner + "\"")

        if (self.creator != None):
            writer.append(" creator=\"" + self.creator + "\"")

        if (self.target != None):
            writer.append(" target=\"" + self.target + "\"")

        if (self.parent != None):
            writer.append(" parent=\"" + self.parent + "\"")

        writer.append(">")
        
        if (self.subject != None):
            writer.append("<subject>")
            writer.append(Utils.escapeHTML(self.subject))
            writer.append("</subject>")

        if (self.message != None):
            writer.append("<message>")
            writer.append(Utils.escapeHTML(self.message))
            writer.append("</message>")

        writer.append("</user-message>")
        return writer
