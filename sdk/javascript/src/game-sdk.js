/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

/**
 * Bot Libre open gaming SDK.
 * This JavaScript SDK lets you integrate web games with your bot.
 * 
 * Version: 6.0.0-2017-10-16
 */

/**
 * Example game implementation of Tic Tac Toe.
 * This Game interface can be used to create a new game class.
 * @class
 */
function TicTacToe() {
	this.listener;
	
	/**
	 * Initialize the game with the WebChatbotListener.
	 */
	this.init = function(listener) {
		this.listener = listener;
	}
	
	this.moveSquare = function(cordinate) {
	    this.listener.sendMessage(cordinate);
	}

	this.drawBoard = function(newBoard) {
	    if (newBoard.length < 9) {
	        return;
	    }
	    var div = document.getElementById(this.listener.prefix + "avatar-game-div");	    
	    var html = "<table id='tictactoe'><tr>";
	    for (var count = 0; count < 9; count++) {
	        var character = newBoard.substring(count, count + 1);
	        var cordinate = "A" + (count + 1);
	        if (count > 2) {
	            cordinate = "B" + (count - 2);
	        }
	        if (count > 5) {
	            cordinate = "C" + (count - 5);
	        }
	        if (character == "X") {
	            html = html + "<td class='X'>" + character + "</td>";
	        }
	        if (character == "O") {
	            html = html + "<td class='O'>" + character + "</td>";
	        }
	        if (character == "_") {
	        html = html + "<td class='blank'>" + character + "</td>";
	        }
	        if (count == 2 || count == 5) {
	            html = html + "</tr><tr>";
	        }
	    }
	    html = html + "</tr></table>";
	    div.innerHTML = html;
    	var cells = div.getElementsByTagName("td");
    	var self = this;
		for (var count = 0; count < cells.length; count++) {
	        var coordinate = "A" + (count + 1);
	        if (count > 2) {
	        	coordinate = "B" + (count - 2);
	        }
	        if (count > 5) {
	        	coordinate = "C" + (count - 5);
	        }
			var cell = cells[count];
			var scope = function() {
				var scopedCoordinate = coordinate;
				cell.addEventListener("click", function(event) {
					self.moveSquare(scopedCoordinate);
					event.stopPropagation();
					return true;
				});
			};
			scope();
		}
	}

	/**
	 * Callback to let the game draw the new board from the bot's message command.
	 * The board is drawn on the "avatar-game-div" element.
	 */
	this.updateAvatar = function(responseMessage) {
		var command = JSON.parse(responseMessage.command);
	    console.log(command);
	    if (command == null || command.board == null) {
			var div = document.getElementById(this.listener.prefix + "avatar-game-div");
			if (div != null) {
				div.style.display = "none";
			}	    	
			return;
	    }
		this.drawBoard(command.board);
		var div = document.getElementById(this.listener.prefix + "avatar-video-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(this.listener.prefix + "avatar-canvas-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(this.listener.prefix + "avatar-image-div");
		if (div != null) {
			div.style.display = "none";
		}
	    var div = document.getElementById(this.listener.prefix + "avatar-game-div");
	    if (div != null) {
			div.style.display = "inline-block";
		}
	}
}
