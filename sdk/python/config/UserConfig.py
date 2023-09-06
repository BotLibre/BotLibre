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
from datetime import datetime
from sdk import SDKConnection
from util.Utils import Writer
from util.Utils import Utils
class UserConfig(Config):
    def __init__(self):
        super().__init__()
        self.password = None
        self.newPassword = None
        self.hint = None
        self.name = None
        self.showName = False
        self.email = None
        self.website = None
        self.bio = None
        self.over18 = False
        self.avatar= None
        self.connects= None
        self.bots= None
        self.posts= None
        self.messages= None
        self.forums= None
        self.scripts= None
        self.graphics= None
        self.avatars= None
        self.domains= None
        self.channels= None
        self.joined= None
        self.lastConnect= None
        self.type= None
        self.isFlagged= None
        self.flaggedReason= None
    
    def displayJoined(self):
        try:
            date = datetime.strptime(self.joined, '%Y-%m-%d')  #Date string is in 'YYYY-MM-DD' format
            return date.strftime('%B %Y')  #Format as 'Month Year'
        except ValueError:
            return self.joined
    
    def displayLastJoined(self):
        try:
            date = datetime.strptime(self.lastConnect, '%Y-%m-%d')  #Date string is in 'YYYY-MM-DD' format
            return date.strftime('%B %Y')  #Format as 'Month Year'
        except ValueError:
            return self.lastConnect
    
    def addCredentials(self, connection: SDKConnection):
        self.application = connection.getCredentials().getApplicationId()
        if(connection.getDomain() != None):
            self.domain = connection.getDomain().id

    def parseXML(self, xml):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.user = root.attrib.get("user")
        self.name = root.attrib.get("name")
        self.showName = root.attrib.get("showName")
        self.token = root.attrib.get("token")
        self.email = root.attrib.get("email")
        self.hint = root.attrib.get("hint")
        self.website = root.attrib.get("website")
        self.connects = root.attrib.get("connects")
        self.bots = root.attrib.get("bots")
        self.posts = root.attrib.get("posts")
        self.messages = root.attrib.get("messages")
        self.forums = root.attrib.get("forums")
        self.channels = root.attrib.get("channels")
        self.avatars = root.attrib.get("avatars")
        self.scripts = root.attrib.get("scripts")
        self.graphics = root.attrib.get("graphics")
        self.domains = root.attrib.get("domains")
        self.joined = root.attrib.get("joined")
        self.lastConnect = root.attrib.get("lastConnect")
        self.type = root.attrib.get("type")
        self.isFlagged = root.attrib.get("isFlagged")
        
        #tag name
        self.bio = root.find("bio")
        self.avatar = root.find("avatar")
        self.flaggedReason = root.find("flaggedReason")
        
    def toXML(self):
        writer = Writer("<user")
        self.writeCredentials(writer)
        if(self.password!= None):
            writer.append(" password=\"" + self.password + "\"")
        
        if(self.newPassword != None):
            writer.append(" newPassword=\"" + self.newPassword + "\"")
            
        if(self.hint != None):
            writer.append(" hint=\"" + self.hint + "\"")
            
        if(self.name != None):
            writer.append(" name=\"" + self.name + "\"")
        
        if(self.showName != None):
            writer.append(" showName=\"" + str(self.showName) + "\"")
        
        if(self.email != None):
            writer.append(" email=\"" + self.email + "\"")
        
        if(self.website != None):
            writer.append(" website=\"" + self.website + "\"")
        
        if(self.over18 != None):
            writer.append(" over18=\"" + str(self.over18) + "\"")
        
        writer.append(">")
        
        if(self.bio != None):
            writer.append("<bio>")
            writer.append(Utils.escapeHTML(self.bio))
            writer.append("</bio>")

        writer.append("</user>")
        
        return writer
        
        