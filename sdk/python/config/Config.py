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
from sdk import SDKConnection
from util.Utils import Writer
from util import Utils

class Config:
    def __init__(self):
        self.application = ""
        self.domain = ""
        self.user = ""
        self.token = ""
        self.instance = ""
        self.type = ""

    def addCredentials(self, connection: SDKConnection):
        self.application = connection.getCredentials().getApplicationId()
        if self.user == None and connection.getUser() != None:
            self.user = connection.getUser().user
            self.token = connection.getUser().token

        if connection.getDomain() != None and self.domain == None:
            self.domain = connection.getDomain().id

    def toXML():
        return "<config/>"

    def parseXML(self, xml):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.application = root.attrib.get("application")
        self.domain = root.attrib.get("domain")
        self.user = root.attrib.get("user")
        self.token = root.attrib.get("token")
        self.instance = root.attrib.get("instance")

    def writeCredentials(self, writer: Writer):
        if(self.user != None):
            writer.append(" user=\"" + self.user + "\"")
        if(self.token != None):
            writer.append(" token=\"" + self.token + "\"")
        if(self.type != None):
            writer.append(" type=\"" + self.type + "\"")
        if(self.instance != None):
            writer.append(" instance=\"" + self.instance + "\"")
        if(self.application != None):
            writer.append(" application=\"" + self.application + "\"")
        if(self.domain != None):
            writer.append(" domain=\"" + self.domain + "\"")
        return writer
