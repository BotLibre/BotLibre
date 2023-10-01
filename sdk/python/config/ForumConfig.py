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
from util.Utils import Utils, Writer

class ForumConfig(WebMediumConfig):
    replyAccessMode: str
    postAccessMode: str
    posts: str
    
    def __init__(self):
        super().__init__()
        self.replyAccessMode = None
        self.postAccessMode = None
        self.posts = None
    
    
    def getType(self) -> str:
        return "forum"
    
    
    def stats(self) -> str:
        return self.posts + " posts"
    
    def credentials(self):
        config = ForumConfig()
        config.id = self.id
        return config
    
    def toXML(self):
        writer = Writer("<forum")
        if(self.replyAccessMode!=None):
            writer.append(" replyAccessMode=\"" + self.replyAccessMode + "\"")
            
        if(self.postAccessMode!=None):
            writer.append(" postAccessMode=\"" + self.postAccessMode + "\"")
        
        self.writeXML(writer)
        writer.append("</forum>")
        return writer
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return 
        self.replyAccessMode = root.attrib.get("replyAccessMode")
        self.postAccessMode = root.attrib.get("postAccessMode")
        self.posts = root.attrib.get("posts")