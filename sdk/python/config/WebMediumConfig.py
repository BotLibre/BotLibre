############################################################################
#
#  Copyright 2023 Paphus Solutions Inc.
#
#  Licensed under the Eclipse Public License, Version 1.0 (the "License")
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
from abc import abstractmethod
from util.Utils import Writer
from util import Utils


class WebMediumConfig(Config):
    id:str
    name:str
    isAdmin:bool
    isAdult:bool
    isPrivate: bool
    isHidden: bool
    accessMode:str
    isFlagged: bool
    isExternal: bool
    isPaphus: bool
    showAds: bool
    forkAccessMode:str
    contentRating:str
    description:str
    details:str
    disclaimer:str
    website:str
    subdomain:str
    tags:str
    categories:str
    flaggedReason:str
    creator:str
    creationDate:str
    lastConnectedUser:str
    license:str
    avatar:str
    script:str
    graphic:str
    thumbsUp:int
    thumbsDown:int
    stars: str
    connects: str
    dailyConnects: str
    weeklyConnects: str
    monthlyConnects: str
    def __init__(self):
        super().__init__()
        self.id = None
        self.name = None
        self.isAdmin = False
        self.isAdult = False
        self.isPrivate = False
        self.isHidden = False
        self.accessMode = None
        self.isFlagged = False
        self.isExternal = False
        self.isPaphus = False
        self.showAds = True
        self.forkAccessMode = None
        self.contentRating = None
        self.description = None
        self.details = None
        self.disclaimer = None
        self.website = None
        self.subdomain = None
        self.tags = None
        self.categories = None
        self.flaggedReason = None
        self.creator = None
        self.creationDate = None
        self.lastConnectedUser = None
        self.license = None
        self.avatar = None
        self.script = None
        self.graphic = None
        self.thumbsUp = 0
        self.thumbsDown = 0
        self.stars = None
        self.connects = None
        self.dailyConnects = None
        self.weeklyConnects = None
        self.monthlyConnects = None

    @abstractmethod
    def getType(self) -> str:
        pass

    @abstractmethod
    def credentials(self):
        pass

    def stats(self) -> str:
        return ""

    def toString(self) -> str:
        return self.name

    def getToken(self) -> int:
        token = 0
        if(self.token != None):
            token = int(self.token)
        return token

    def writeXML(self, writer: Writer):
        self.writeCredentials(writer)
        if(self.id != None):
            writer.append(" id=\"" + self.id + "\"")

        if(self.name != None):
            writer.append(" name=\"" + self.name + "\"")

        if(self.isPrivate != None and self.isPrivate == True):
            writer.append(" isPrivate=\"true\"")

        if(self.isHidden != None and self.isHidden == True):
            writer.append(" isHidden=\"true\"")

        if(self.accessMode != None):
            writer.append(" accessMode=\"" + self.accessMode + "\"")

        if(self.contentRating != None):
            writer.append(" contentRating=\"" + self.contentRating + "\"")

        if(self.forkAccessMode != None):
            writer.append(" forkAccessMode=\"" + self.forkAccessMode + "\"")

        if(self.stars != None):
            writer.append(" stars=\"" + self.stars + "\"")

        if(self.isAdult != None and self.isAdult == True):
            writer.append(" isAdult=\"true\"")

        if(self.isFlagged != None and self.isFlagged == True):
            writer.append(" isFlagged=\"true\"")
        if(self.isExternal != None and self.isExternal == True):
            writer.append(" isExternal=\"true\"")
        if(self.showAds != None and self.showAds == True):
            writer.append(" showAds=\"true\"")
        if(self.isAdult != None and self.isAdult == True):
            writer.append(" isAdult=\"true\"")
            
        writer.append(">")
        if (self.description != None):
            writer.append("<description>")
            writer.append(Utils.escapeHTML(self.description))
            writer.append("</description>")

        if (self.details != None):
            writer.append("<details>")
            writer.append(Utils.escapeHTML(self.details))
            writer.append("</details>")

        if (self.disclaimer != None):
            writer.append("<disclaimer>")
            writer.append(Utils.escapeHTML(self.disclaimer))
            writer.append("</disclaimer>")

        if (self.categories != None):
            writer.append("<categories>")
            writer.append(Utils.escapeHTML(self.categories))
            writer.append("</categories>")

        if (self.tags != None):
            writer.append("<tags>")
            writer.append(Utils.escapeHTML(self.tags))
            writer.append("</tags>")

        if (self.license != None):
            writer.append("<license>")
            writer.append(Utils.escapeHTML(self.license))
            writer.append("</license>")

        if (self.subdomain != None):
            writer.append("<subdomain>")
            writer.append(Utils.escapeHTML(self.subdomain))
            writer.append("</subdomain>")

        if (self.flaggedReason != None):
            writer.append("<flaggedReason>")
            writer.append(Utils.escapeHTML(self.flaggedReason))
            writer.append("</flaggedReason>")

    def parseXML(self, xml):
        root = Utils.loadXML(xml)
        if(root == None):
            return
        print(root)
        self.id = root.attrib.get("id")
        self.name = root.attrib.get("name")
        self.creationDate = root.attrib.get("creationDate")
        self.isPrivate = root.attrib.get("isPrivate")
        self.isHidden = root.attrib.get("isHidden")
        self.accessMode = root.attrib.get("accessMode")
        self.contentRating = root.attrib.get("contentRating")
        self.forkAccessMode = root.attrib.get("forkAccessMode")
        self.isAdmin = root.attrib.get("isAdmin")
        self.isAdult = root.attrib.get("isAdult")
        self.isFlagged = root.attrib.get("isFlagged")
        self.isExternal = root.attrib.get("isExternal")
        self.creator = root.attrib.get("creator")
        self.creationDate = root.attrib.get("creationDate")
        self.connects = root.attrib.get("connects")
        self.dailyConnects = root.attrib.get("dailyConnects")
        self.weeklyConnects = root.attrib.get("weeklyConnects")
        self.showAds = root.attrib.get("showAds")
        self.monthlyConnects = root.attrib.get("monthlyConnects")
        self.thumbsUp = root.attrib.get("thumbsUp")
        self.thumbsDown = root.attrib.get("thumbsDown")

        # tag names
        self.description = root.find("description")
        self.details = root.find("details")
        self.disclaimer = root.find("disclaimer")
        self.categories = root.find("categories")
        self.tags = root.find("tags")
        self.flaggedReason = root.find("flaggedReason")
        self.lastConnectedUser = root.find("lastConnectedUser")
        self.license = root.find("license")
        self.website = root.find("website")
        self.subdomain = root.find("subdomain")
        self.avatar =root.find("avatar")
