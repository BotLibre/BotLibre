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
from config.Config import Config
from config.UserConfig import UserConfig
from config.DomainConfig import DomainConfig
from config.ChatConfig import ChatConfig
from sdk.Credentials import Credentials
from util.Utils import Utils, Writer
from config.ChatResponse import ChatResponse
from config.ForumPostConfig import ForumPostConfig
from config.WebMediumConfig import WebMediumConfig
from config.MediaConfig import MediaConfig
from config.UserMessageConfig import UserMessageConfig
from config.AvatarMessage import AvatarMessage
from config.ResponseConfig import ResponseConfig
from config.AvatarMedia import AvatarMedia
from config.AvatarConfig import AvatarConfig
from config.ForumConfig import ForumConfig
from requests_toolbelt.multipart.encoder import MultipartEncoder



from typing import TypeVar
T = TypeVar('T', bound=WebMediumConfig)

class SDKConnection(object):
    types: list
    channelTypes: list
    accessModes: list
    mediaAccessModes: list
    learningModes: list
    correctionModes: list
    botModes: list
    url: str
    credentials: Credentials
    debug: bool
    domain: str
    user: UserConfig
    # Create an SDK connection with the credentials.
    # Use the Credentials subclass specific to your server.
    def __init__(self, credentials, debug=False):
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
    
    
    # Execute the custom API.
    def custom(self, api, config: Config, result: Config):
        config.addCredentials(self)
        xml = self.POST(self.url + "/" + api, config.toXML())
        if(xml == None):
            return None
        try:
            result.parseXML(xml)
        except Exception as e:
            Utils.log_err(api, e)
        return result
    
    
    # Connect to the domain.
	# A domain is an isolated content space.
	# Any browse or query request will be specific to the domain's content.
    def connectDomain(self, config: DomainConfig) -> DomainConfig:
        self.domain = self.fetch(config)
        return self.domain

    # Process the bot chat message and return the bot's response.
	# The ChatConfig should contain the conversation id if part of a conversation.
	# If a new conversation the conversation id i returned in the response.
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
            
            
    # Fetch the content details from the server.
	# The id or name and domain of the object must be set.
    def fetch(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e)    
    
    
    
    
    # Create the new content.
	# The content will be returned with its new id.
    def create(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config: T()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e) 
        
    # Update the content.
    def update(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config: T()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e)
    
    # Process the avatar message and return the avatars response.
	# This allows the speech and video animation for an avatar to be generated for the message.
    def avatarMessage(self, config: AvatarMessage) -> ChatResponse:
        config.addCredentials(self)
        xml = self.POST(self.url + "/avatar-message", config.toXML())
        if(xml == None):
            return None
        try:
            response = ChatResponse()
            response.parseXML(xml)
            return response
        except Exception as e:
            Utils.log_err("avatar-message", e)
    
    
    # Create a new user
    def createUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
            return user
        except Exception as e:
            Utils.log_err("/create-user", e)
            
    # Update the user details.
	# The password must be passed to allow the update.
    def updateUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
            return user
        except Exception as e:
            Utils.log_err("/update-user", e)
    
    
    # Permanently delete the content with the id.
    def delete(self, config: WebMediumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-" + config.getType(), config.toXML())
        if(self.domain!=None and self.domain.id == config.id and config.getType() == "domain"):
            self.domain = None
            
    
    
    # Create a new forum post.
    # You must set the forum id for the post.
    def createForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("/create-forum-post", e)
    
    
    # Update the forum post.
    def updateForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("update-forum-post" ,e)
            
    
    # Create or update the response.
	# This can also be used to flag, unflag, validate, or invalidate a response.
    def saveResponse(self, config: ResponseConfig) -> ResponseConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/save-response", config.toXML())
        if(xml == None):
            return None
        try:
            config = ResponseConfig()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("save-response", e)
    
    
    # Save the image as the avatar's background.
    def saveAvatarBackground(self, file, config: AvatarMedia):
        config.addCredentials(self)
        self.POSTIMAGE(self.url + "/save-avatar-background", file, config.name, config.toXML())
    # Add the avatar media file to the avatar.
    def createAvatarMedia(self, file, config: AvatarMedia):
        config.addCredentials(self)
        if(config.name.endswith(".jpg") or config.name.endswith(".jpeg") or config.name.endswith(".png")):
            if(config.hd): # TODO: Implement POSTHDIMAGE(..)
                self.POSTHDIMAGE(self.url + "/create-avatar-media", file, config.name, config.toXML())
            else:
                self.POSTIMAGE(self.url + "/create-avatar-media", file, config.name, config.toXML())
        else:
            self.POSTFILE(self.url + "/create-avatar-media", file, config.name, config.toXML())

    # Permanently delete the forum post with the id.
    def deleteForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-forum-post", config.toXML())
        
    # Permanently delete the response, greetings, or default response with the response id (and question id).
    def deleteResponse(self, config: ResponseConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-response", config.toXML())
        
    # Permanently delete the avatar media.
    def deleteAvatarMedia(self, config: AvatarMedia):
        config.addCredentials(self)
        self.POST(self.url + "/delete-avatar-media", config.toXML())
    
    # Permanently delete the avatar background.
    def deleteAvatarBackground(self, config: AvatarConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-avatar-background", config.toXML())
    
    # Save the avatar media tags.
    def saveAvatarMedia(self, config: AvatarMedia):
        config.addCredentials(self)
        self.POST(self.url + "/save-avatar-media", config.toXML())
    
    # Flag the content as offensive, a reason is required.
    def flag(self, config: T):
        config.addCredentials(self)
        self.POST(self.url + "/flag-" + config.getType(), config.toXML())
    
    # Flag the user as offensive, a reason is requried
    def flagUser(self, config: UserConfig):
        config.addCredentials(self)
        self.POST(self.url + "/flag-user", config.toXML())
        
    # Subscribe for email updates for the post.
    def subscribeForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/subscribe-post", config.toXML())
        
    # Unsubscribe from email updates for the post.
    def unsubscribe(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/unsubscribe-post", config.toXML())
    
    # Subscribe for email updates for the forum.
    def subscribeForum(self, config: ForumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/subscribe-forum",config.toXML())
        
    # Unsubscribe from email updates for the forum.
    def unsubscribeForum(self, config: ForumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/unsubscribe-forum",config.toXML())
        
    # Create a new file/image/media attachment for a chat channel.
    def createChannelFileAttachment(self, file, config: MediaConfig) -> MediaConfig:
        # TODO: Missing file
        config.addCredentials(self)
        xml = self.POSTFILE(self.url + "/create-channel-attachment", file, config.name, config.toXML())
        if(xml == None):
            return None
        try:
            media = MediaConfig()
            media.parseXML(xml)
            return media
        except Exception as e:
            Utils.log_err("/create-channel-attachment",e)
    
    # Create a new file/image/media attachment for a chat channel.       
    def createChannelImageAttachment(self, file, config: MediaConfig) -> MediaConfig:
        #TODO: Missing file
        config.addCredentials(self)
        xml = self.POSTIMAGE(self.url + "/create-channel-attachment", file, config.name, config.toXML())
        if(xml == None):
            return None
        try:
            media = MediaConfig()
            media.parseXML(xml)
            return media
        except Exception as e:
            Utils.log_err("/create-channel-attachment",e)
            
    # Create a reply to a forum post.
	# You must set the parent id for the post replying to.
    def createReply (self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-reply", config.toXML())
        if(xml == None):
            return None
        try:
            reply = ForumPostConfig()
            reply.parseXML(xml)
            return reply
        except Exception as e:
            Utils.log_err(e)
            
    # Create a user message.
	# This can be used to send a user a direct message.
	# SPAM will cause your account to be deleted.
    def createUserMessage(self, config: UserMessageConfig):
        config.addCredentials(self)
        self.POST(self.url + "/create-user-message", config.toXML())
    
    # Fetch the user details.
	# Function names can't be the same.
    def fetchUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/view-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            return user
        except Exception as e:
            Utils.log_err("view-user", e)
    
    # Fetch the forum post details for the forum post id.
    def fetchForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("check-forum-post", e)
            
    # Fetch the URL for the image from the server.
    def fetchImage(self, image: str):
        return "http://" + self.credentials.host + self.credentials.app + "/" + image
    
            
    
    # Enable debugging, debug messages will be logged to System.out.
    def setDebug(self, debug: bool):
        self.debug = debug

    # Return the current domain.
	# A domain is an isolated content space.
    def getDomain(self) -> DomainConfig:
        if(self.domain != None):
            return self.domain
        return None

    # Set the current domain.
	# A domain is an isolated content space.
	# connect() should be used to validate and connect a domain.
    def setDomain(self, domain: DomainConfig):
        self.domain = domain


    # Disconnect from the connection.
    # An SDKConnection does not keep a live connection, but this resets its connected user and admin.
    def disconnect(self):
        self.user = None
        self.domain = None


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
    
    # Return the current connected user.
    def getUser(self) -> UserConfig:
        if(self.user!= None):
            return self.user
        return None
    
    # Set the current connected user.
	# connect() should be used to validate and connect a user.
    def setUser(self, user: UserConfig):
        self.user = user
    
    
    # TODO: GET (url: str) - response.content;
    
    
    def POST(self, url: str, xml: str):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
        headers = {
            "Content-Type": "application/xml"
        }
        # Send the POST request with XML data
        response = requests.post(url, data=str(xml), headers=headers)
        
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                return None
                
        return response.text
    
    
    def POSTFILE(self, url, file, name: str, xml: str):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
            Utils.log("Name", str(name))
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': file,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_FILE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_FILE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                print(response.text)
                return None
                
        return response.text
    
    
    #TODO: resize image before uploading... 600x600
    def POSTHDIMAGE(self, url, image, name: str, xml: str):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
            Utils.log("Name", str(name))
        
        # TODO: Resize image - TESTING
        # Utils.resizeImage(600, 600, image)
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': image,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_IMAGE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_IMAGE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                print(response.text)
                return None
                
        return response.text
    
    #TODO: resize image before uploading... 300x300
    def POSTIMAGE(self, url, image, name: str, xml: str):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
            Utils.log("Name", str(name))
        
        # TODO: Resize image - TESTING
        # Utils.resizeImage(image=image)
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': image,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_IMAGE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                if(response.text==None or response.text=="null"):
                    print("Returned: Null.")
                    return
                Utils.log("POST_IMAGE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                return None
                
        return response.text

    def __str__(self):
        writer = Writer()
        writer.append("URL: " + self.url + "\n")
        writer.append("HOST: " + self.credentials.getHost() + "\n")
        writer.append("Application ID: " + self.credentials.applicationId + "\n")
        writer.append("Debug: " + str(self.debug))
        writer.append("\n******* User Details *******\n") if self.getUser() != None else None
        writer.append("Name: " + str(self.getUser().name)+ "\n") if self.getUser() != None else None
        writer.append("Email: " + str(self.getUser().email)+ "\n") if self.getUser() != None and self.getUser().email != "" else None
        writer.append("Connects: " + str(self.getUser().connects)+ "\n") if self.getUser() != None else None
        writer.append("Bots: " + str(self.getUser().bots)+ "\n") if self.getUser() != None else None
        writer.append("Joined: " + str(self.getUser().displayJoined())) if self.getUser() != None else None
        return writer.__str__()
        
