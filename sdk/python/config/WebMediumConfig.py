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
    def __init__(self):
        self.id
        self.name
        self.isAdmin
        self.isAdult
        self.isPrivate
        self.isHidden
        self.accessMode
        self.isFlagged
        self.isExternal
        self.isPaphus
        self.showAds = True
        self.forkAccessMode
        self.contentRating
        self.description
        self.details
        self.disclaimer
        self.website
        self.subdomain
        self.tags
        self.categories
        self.flaggedReason
        self.creator
        self.creationDate
        self.lastConnectedUser
        self.license
        self.avatar
        self.script
        self.graphic
        self.thumbsUp = 0
        self.thumbsDown = 0
        self.stars = ""
        self.connects
        self.dailyConnects
        self.weeklyConnects
        self.monthlyConnects

    @abstractmethod
    def getType(self):
        pass

    @abstractmethod
    def credentials(self):
        pass

    def stats(self):
        return ""

    def toString(self):
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
