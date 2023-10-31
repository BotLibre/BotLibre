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
import xml.etree.ElementTree as ET
import requests
import os, tempfile, mimetypes
from PIL import Image
import traceback
import io


class Utils(object):
    def __init__(self) -> None:
        pass

    @staticmethod
    def loadXML(xml: str) -> ET.Element:
        if xml == None:
            return None
        try:
            if isinstance(xml, ET.Element):
                return xml
            return ET.fromstring(xml)
        except ET.ParseError as e:
            print("Error parsing XML:", str(e))
            return None

    @staticmethod
    def log(header: str, info: str = ""):
        print("\n")
        print("-"*50)
        print("|->" + str(header))
        if(info != ""):
            print("-"*50)
            print("|-->\n" + str(info)+"\n|-->")
            print("-"*50)
    
    def logs(header:str, *args: str):
        lineLen: int = 40
        headerLen: int = int(len(header)/2)
        padding: int = int(((lineLen-6)+headerLen)/2)
        print("\n")
        print("-"*(lineLen+headerLen))
        print("|->" + (" "*(padding-headerLen)) + str(header) + (" "*(padding-headerLen))+"<-|")
        for string_arg in args:
            if string_arg == None:
                continue
            print("-"*(lineLen+headerLen))
            print("|-->\n" + str(string_arg)+"\n|-->")
            print("-"*(lineLen+headerLen))

    @staticmethod
    def log_err(header: str, e: Exception):
        print("\n")
        print("x"*50)
        print("x->" + str(header))
        print("-"*50)
        print("x--> " + str(e))
        traceback.print_exc()
        print("x"*50)
        print("\n")

    @staticmethod
    def escapeHTML(html:str):
        escape_map = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "`": "&#96;",
            "'": "&#39;"
        }
        for char, entity in escape_map.items():
            html = html.replace(char, entity)
        return html

    # This function uses loads an image from a url and return's it as bytes.
    # Also returns fileType, and fileName.
    @staticmethod
    def PostImageFromURL(url: str):
        response = requests.get(url)
        if response.status_code == 200:
            name = os.path.basename(url)
            file = io.BytesIO()
            file.write(response.content)
            file.seek(0)  # file position to the beginning
            mime_type, _ = mimetypes.guess_type(name)
            return file, name, mime_type
        else:
            Utils.log_err("Status Code " + str(response.status_code), "Failed to get the image.")
 
 
    # Resize image for POSTIMAGE and POSTHDIMAGE TODO: test
    def resizeImage(self, sWith: int = 300, sHeight: int = 300, image = None):
        if(image == None):
            Utils.log_err("IMAGE NULL", "Need an image to resize.")
            return
        image = Image.open(image)
        image = image.resize(sWith, sHeight)
        byteArray = io.BytesIO()
        image.save(byteArray, format='JPEG', quality=90)
        byteArray.seek(0)
        return byteArray

class Writer:
    def __init__(self, initial_value=""):
        self.chars = list(initial_value)

    def append(self, text: str):
        self.chars.extend(text)

    def clear(self):
        self.chars.clear()
    
    def __str__(self):
        return "".join(self.chars)
