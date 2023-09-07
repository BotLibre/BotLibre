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


# Credential used to establish a connection.
# Requires the url, and an application id.
# You can obtain your application id from your user details page on the hosting website.
class Credentials(object):
    def __init__(self):
        self.host = ""
        self.app = ""
        self.url = ""
    
        # Your application's unique identifier.
        # You can obtain your application id from your user details page on the hosting website.
        self.applicationId=""
    
    def Credentials(self,url, applicationId):
        self.url = url
        self.applicationId = applicationId
    
    def getUrl(self):
        return self.url
    # Sets the server host name, i.e. www.paphuslivechat.com
    def getHost(self):
        return self.host
    
    def getApp(self):
        return self.app
    
    #Sets the hosted service server url, i.e. http://www.paphuslivechat.com
    def setUrl(self, url):
        self.url = url
        
    #Sets the server host name, i.e. www.paphuslivechat.com
    def setHost(self, host):
        self.host = host
        
    #Sets an app url postfix, this is normally not required, i.e. "".
    def setApp(self, app):
        self.app = app
    
    #Returns your application's unique identifier.
    def getApplicationId(self):
        return self.applicationId
    
    # Sets your application's unique identifier.
    # You can obtain your application id from your user details page on the hosting website.
    def setApplicationId(self, applicationId):
        self.applicationId = applicationId
        
    