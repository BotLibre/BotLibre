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


class ResponseConfig(Config):
    questionId: str
    responseId: str
    question: str
    response: str
    previous: str
    onRepeat: str
    label: str
    topic: str
    keywords: str
    required: str
    emotions: str
    actions: str
    poses: str
    noRepeat: bool
    requirePrevious: bool
    requireTopic: bool
    flagged: bool
    correctness: str
    command: str

    def __init__(self):
        self.questionId = None
        self.responseId = None
        self.question = None
        self.response = None
        self.previous = None
        self.onRepeat = None
        self.label = None
        self.topic = None
        self.keywords = None
        self.required = None
        self.emotions = None
        self.actions = None
        self.poses = None
        self.noRepeat = None
        self.requirePrevious = None
        self.requireTopic = None
        self.flagged = None
        self.correctness = None
        self.command = None

    def toXML(self):
        writer = Writer()
        self.writerXML(writer)
        return writer

    def writerXML(self, writer: Writer):
        writer = Writer("<response")
        self.writeCredentials(writer)
        if(self.questionId != None):
            writer.append(" questionId=\"" + self.questionId + "\"")
        if(self.responseId != None):
            writer.append(" responseId=\"" + self.responseId + "\"")
        if(self.label != None):
            writer.append(" label=\"" + self.label + "\"")
        if(self.topic != None):
            writer.append(" topic=\"" + self.topic + "\"")
        if(self.keywords != None):
            writer.append(" keywords=\"" + self.keywords + "\"")
        if(self.required != None):
            writer.append(" required=\"" + self.required + "\"")
        if(self.emotions != None):
            writer.append(" emotions=\"" + self.emotions + "\"")
        if(self.actions != None):
            writer.append(" actions=\"" + self.actions + "\"")
        if(self.poses != None):
            writer.append(" poses=\"" + self.poses + "\"")
        if(self.correctness != None):
            writer.append(" correctness=\"" + self.correctness + "\"")

        writer.append(" noRepeat=\"true\"") if self.noRepeat == True else None
        writer.append(" requirePrevious=\"true\"") if self.requirePrevious == True else None
        writer.append(" requireTopic=\"true\"") if self.requireTopic == True else None
        writer.append(" flagged=\"true\"") if self.flagged == True else None
        writer.append(">")
        if(self.question != None):
            writer.append("<question>")
            writer.append(Utils.escapeHTML(self.question))
            writer.append("</question>")

        if(self.response != None):
            writer.append("<response>")
            writer.append(Utils.escapeHTML(self.response))
            writer.append("</response>")

        if(self.previous != None):
            writer.append("<previous>")
            writer.append(Utils.escapeHTML(self.previous))
            writer.append("</previous>")

        if(self.onRepeat != None):
            writer.append("<onRepeat>")
            writer.append(Utils.escapeHTML(self.onRepeat))
            writer.append("</onRepeat>")

        if(self.command != None):
            writer.append("<command>")
            writer.append(Utils.escapeHTML(self.command))
            writer.append("</command>")
        writer.append("</response>")

    def parseXML(self, xml:str):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return
        self.questionId = root.attrib.get("questionId")
        self.responseId = root.attrib.get("responseId")
        self.label = root.attrib.get("label")
        self.topic = root.attrib.get("topic")
        self.keywords = root.attrib.get("keywords")
        self.required = root.attrib.get("required")
        self.emotions = root.attrib.get("emotions")
        self.actions = root.attrib.get("actions")
        self.poses = root.attrib.get("poses")
        self.type = root.attrib.get("type")
        self.correctness = root.attrib.get("correctness")
        # tag name
        if(root.find("noRepeat") != None):
            self.noRepeat = root.find("noRepeat")
        if(root.find("requireTopic") != None):
            self.requireTopic = root.find("requireTopic")
        if(root.find("flagged") != None):
            self.flagged = root.find("flagged")
        if(root.find("requirePrevious") != None):
            self.requirePrevious = root.find("requirePrevious")
        if(root.find("question") != None):
            self.question = root.find("question")
        if(root.find("response") !=None):
            self.response = root.find("response")

        if(root.find("command") != None):
            self.command = root.find("command")
            self.command = str(self.command).replace("&#34;", "\"")
