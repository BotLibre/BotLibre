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
from sdk.SDKConnection import SDKConnection
from sdk.BotlibreCredentials import BotlibreCredentials
from util.Utils import Utils
from config.DomainConfig import DomainConfig
from config.UserConfig import UserConfig
from config.ChatResponse import ChatResponse
from config.ChatConfig import ChatConfig

class Main(object):
    def __init__(self, debug=True, adult=False):
        self.debug = debug
        self.adult = adult
        self.applicationId = ""
        self.domainId = None
        self.username = ""
        self.password = ""
        self.domain = ""
        self.connection = None
        self.defaultType = "Bots"
        self.website = "http://www.botlibre.com"
        self.websiteHttps = "https://www.botlibre.com"
        self.server = "botlibre.com"
        self.botInstanceId = ""
        self.showAds = True
        self.connection = SDKConnection(BotlibreCredentials(self.applicationId))
        if(self.domainId != None):
            self.domain = DomainConfig()
            self.domain.id = self.domainId
            self.connection.setDomain(self.domain)
        if(self.debug):
            Utils.log("[Main] init SDKConnection", self.connection)
            self.showAds = False
            self.connection.setDebug(True)
        
    def connectUserAccount(self) -> bool:
        if(self.connection.user != None):
            Utils.log("User LoggedIn","Token has been established.")
            return
        userConfig = UserConfig()
        userConfig.application = self.applicationId
        userConfig.user =self.username
        userConfig.password = self.password
        if(self.username =="" or self.password == "" or self.application == ""):
            Utils.log("[Main]","Please fill the required data @ connectUserAccount in main.py")
            return False
        self.connection.connect(userConfig)
        return True
    
    def sendChatMessage(self) -> ChatResponse:
        check_user = self.connectUserAccount()
        if(check_user == False):
            return None
        config = ChatConfig()
        config.instance = "165"
        config.message = input("BotID: "+ config.instance + " - " +"Enter Message: ")
        return self.connection.chat(config)
    
    
    
    


###### MAIN ######
main = Main()
instructions = (
    """
    0 - User Account
    1 - Post Chat
    """
        )
print(instructions)
def switch(option):
    try:
        if(int(option)==0):## Connect User Account
            main.connectUserAccount()
            ## Check user data
            user = main.connection.user
            Utils.log("User Details", "User: "+user.user + "\nEmail: " + user.email)
        elif(int(option)==1): ## Chat
            response = main.sendChatMessage()
            Utils.log("Message Detials", "Response: " + response.message)
        else:
            pass
    except:
        pass



while True:
    user_input = input("Enter an option (0 : 99) ('q' to quit or 'h' for help): ")

    # Check if the user wants to quit
    if user_input.lower() == 'q':
        break 
    elif user_input.lower() == 'h':
        print(instructions)
        pass

    switch(user_input)
        