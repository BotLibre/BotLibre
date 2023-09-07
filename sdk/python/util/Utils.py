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
import traceback
class Utils(object):
    def __init__(self) -> None:
        pass
    
    @staticmethod
    def loadXML(xml):
        data = xml.content
        root = ET.fromstring(data)
        return root


    @staticmethod
    def log(header, info):
        print("\n")
        print("-"*50)
        print("|->" + str(header))
        print("-"*50)
        print("|--> " + str(info))
        print("-"*50)
        print("\n")
        
        
    @staticmethod
    def log_err(header, e: Exception):
        print("\n")
        print("x"*50)
        print("x->" + str(header))
        print("-"*50)
        print("x--> " + str(e))
        traceback.print_exc()
        print("x"*50)
        print("\n")

    @staticmethod
    def escapeHTML(html):
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

    @staticmethod
    # Define a function to print all elements and their text recursively
    def print_elements(element, indent=""):
        print(indent + element.tag, element.attrib)
        for child in element:
            Utils.print_elements(child, indent + "  ")
            if child.text and child.text.strip():
                print(indent + "  Text:", child.text)
    
    
class Writer:
    def __init__(self, initial_value=""):
        self.chars = list(initial_value)

    def append(self, text):
        self.chars.extend(text)

    def clear(self):
        self.chars.clear()

    def __str__(self):
        return "".join(self.chars)