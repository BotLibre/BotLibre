// Allows bot to play and learn to not lose at Checkers.
state Checkers {
	pattern "checkers" answer start();
	pattern "checkers" answer start();
    pattern "[stop exit quit end]" topic "Checkers" answer end();
    pattern "* *" topic "Checkers" answer play();
    pattern "*" topic "Checkers whoStarts" answer whoStarts();
	pattern "checkers autoplay" answer autoPlay();
	pattern "checkers autoplay *" answer autoPlayMany();
	pattern "[yes yep yeah y ok okay]" topic "Checkers playAgain" answer start();
	pattern "*" topic "Checkers playAgain" answer end();
	
	function end() {
	    conversation.topic = null;
    	conversation.board = null;
	    conversation.playerBoard = null;
	    return "Ok, thanks for playing.";
	}
    
	function start() {
    	conversation.topic = "Checkers whoStarts";
    	conversation.board = null;
	    conversation.playerBoard = null;
	    return "Do you want to be <button>R</button> or <button>B</button>?"; 
	}
	
    // Responds in accordance to whether the bot is R or B by giving the player a blank board or making a move and returning the board.
	function whoStarts() {
	    if (star != "R" && star != "B") {
	        return "Type R or B.";
	    }
	    conversation.topic = "Checkers";
	    conversation.board = "/r/r/r/rr/r/r/r//r/r/r/r_/_/_/_//_/_/_/_b/b/b/b//b/b/b/bb/b/b/b/";
	    conversation.foo = "/r/r/r/rr/r/r/r//r/r/r/r_/_/_/_//_/_/_/_b/b/b/b//b/b/b/bb/b/b/b/";
	    var newBoard = conversation.board;
	    if (star == "R") {
	        conversation.player = "R";
	        conversation.bot = "B";
	        Avatar.setCommand({ type: "game", start:"Checkers", board: newBoard });
	        return "Your Move.";
	    }
	    conversation.bot = "R";
	    conversation.player = "B";
	    var bot = conversation.bot;
	    var player = conversation.player;
    	makeMove();
    	Avatar.setCommand({ type: "game", start:"Checkers", board: newBoard });
    	return "Your move.";
	}
	
    // Plays Checkers. Ensures the player has made a valid move.
	function play() {
	    var board = conversation.board;
	    var newBoard = board;
	    var bot = conversation.bot;
	    var player = conversation.player;
	    var turn = player;
	    var moves = 0;
	    if (newBoard.length() != 64 ) {
	        Avatar.setCommand({ type: "game", board: newBoard });
	        return "Invalid board.";
	    }
	    var coordinateFrom = star[0].toNumber();
	    var coordinateTo = star[1].toNumber();
	    var move = coordinateTo - coordinateFrom;
	    var checkJump = checkJump();
    	
	    if (board.substring(coordinateTo -1, coordinateTo) != "_") {
	       Avatar.setCommand({ type: "game", board: newBoard });
	       return "Invalid Move."
	    }
	    
	    if (player == "R") {
    	    if (board.substring(coordinateFrom - 1, coordinateFrom) != "R") {
    	       Avatar.setCommand({ type: "game", board: newBoard });
    	       return "You are R."
    	    }
    	    if (checkJump == false) {
        	    if (board.substring(coordinateFrom - 1, coordinateFrom).toSymbol() == "r".toSymbol()) {
        	       if (move != 7 && move != 9) {
        	           Avatar.setCommand({ type: "game", board: newBoard });
        	           return "Invalid Move.";
        	       }
        	    }
        	    if (board.substring(coordinateFrom - 1, coordinateFrom) == "R") {
        	       if (Math.abs(move) != 7 && Math.abs(move) != 9) {
        	           Avatar.setCommand({ type: "game", board: newBoard });
        	           return "Invalid move."
        	       }
        	    }
    	    }
    	    if (checkJump == true) {
    	        if (Math.abs(move) != 14 && move != 18) {
                    Avatar.setCommand({ type: "game", board: newBoard });
    	            return "You must make jump.";
                }
    	    }
	    }
	    if (player == "B") {
    	    if (board.substring(coordinateFrom - 1, coordinateFrom) != "B") {
    	       Avatar.setCommand({ type: "game", board: newBoard });
    	       return "You are B."
    	    }
    	    if (checkJump == false) {
        	    if (board.substring(coordinateFrom - 1, coordinateFrom).toSymbol() == "b".toSymbol()) {
        	       if (move != -7 && move != -9) {
        	           Avatar.setCommand({ type: "game", board: newBoard });
        	           return "Invalid Move.";
        	       }
        	    }
        	    if (board.substring(coordinateFrom - 1, coordinateFrom) == "B") {
        	       if (Math.abs(move) != 7 && Math.abs(move) != 9) {
        	           Avatar.setCommand({ type: "game", board: newBoard });
        	           return "Invalid move."
        	       }
        	    }
    	    }
    	    if (checkJump == true) {
    	        if (move != -14 && move != -18) {
                    Avatar.setCommand({ type: "game", board: newBoard });
    	            return "You must make jump.";
                }
    	    }
	    }
    	    newBoard = board.substring(0,coordinateTo - 1) + board.substring(coordinateFrom - 1, coordinateFrom) + board.substring(coordinateTo, 64);
    	    newBoard = newBoard.substring(0,coordinateFrom - 1) + "_" + newBoard.substring(coordinateFrom, 64);
    	    if (checkJump == true) {
                var jumpedPiece = (coordinateTo + coordinateFrom) / 2;
    	        newBoard = newBoard.substring(0,jumpedPiece - 1) + "_" + newBoard.substring(jumpedPiece, 64);
    	        checkJump = checkJump();
    	    }
    	    if (checkQueen() == true) {
    	        newBoard = newBoard.substring(0,coordinateTo - 1) + player + newBoard.substring(coordinateTo, 64);
    	    }
        conversation.board = newBoard;
        conversation.playerBoard = newBoard;
        conversation.append(#playerBoards, newBoard);
        if (checkGameOver() == false) {
            endGame();
            Avatar.setCommand({ type: "game", board: newBoard });
            return "You win. Want to play again? <button>Yes</button> <button>No</button>";
        }
        if (checkJump == true) {
            Avatar.setCommand({ type: "game", board: newBoard });
            return "You can jump again."
        }
        makeMove();
	}
	
    // Bot makes a move.
	function makeMove() {
        turn = bot;
        var moveFrom = null;
	    var moveTo = null;
        board = conversation.board;
        checkJump = checkJump();
        if (checkJump == false) {
            randomMove();
        }
        if (moveTo == null) {
            endGame();
            Avatar.setCommand({ type: "game", board: newBoard });
            return "You win. Want to play again? <button>Yes</button> <button>No</button>";
        }

        if (moveTo != null) {
            // Move
            newBoard = board.substring(0,moveTo) + board.substring(moveFrom, moveFrom + 1) + board.substring(moveTo + 1, 64);
    	    newBoard = newBoard.substring(0,moveFrom) + "_" + newBoard.substring(moveFrom + 1, 64);
            if (checkJump == true) {
                var jumpedPiece = (moveTo + moveFrom) / 2;
    	        newBoard = newBoard.substring(0,jumpedPiece) + "_" + newBoard.substring(jumpedPiece + 1, 64);
    	        checkJump = checkJump();
    	    }
    	    if (checkQueen() == true) {
    	        newBoard = newBoard.substring(0,moveTo) + bot + newBoard.substring(moveTo + 1, 64);
    	    }
            conversation.board = newBoard;
            conversation.append(#boards, newBoard);
            if (checkGameOver() == true) {
                endGame();
                Avatar.setCommand({ type: "game", board: newBoard });
                return "I win. Want to play again? <button>Yes</button> <button>No</button>";
            }
            if (checkJump == true) {
                makeMove();
    	    }
        	Avatar.setCommand({ type: "game", board: newBoard });
            return "Your move.";
        }
	}
	
	function randomMove() {
	    var validPieces = new Array();
	    var validMoves = new Array();
	    var bot = conversation.bot;
	    for (var count = 0; count < 64; count++) {
	        if (bot == "B" && newBoard.substring(count, count + 1) == "b") {
                if (newBoard.substring(count - 9, count - 8) == "_" || newBoard.substring(count - 7, count - 6) == "_") {
                    validPieces.add(count);
                }
                if (newBoard.substring(count, count + 1) == "B") {
                    if (newBoard.substring(count + 9, count + 10) == "_" || newBoard.substring(count + 7, count + 8) == "_") {
                        validPieces.add(count);
                    }
    	        }
	        }
	        if (bot == "R" && newBoard.substring(count, count + 1) == "r") {
                if (newBoard.substring(count + 9, count + 10) == "_" || newBoard.substring(count + 7, count + 8) == "_") {
                    validPieces.add(count);
                }
                if (newBoard.substring(count, count + 1) == "R") {
                    if (newBoard.substring(count - 9, count - 8) == "_" || newBoard.substring(count - 7, count - 6) == "_") {
                        validPieces.add(count);
                    }
    	        }
	        }
	    }
	    moveFrom = validPieces.random(#element);
	    if (bot == "B") {
    	    if (newBoard.substring(moveFrom - 9, moveFrom - 8) == "_") {
                validMoves.add(moveFrom - 9);
            }
            if (newBoard.substring(moveFrom - 7, moveFrom - 6) == "_") {
                validMoves.add(moveFrom - 7);
            }
            if (newBoard.substring(moveFrom, moveFrom + 1).toSymbol() == "B".toSymbol()) {
                if (newBoard.substring(moveFrom + 9, moveFrom + 10) == "_") {
                    validMoves.add(moveFrom + 9);
                }
                if (newBoard.substring(moveFrom + 7, moveFrom + 8) == "_") {
                    validMoves.add(moveFrom + 7);
                }
	        }
	    }
	    if (bot == "R") {
    	    if (newBoard.substring(moveFrom + 9, moveFrom + 10) == "_") {
                validMoves.add(moveFrom + 9);
            }
            if (newBoard.substring(moveFrom + 7, moveFrom + 8) == "_") {
                validMoves.add(moveFrom + 7);
            }
            if (newBoard.substring(moveFrom, moveFrom + 1).toSymbol() == "R".toSymbol()) {
                if (newBoard.substring(moveFrom - 9, moveFrom - 8) == "_") {
                    validMoves.add(moveFrom - 9);
                }
                if (newBoard.substring(moveFrom - 7, moveFrom - 6) == "_") {
                    validMoves.add(moveFrom - 7);
                }
	        }
	    }
        moveTo = validMoves.random(#element);
	    return moveFrom + moveTo;
	}
	
    // Checks if someone has won.
    function checkGameOver() {
        var win = conversation.bot == "R";
        var over = true;
        for (var count = 0; count < 64; count++) {
	        if (newBoard.substring(count, count + 1) == "R") {
                over = false;
	        }
	    }
	    if (over == true) {
            return !win;
        }
	    over = true;
	    for (var count = 0; count < 64; count++) {
	        if (newBoard.substring(count, count + 1) == "B") {
                over = false;
	        }
	    }
	    if (over == true) {
            return win;
        }
	    return null;
    }
    
    function checkJump() {
        if (turn == player) {
            for (var count = 0; count < 64; count++) {
    	        if (player == "R" && newBoard.substring(count, count + 1) == "r") {
                    if (newBoard.substring(count + 7, count + 8) == "b" && newBoard.substring(count + 14, count + 15) == "_") {
                        return true;
                    }
                    if (newBoard.substring(count + 9, count + 10) == "b" && newBoard.substring(count + 18, count + 19) == "_") {
                        return true;
                    }
                    if (newBoard.substring(count, count + 1).toSymbol() == "R".toSymbol()) {
                        if (newBoard.substring(count - 7, count - 6) == "b" && newBoard.substring(count - 14, count - 13) == "_") {
                            return true;
                        }
                        if (newBoard.substring(count - 9, count - 8) == "b" && newBoard.substring(count - 18, count - 17) == "_") {
                            return true;
                        }
    	            }
    	        }
    	        if (player == "B" && newBoard.substring(count, count + 1) == "b") {
                    if (newBoard.substring(count - 7, count - 6) == "r" && newBoard.substring(count - 14, count - 13) == "_") {
                        return true;
                    }
                    if (newBoard.substring(count - 9, count - 8) == "r" && newBoard.substring(count - 18, count - 17) == "_") {
                        return true;
                    }
                    if (newBoard.substring(count, count + 1).toSymbol() == "B".toSymbol()) {
                        if (newBoard.substring(count + 7, count + 8) == "r" && newBoard.substring(count + 14, count + 15) == "_") {
                            return true;
                        }
                        if (newBoard.substring(count + 9, count + 10) == "r" && newBoard.substring(count + 18, count + 19) == "_") {
                            return true;
                        }
    	            }
    	        }
    	        
    	    }
        }
        if (turn == bot) {
            for (var count = 0; count < 64; count++) {
    	        if (bot == "B" && newBoard.substring(count, count + 1) == "b") {
                    if (newBoard.substring(count - 7, count - 6) == "r" && newBoard.substring(count - 14, count - 13) == "_") {
                        moveFrom = count;
                        moveTo = count - 14;
                        return true;
                    }
                    if (newBoard.substring(count - 9, count - 8) == "r" && newBoard.substring(count - 18, count - 17) == "_") {
                        moveFrom = count;
                        moveTo = count - 18;
                        return true;
                    }
                    if (newBoard.substring(count, count + 1).toSymbol() == "B".toSymbol()) {
                        if (newBoard.substring(count + 7, count + 8) == "r" && newBoard.substring(count + 14, count + 15) == "_") {
                            moveFrom = count;
                            moveTo = count + 14;
                            return true;
                        }
                        if (newBoard.substring(count + 9, count + 10) == "r" && newBoard.substring(count + 18, count + 19) == "_") {
                            moveFrom = count;
                            moveTo = count + 18;
                            return true;
                        }
        	        }
    	        }
    	        if (bot == "R" && newBoard.substring(count, count + 1) == "r") {
                    if (newBoard.substring(count + 7, count + 8) == "b" && newBoard.substring(count + 14, count + 15) == "_") {
                        moveFrom = count;
                        moveTo = count + 14;
                        return true;
                    }
                    if (newBoard.substring(count + 9, count + 10) == "b" && newBoard.substring(count + 18, count + 19) == "_") {
                        moveFrom = count;
                        moveTo = count + 18;
                        return true;
                    }
                    if (newBoard.substring(count, count + 1).toSymbol() == "R".toSymbol()) {
                        if (newBoard.substring(count - 7, count - 6) == "b" && newBoard.substring(count - 14, count - 13) == "_") {
                            moveFrom = count;
                            moveTo = count - 14;
                            return true;
                        }
                        if (newBoard.substring(count - 9, count - 8) == "b" && newBoard.substring(count - 18, count - 17) == "_") {
                            moveFrom = count;
                            moveTo = count - 18;
                            return true;
                        }
        	        }
    	        }
    	    }
        }
        return false;
    }
    
    function checkQueen() {
        for (var count = 0; count < 8; count++) {
	        if (turn == "B") {
    	        if (newBoard.substring(count, count + 1).toSymbol() == "b".toSymbol()) {
                    return true;
    	        } 
	        }
	    }
	    for (var count = 56; count < 64; count++) {
	        if (turn == "R") {
    	        if (newBoard.substring(count, count + 1).toSymbol() == "r".toSymbol()) {
                    return true;
    	        }
	        }
	    }
	    return newBoard;
    }
    
    function endGame() {
        conversation.topic = "Checkers playAgain";
	    conversation.boards = null;
	    conversation.playerBoards = null;
    }
}

