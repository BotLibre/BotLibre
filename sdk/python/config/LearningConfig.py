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
class LearningConfig(Config):
    learningMode: str
    learningRate: str
    correctionMode: str
    enableComprehension: bool
    enableEmoting: bool
    enableEmotions: bool
    enableConsciousness: bool
    enableWiktionary: bool
    enableResponseMatch: bool
    learnGrammar: bool
    synthesizeResponse: bool
    fixFormulaCase: bool
    checkExactMatchFirst: bool
    scriptTimeout: int
    responseMatchTimeout: int
    conversationMatchPercentage: str
    discussionMatchPercentage: str
    nlp: str
    language: str
    disableFlag: bool
    reduceQuestions: bool
    trackCase: bool
    
    def __init__(self):
        super().__init__()
        self.learningMode = None
        self.learningRate = None
        self.correctionMode = None
        self.enableComprehension = False
        self.enableEmoting = False
        self.enableEmotions = False
        self.enableConsciousness = False
        self.enableWiktionary = False
        self.enableResponseMatch = False
        self.learnGrammar = False
        self.synthesizeResponse= False
        self.fixFormulaCase= False
        self.checkExactMatchFirst= False
        self.scriptTimeout = None
        self.responseMatchTimeout = None
        self.conversationMatchPercentage = None
        self.discussionMatchPercentage = None
        self.nlp = None
        self.language = None
        self.disableFlag = False
        self.reduceQuestions = False
        self.trackCase = False
    
    def parseXML(self, xml):
        super().parseXML(xml)
        root = Utils.loadXML(xml)
        if(root == None):
            return 
        self.learningMode = root.attrib.get("learningMode")
        self.learningRate = root.attrib.get("learningRate")
        self.enableComprehension = root.attrib.get("enableComprehension")
        self.enableEmoting = root.attrib.get("enableEmoting")
        self.enableEmotions = root.attrib.get("enableEmotions")
        self.enableConsciousness = root.attrib.get("enableConsciousness")
        self.enableWiktionary = root.attrib.get("enableWiktionary")
        self.enableResponseMatch = root.attrib.get("enableResponseMatch")
        self.learnGrammar = root.attrib.get("learnGrammar")
        self.synthesizeResponse = root.attrib.get("synthesizeResponse")
        self.fixFormulaCase = root.attrib.get("fixFormulaCase")
        self.checkExactMatchFirst = root.attrib.get("checkExactMatchFirst")
        self.nlp = root.attrib.get("nlp")
        self.language = root.attrib.get("language")
        self.disableFlag = root.attrib.get("disableFlag")
        self.reduceQuestions = root.attrib.get("reduceQuestions")
        self.trackCase = root.attrib.get("trackCase")
        
        value = root.attrib.get("scriptTimeout")
        if(value!=None and len(value)>0):
            self.scriptTimeout = int(value)
        value = root.attrib.get("responseMatchTimeout")
        if(value!=None and len(value)>0):
            self.scriptTresponseMatchTimeoutimeout = int(value)
        self.conversationMatchPercentage = root.attrib.get("conversationMatchPercentage")
        self.discussionMatchPercentage = root.attrib.get("discussionMatchPercentage")
    
    def toXML(self):
        writer = Writer("<learning")
        self.writeCredentials(writer)
        if(self.learningMode!=None):
            writer.append(" learningMode=\"" + self.learningMode + "\"")
        if(self.correctionMode!=None):
            writer.append(" correctionMode=\"" + self.correctionMode + "\"")
        writer.append(" enableComprehension=\"" + str(self.enableComprehension) + "\"")
        writer.append(" enableEmoting=\"" + str(self.enableEmoting) + "\"")
        writer.append(" enableEmotions=\"" + str(self.enableEmotions) + "\"")
        writer.append(" enableConsciousness=\"" + str(self.enableConsciousness) + "\"")
        writer.append(" enableWiktionary=\"" + str(self.enableWiktionary) + "\"")
        writer.append(" enableResponseMatch=\"" + str(self.enableResponseMatch) + "\"")
        writer.append(" learnGrammar=\"" + str(self.learnGrammar) + "\"")
        writer.append(" synthesizeResponse=\"" + str(self.synthesizeResponse) + "\"")
        writer.append(" fixFormulaCase=\"" + str(self.fixFormulaCase) + "\"")
        writer.append(" checkExactMatchFirst=\"" + str(self.checkExactMatchFirst) + "\"")
        writer.append(" nlp=\"" + str(self.nlp) + "\"")
        writer.append(" language=\"" + str(self.language) + "\"")
        writer.append(" disableFlag=\"" + str(self.disableFlag) + "\"")
        writer.append(" reduceQuestions=\"" + str(self.reduceQuestions) + "\"")
        writer.append(" trackCase=\"" + str(self.trackCase) + "\"")
        if (self.scriptTimeout!= None and self.scriptTimeout != 0):
            writer.append(" scriptTimeout=\"" + str(self.scriptTimeout) + "\"")
        if (self.responseMatchTimeout != None):
            writer.append(" responseMatchTimeout=\"" + str(self.responseMatchTimeout) + "\"")
        if (self.conversationMatchPercentage != None):
            writer.append(" conversationMatchPercentage=\"" + str(self.conversationMatchPercentage) + "\"")
        if (self.learningRate):
            writer.append(" learningRate=\"" + str(self.learningRate) + "\"")
            
        writer.append("/>")
        return writer