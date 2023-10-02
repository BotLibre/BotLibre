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
import datetime
from util.Utils import Utils, Writer
from config.Config import Config
class ForumPostConfig(Config):
    id: str
    topic: str
    summary: str
    details: str
    detailsText: str
    forum: str
    tags: str
    thumbsUp: int
    thumbsDown: int
    stars: str
    isAdmin: str
    isFlagged: str
    creator: str
    creationDate: str
    views: str
    dailyViews: str
    weeklyViews: str
    monthlyViews: str
    replyCount: str
    parent:str
    avatar:str
    replies: list
    
    def __init__(self):
        super().__init__()
        self.id = None
        self.topic = None
        self.summary = None
        self.details = None
        self.detailsText = None
        self.forum = None
        self.tags = None
        self.thumbsUp = 0
        self.thumbsDown = 0
        self.stars = "0"
        self.isAdmin = None
        self.isFlagged = None
        self.flaggedReason = None
        self.isFeatured = False
        self.creator = None
        self.creationDate = None
        self.views = None
        self.dailyViews = None
        self.weeklyViews = None
        self.monthlyViews = None
        self.replyCount = None
        self.parent = None
        self.avatar = None
        self.replies = []
        
    def toXML(self):
        writer = Writer("<")
        self.writerXML(writer)
        return writer
    
    
    def writerXML(self, writer:Writer):
        writer.append("forum-post")
        self.writeCredentials(writer)
        if(self.id!=None):
            writer.append(" id=\"" + self.id + "\"")
        if(self.parent!=None):
            writer.append(" parent=\"" + self.parent + "\"")
        if(self.forum!=None):
            writer.append(" forum=\"" + self.forum + "\"")
        if(self.isFeatured!=None):
            writer.append(" isFeatured=\"true\"")
        if(self.stars!=None):
            writer.append(" stars=\"" + self.stars + "\"")
        writer.append(">")
        if(self.topic!=None):
            writer.append("<topic>")
            writer.append(Utils.escapeHTML(self.topic))
            writer.append("</topic>")
        if(self.details!=None):
            writer.append("<details>")
            writer.append(Utils.escapeHTML(self.details))
            writer.append("</details>")
        if(self.tags!=None):
            writer.append("<tags>")
            writer.append(Utils.escapeHTML(self.tags))
            writer.append("</tags>")
        if(self.flaggedReason!=None):
            writer.append("<flaggedReason>")
            writer.append(Utils.escapeHTML(self.flaggedReason))
            writer.append("</flaggedReasons>")
        writer.append("</forum-post>")
        
    def parseXML(self, xml):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.id = root.attrib.get("id")
        self.parent = root.attrib.get("parent")
        self.forum = root.attrib.get("forum")
        self.views = root.attrib.get("views")
        self.dailyViews = root.attrib.get("dailyViews")
        self.weeklyViews = root.attrib.get("weeklyViews")
        self.monthlyViews = root.attrib.get("monthlyViews")
        self.isAdmin = root.attrib.get("isAdmin")
        self.replyCount = root.attrib.get("replyCount")
        self.isFlagged = root.attrib.get("isFlagged")
        self.isFeatured = root.attrib.get("isFeatured")
        self.creator = root.attrib.get("creator")
        self.creationDate = root.attrib.get("creationDate")
        if(root.attrib.get("thumbsUp")!=None and len(root.attrib.get("thumbsUp").strip()) > 0):
            self.thumbsUp = int(root.attrib.get("thumbsUp"))
        if(root.attrib.get("thumbsDown")!=None and len(root.attrib.get("thumbsDown").strip()) > 0):
            self.thumbsDown = int(root.attrib.get("thumbsDown"))
        if(root.attrib.get("stars")!=None and len(root.attrib.get("stars").strip()) > 0):
            self.stars = root.attrib.get("stars")
        
        #tag name
        if(root.find("summary")!=None):  
            self.summary = root.find("summary").text
        if(root.find("details")!=None):  
            self.details = root.find("details").text
        if(root.find("detailsText")!=None):  
            self.detailsText = root.find("detailsText").text
        if(root.find("topic")!=None):  
            self.topic = root.find("topic").text
        if(root.find("flaggedReason")!=None):  
            self.flaggedReason = root.find("flaggedReason").text
        if(root.find("avatar")!=None):
            self.avatar = root.find("avatar").text
        if(root.find("avatar")!=None):
            self.avatar = root.find("avatar").text
        if(root.findall(".//replies")!=None):
            for reply in root.findall(".//replies"):
                config = ForumPostConfig()
                config.parseXML(reply)
                self.replies.append(config)
        
    def credentails(self):
        config = ForumPostConfig()
        config.id = self.id
        return config
    
    def displayCreationDate(self):
        try:
            date = datetime.strptime(self.joined, '%Y-%m-%d')  #Date string is in 'YYYY-MM-DD' format
            return date.strftime('%B %Y')  #Format as 'Month Year'
        except ValueError:
            return self.creationDate