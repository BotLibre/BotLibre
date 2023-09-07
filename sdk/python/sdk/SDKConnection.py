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
import requests
from config.UserConfig import UserConfig
from config.DomainConfig import DomainConfig
from config.ChatConfig import ChatConfig
from sdk.Credentials import Credentials
from util.Utils import Utils
from config.ChatResponse import ChatResponse

class SDKConnection(object):
    # Create an SDK connection with the credentials.
        # Use the Credentials subclass specific to your server.
    def __init__(self, credentials, debug=True):
        self.types = ["Bots", "Forums", "Graphics",
                      "Live Chat", "Domains", "Scripts", "IssueTracker"]
        self.channelTypes = ["ChatRoom", "OneOnOne"]
        self.accessModes = ["Everyone", "Users", "Members", "Administrators"]
        self.mediaAccessModes = ["Everyone", "Users",
                                 "Members", "Administrators", "Disabled"]
        self.learningModes = ["Disabled",
                              "Administrators", "Users", "Everyone"]
        self.correctionModes = ["Disabled",
                                "Administrators", "Users", "Everyone"]
        self.botModes = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
        self.url = credentials.url
        self.credentials = credentials
        self.debug = debug
        self.domain = None
        self.user = None

    # Return the name of the default user image.
    def defaultUserImage():
        return "images/user-thumb.jpg"

    # Validate the user credentials (password, or token).
    # The user details are returned (with a connection token, password removed).
    # The user credentials are soted in the connection, and used on subsequent calls.
    # An SDKException is thrown if the connect failed.
    def connect(self, config: UserConfig) -> UserConfig:
        print("test")
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-user", config.toXML())
        if(xml == None):
            self.user == None
            return None
        
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
        except Exception as e:
            Utils.log_err("check-user", e)
        
        return self.user
    

    #Process the bot chat message and return the bot's response.
	#The ChatConfig should contain the conversation id if part of a conversation.
	#If a new conversation the conversation id i returned in the response.
    def chat(self, config: ChatConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/post-chat",config.toXML())
        if(xml == None):
            return None
        try:
            response = ChatResponse()
            response.parseXML(xml)
            return response
        except Exception as e:
            Utils.log_err("post-chat", e)
            
            
    #Enable debugging, debug messages will be logged to System.out.
    def setDebug(self, debug):
        self.debug = debug

    #Return the current domain.
	#A domain is an isolated content space.
    def getDomain(self) -> DomainConfig:
        if(self.domain != None):
            return self.domain
        return None

    # Set the current domain.
	# A domain is an isolated content space.
	# connect() should be used to validate and connect a domain.
    def setDomain(self, domain: DomainConfig):
        self.domain = domain

    # Return the current application credentials.
    def getCredentials(self) -> Credentials:
        if(self.credentials == None):
            vars(self.credentials)
            Utils.log("SDKConnection Credentials","Credentials is null")
            return None
        return self.credentials

    #Set the application credentials.
    def setCredentials(self, credentails: Credentials):
        self.credentials = credentails
        self.url = credentails.url
        
    # Return is debugging has been enabled
    def isDebug(self) -> bool:
        return self.debug


    def POST(self, url, xml):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
        headers = {
            "Content-Type": "application/xml"
        }
        # Send the POST request with XML data
        response = requests.post(url, data=str(xml), headers=headers)
        if response.status_code == 200:
            if(self.debug):
                Utils.log("POST_SUCCESSFUL: " + url, response.content)
        else:
            if(self.debug):
                Utils.log("POST_FAILED: " + response.status_code, response.content)
                
        return response
