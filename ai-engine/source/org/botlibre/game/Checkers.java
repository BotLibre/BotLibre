/******************************************************************************
 *
 *  Copyright 2020 Paphus Solutions Inc.
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

package org.botlibre.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import org.botlibre.util.Utils;

/**
 * Checkers game. This class let's an AI player play another AI player using
 * various different strategies. Use the Checkers class to play or train. The
 * CheckersGame class keeps track of the state of the game. The Analytic class
 * is used for training the analytic network.
 * 
 * 
 * Analytic network input and output formatting:
 * 
 * The analytic will make a move for black. The board and pieces will have to be
 * flipped if the bot plays from the red side.
 * 
 * Input format:
 * 
 * The input consists of 32 numbers from -1.0 to 1.0. 0.5 means a black man,
 * -0.5 means a red man, 1.0 means a black king, -1.0 means a red king. The
 * input is taken by going left-to-right from the top row (red side) to the
 * bottom row (black side) of the board, skipping the unreachable squares.
 * 
 * Output format:
 * 
 * The output consists of 128 numbers from -1.0 to 1.0. Higher values represent
 * preferred moves.
 * 
 * Directions to move the piece: 0-31: bottom right; 32-63: the bottom left;
 * 64-95: top right; 96-127: top left.
 * 
 * The position of the piece to be moved is represented by the value modulo 32.
 * The number for the position is taken by going left-to-right from the top row
 * (red side) to the bottom row (black side) of the board, skipping the
 * unreachable squares.
 * 
 */
public class Checkers {
	public static void main(String[] args) throws Exception {
		// Edit these details as necessary:
		final String[] apiDetails = new String[] { "http://www.botlibre.com/rest/api", "admin",
				"password", "2477650835389014032", "13" };

		// Some strategies you may want to use:
		final Strategy randomStrategy = new Strategy.RandomStrategy();
		final Strategy randomLookAheadStrategy = new Strategy.RandomLookAheadStrategy();
		final Strategy remoteDeepLearningStrategy = new Strategy.RemoteDeepLearningStrategy(apiDetails);
//		final Strategy combineRandomAndDeepLearning = new Strategy.randomCombinationStrategy(randomStrategy, 0.1,
//				remoteDeepLearningStrategy, 0.9);

		// Trainer for the analytic network
		final Analytic trainer = new Analytic(apiDetails);

		// Reset the analytic network (THIS WILL PERMANENTLY RESET THE NETWORK)
//		trainer.reset();

		// Learn some games (THIS WILL PERMANENTLY CHANGE THE NETWORK)
//		showResults(learnGames(5000, randomLookAheadStrategy, randomStrategy, trainer));
//		showResults(learnGames(5000, randomStrategy, randomLookAheadStrategy, trainer));
//		showResults(learnGames(500, randomLookAheadStrategy, randomLookAheadStrategy, trainer));
//		showResults(learnGames(500, randomLookAheadStrategy, randomLookAheadStrategy, trainer));

		// Play some games
//		showResults(playGames(100, remoteDeepLearningStrategy, randomStrategy));
//		showResults(playGames(100, randomStrategy, remoteDeepLearningStrategy));
//		showResults(playGames(100, remoteDeepLearningStrategy, randomLookAheadStrategy));
//		showResults(playGames(100, randomLookAheadStrategy, remoteDeepLearningStrategy));
	}

	// Print the results of learnGames() or playGames()
	public static void showResults(int[] results) {
		System.out.println("Games played: " + Integer.toString(results[0]));
		System.out.println("Red wins: " + Integer.toString(results[1]));
		System.out.println("Black wins: " + Integer.toString(results[2]));
	}

	// Play the two strategies against each other.
	public static int[] playGames(int iterations, Strategy redStrategy, Strategy blackStrategy) throws Exception {
		int[] results = new int[] { 0, 0, 0 };
		for (int i = 0; i < iterations; i++) {
			if (i % 20 == 0) {
				System.out.println(Integer.toString(i) + " games played...");
			}
			CheckersGame.Player winner = playGame(redStrategy, blackStrategy);
			results[0] += 1;
			if (winner == CheckersGame.Player.RED) {
				results[1] += 1;
			} else if (winner == CheckersGame.Player.BLACK) {
				results[2] += 1;
			}
		}
		return results;
	}

	public static CheckersGame.Player playGame(Strategy redStrategy, Strategy blackStrategy) throws Exception {
		return playGame(redStrategy, blackStrategy, new CheckersGame.Board());
	}

	public static CheckersGame.Player playGame(Strategy redStrategy, Strategy blackStrategy, CheckersGame.Board board)
			throws Exception {
		int movesSinceLastInterestingMove = 0;
		while (!board.gameOver) {
//				board.printBoard();
			CheckersGame.Move move = null;
			if (board.playerThisTurn == CheckersGame.Player.RED) {
				move = redStrategy.getMove(board);
			} else if (board.playerThisTurn == CheckersGame.Player.BLACK) {
				move = blackStrategy.getMove(board);
			}
			board = board.applyMove(move);
			if (!move.jump && !move.queen) {
				movesSinceLastInterestingMove += 1;
				if (movesSinceLastInterestingMove >= 40) {
					// 40 move rule
					break;
				}
			} else {
				movesSinceLastInterestingMove = 0;
			}
		}
//			board.printBoard();
//			System.out.println("Game over.");
		return board.winner;
	}

	// Train the network by playing the two strategies against each other.
	// It will learn from any winning game.
	public static int[] learnGames(int iterations, Strategy redStrategy, Strategy blackStrategy, Analytic trainer)
			throws Exception {
		int[] results = new int[] { 0, 0, 0 };
		for (int i = 0; i < iterations; i++) {
			if (i % 200 == 0) {
				System.out.println(Integer.toString(i) + " games learned...");
			}
			CheckersGame.Player winner = learnGame(redStrategy, blackStrategy, trainer);
			results[0] += 1;
			if (winner == CheckersGame.Player.RED) {
				results[1] += 1;
			} else if (winner == CheckersGame.Player.BLACK) {
				results[2] += 1;
			}
		}
		trainer.flushBuffer();
		return results;
	}

	public static CheckersGame.Player learnGame(Strategy redStrategy, Strategy blackStrategy, Analytic trainer)
			throws Exception {
		return learnGame(redStrategy, blackStrategy, trainer, new CheckersGame.Board());
	}

	public static CheckersGame.Player learnGame(Strategy redStrategy, Strategy blackStrategy, Analytic trainer,
			CheckersGame.Board board) throws Exception {
		ArrayList<CheckersGame.Board> redBoards = new ArrayList<>();
		ArrayList<CheckersGame.Move> redMoves = new ArrayList<>();
		ArrayList<CheckersGame.Board> blackBoards = new ArrayList<>();
		ArrayList<CheckersGame.Move> blackMoves = new ArrayList<>();
		int movesSinceLastInterestingMove = 0;
		while (!board.gameOver) {
			CheckersGame.Move move = null;
			if (board.playerThisTurn == CheckersGame.Player.RED) {
				move = redStrategy.getMove(board);
				redBoards.add(board);
				redMoves.add(move);
			} else if (board.playerThisTurn == CheckersGame.Player.BLACK) {
				move = blackStrategy.getMove(board);
				blackBoards.add(board);
				blackMoves.add(move);
			}
			board = board.applyMove(move);
			if (!move.jump && !move.queen) {
				movesSinceLastInterestingMove += 1;
				if (movesSinceLastInterestingMove >= 40) {
					// no captures or queens in last 40 moves means game is drawn
					break;
				}
			} else {
				movesSinceLastInterestingMove = 0;
			}
		}
		Iterator<CheckersGame.Board> boardIterator = null;
		Iterator<CheckersGame.Move> moveIterator = null;
		if (board.winner == CheckersGame.Player.RED) {
			boardIterator = redBoards.iterator();
			moveIterator = redMoves.iterator();
		} else if (board.winner == CheckersGame.Player.BLACK) {
			boardIterator = blackBoards.iterator();
			moveIterator = blackMoves.iterator();
		} else {
//			boardIterator = blackBoards.iterator();
//			moveIterator = blackMoves.iterator();
//			boardIterator = redBoards.iterator();
//			moveIterator = redMoves.iterator();
		}
		if (boardIterator != null && moveIterator != null) {
			while (boardIterator.hasNext() && moveIterator.hasNext()) {
//				trainer.train(boardIterator.next(), moveIterator.next());
				CheckersGame.Board nextBoard = boardIterator.next();
				CheckersGame.Move nextMove = moveIterator.next();
//				nextBoard.printBoard();
//				System.out.println(Integer.toString(nextMove.move[0]) + " " + Integer.toString(nextMove.move[1]));
				trainer.train(nextBoard, nextMove);
			}
		}
		return board.winner;
	}

}

// Class for the tracking the state of a checkers game.
class CheckersGame {
	public enum Player {
		RED, BLACK
	}

	public static Player oppositePlayer(Player player) {
		if (player == Player.RED) {
			return Player.BLACK;
		} else if (player == Player.BLACK) {
			return player.RED;
		} else {
			return null;
		}
	}

	// Class for moves. These can be "applied" to a Board instance.
	public static class Move {
		public final int[] move;
		public final Player playerThisTurn;
		public final boolean jump;
		public final boolean queen;
		public final int indexOfPieceCaptured;

		public Move(int[] move, Player playerThisTurn, boolean jump, Board board) {
			this(move, playerThisTurn, jump, board.checkKing(move));
		}

		public Move(int[] move, Player playerThisTurn, boolean jump, boolean queen) {
			this(move, playerThisTurn, jump, queen, (jump ? indexOfPieceCaptured(move) : -1));
		}

		public Move(int[] move, Player playerThisTurn, boolean jump, boolean queen, int indexOfPieceCaptured) {
			this.move = move;
			this.playerThisTurn = playerThisTurn;
			this.jump = jump;
			this.queen = queen;
			this.indexOfPieceCaptured = indexOfPieceCaptured;
		}

		public static int indexOfPieceCaptured(int[] move) {
			if (move[1] - move[0] == 18 || move[0] - move[1] == 18) {
				if (move[1] > move[0]) {
					return move[0] + 9;
				} else {
					return move[1] + 9;
				}
			} else if (move[1] - move[0] == 14 || move[0] - move[1] == 14) {
				if (move[1] > move[0]) {
					return move[0] + 7;
				} else {
					return move[1] + 7;
				}
			} else {
				return -1;
			}
		}

		// Flip the move to the perspective of the opposite player
		public Move rotateMove() {
			return new Move(new int[] { 63 - move[0], 63 - move[1] }, oppositePlayer(this.playerThisTurn), jump, queen,
					(indexOfPieceCaptured == -1) ? -1 : 63 - indexOfPieceCaptured);
		}

		public boolean equivalentTo(int[] move) {
			return this.move[0] == move[0] && this.move[1] == move[1];
		}

		public boolean identicalAs(Move x) {
			if (x == null) {
				return false;
			}
			return move[0] == x.move[0] && move[1] == x.move[1] && playerThisTurn == x.playerThisTurn && jump == x.jump
					&& queen == x.queen && indexOfPieceCaptured == x.indexOfPieceCaptured;
		}
	}

	// Class for the state of the board
	public static class Board {
		public char[] board;
		public Player playerThisTurn;
		boolean gameOver = false;
		Player winner;
		public Move[] legalMoves;

		public Board() {
			this("/r/r/r/rr/r/r/r//r/r/r/r_/_/_/_//_/_/_/_b/b/b/b//b/b/b/bb/b/b/b/", Player.RED);
		}

		public Board(String board, Player playerThisTurn) {
			this(board.toCharArray(), playerThisTurn);
		}

		public Board(char[] board, Player playerThisTurn) {
			this.board = board;
			this.playerThisTurn = playerThisTurn;
			updateLegalMoves();
		}

		private Board(char[] board, Player playerThisTurn, boolean gameOver, Player winner, Move[] legalMoves) {
			this.board = board;
			this.playerThisTurn = playerThisTurn;
			this.gameOver = gameOver;
			this.winner = winner;
			this.legalMoves = legalMoves;
		}

		// Rotate the board to the perspective of the other player.
		// Flips the pieces too.
		public Board rotateBoard() {
			char[] rotatedBoard = new char[64];
			for (int i = 0; i < 64; i++) {
				char originalPiece = board[63 - i];
				if (originalPiece == '_') {
					rotatedBoard[i] = '_';
				} else if (originalPiece == '/') {
					rotatedBoard[i] = '/';
				} else if (originalPiece == 'r') {
					rotatedBoard[i] = 'b';
				} else if (originalPiece == 'b') {
					rotatedBoard[i] = 'r';
				} else if (originalPiece == 'R') {
					rotatedBoard[i] = 'B';
				} else if (originalPiece == 'B') {
					rotatedBoard[i] = 'R';
				}
			}
			Move[] rotatedLegalMoves = null;
			if (legalMoves != null) {
				rotatedLegalMoves = new Move[legalMoves.length];
				for (int i = 0; i < legalMoves.length; i++) {
					Move rotatedMove = new Move(new int[] { 63 - legalMoves[i].move[0], 63 - legalMoves[i].move[1] },
							oppositePlayer(playerThisTurn), legalMoves[i].jump, legalMoves[i].queen);
					rotatedLegalMoves[i] = rotatedMove;
				}
			}
			Player rotatedWinner;
			if (winner == null) {
				rotatedWinner = null;
			} else {
				rotatedWinner = Player.BLACK;
			}
			return new Board(rotatedBoard, Player.BLACK, gameOver, rotatedWinner, rotatedLegalMoves);
		}

		public void printBoard() {
			String boardString = new String(board);
			System.out.println("");
			System.out.println(boardString.substring(0, 8));
			System.out.println(boardString.substring(8, 16));
			System.out.println(boardString.substring(16, 24));
			System.out.println(boardString.substring(24, 32));
			System.out.println(boardString.substring(32, 40));
			System.out.println(boardString.substring(40, 48));
			System.out.println(boardString.substring(48, 56));
			System.out.println(boardString.substring(56, 64));
		}

		// Checks if a move is legal
		public boolean isLegalMove(Move m) {
			if (m == null)
				return false;
			for (Move lm : legalMoves) {
				if (m.equals(lm) || m.identicalAs(lm)) {
					return true;
				}
			}
			return false;
		}

		// Convert a move described as indexes to a Move object.
		// Will only select a move by looking through legalMoves.
		public Move indexesToLegalMove(int[] move) {
			for (Move lm : legalMoves) {
				if (lm.equivalentTo(move)) {
					return lm;
				}
			}
			return null;
		}

		// Applies a Move to the current board, and return the new board.
		// Does not alter the current instance of the board.
		public Board applyMove(Move m) {
			char piece = board[m.move[0]];
			char[] newBoardArray = Arrays.copyOf(board, 64);
			newBoardArray[m.move[0]] = '_';
			if (m.jump) {
				newBoardArray[m.indexOfPieceCaptured] = '_';
			}
			if (m.queen) {
				if (piece == 'r') {
					newBoardArray[m.move[1]] = 'R';
				} else if (piece == 'b') {
					newBoardArray[m.move[1]] = 'B';
				}
			} else {
				newBoardArray[m.move[1]] = piece;
			}

			Board newBoard = new Board(newBoardArray, playerThisTurn, gameOver, winner, null);

			if (m.jump && !m.queen) {
				newBoard.updateJumpAgain(m.move[1]);
				if (newBoard.legalMoves != null) {
					return newBoard;
				}
			}

			if (playerThisTurn == Player.RED) {
				newBoard.playerThisTurn = Player.BLACK;
			} else if (playerThisTurn == Player.BLACK) {
				newBoard.playerThisTurn = Player.RED;
			}
			newBoard.updateLegalMoves();
			return newBoard;
		}

		// Update legalMoves by checking if a piece at the given location can jump
		// again.
		// Useful for checking if another jump is forced if the previous move was a
		// jump.
		private void updateJumpAgain(int index) {
			ArrayList<int[]> jumpAgainMovesList = new ArrayList<int[]>();
			if (playerThisTurn == Player.RED) {
				if (index + 14 < 64 && board[index + 14] == '_'
						&& (board[index + 7] == 'b' || board[index + 7] == 'B')) {
					jumpAgainMovesList.add(new int[] { index, index + 14 });
				}
				if (index + 18 < 64 && board[index + 18] == '_'
						&& (board[index + 9] == 'b' || board[index + 9] == 'B')) {
					jumpAgainMovesList.add(new int[] { index, index + 18 });
				}
				if (board[index] == 'R') {
					if (index - 14 > 0 && board[index - 14] == '_'
							&& (board[index - 7] == 'b' || board[index - 7] == 'B')) {
						jumpAgainMovesList.add(new int[] { index, index - 14 });
					}
					if (index - 18 > 0 && board[index - 18] == '_'
							&& (board[index - 9] == 'b' || board[index - 9] == 'B')) {
						jumpAgainMovesList.add(new int[] { index, index - 18 });
					}
				}
			} else if (playerThisTurn == Player.BLACK) {
				if (index - 14 > 0 && board[index - 14] == '_'
						&& (board[index - 7] == 'r' || board[index - 7] == 'R')) {
					jumpAgainMovesList.add(new int[] { index, index - 14 });
				}
				if (index - 18 > 0 && board[index - 18] == '_'
						&& (board[index - 9] == 'r' || board[index - 9] == 'R')) {
					jumpAgainMovesList.add(new int[] { index, index - 18 });
				}
				if (board[index] == 'R') {
					if (index + 14 < 64 && board[index + 14] == '_'
							&& (board[index + 7] == 'r' || board[index + 7] == 'R')) {
						jumpAgainMovesList.add(new int[] { index, index + 14 });
					}
					if (index + 18 < 64 && board[index + 18] == '_'
							&& (board[index + 9] == 'r' || board[index + 9] == 'R')) {
						jumpAgainMovesList.add(new int[] { index, index + 18 });
					}
				}
			}

			if (jumpAgainMovesList.isEmpty()) {
				legalMoves = null;
			} else {
				legalMoves = new Move[jumpAgainMovesList.size()];
				int i = 0;
				for (int[] move : jumpAgainMovesList) {
					legalMoves[i] = createMove(move, true);
					i++;
				}
			}
		}

		// Convert a move from indexes to a Move object.
		private Move createMove(int[] move, boolean jump) {
			return new Move(move, playerThisTurn, jump, checkKing(move));
		}

		// Update legalMoves from the current state of the board.
		// Assumes that we are not trying to jump again (use updateJumpAgain for that).
		private void updateLegalMoves() {
			updateLegalJumpMoves();
			if (legalMoves == null) {
				updateLegalSimpleMoves();
			}
			if (legalMoves == null) {
				gameOver = true;
				if (playerThisTurn == Player.RED) {
					winner = Player.BLACK;
				} else if (playerThisTurn == Player.BLACK) {
					winner = Player.RED;
				}
			}
		}

		private void updateLegalJumpMoves() {
			ArrayList<int[]> legalJumpMovesList = new ArrayList<int[]>();
			for (int index = 0; index < 64; index++) {
				char curPiece = board[index];
				if (curPiece == '/') {
					continue;
				} else if (playerThisTurn == Player.RED && (curPiece == 'b' || curPiece == 'B')) {
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'r')
							&& (this.board[index + 9] == '_')) {
						legalJumpMovesList.add(new int[] { index - 9, index + 9 });
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'r')
							&& (this.board[index + 7] == '_')) {
						legalJumpMovesList.add(new int[] { index - 7, index + 7 });
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'R')
							&& (this.board[index + 9] == '_')) {
						legalJumpMovesList.add(new int[] { index - 9, index + 9 });
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'R')
							&& (this.board[index + 7] == '_')) {
						legalJumpMovesList.add(new int[] { index - 7, index + 7 });
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index + 9] == 'R')
							&& (this.board[index - 9] == '_')) {
						legalJumpMovesList.add(new int[] { index + 9, index - 9 });
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index + 7] == 'R')
							&& (this.board[index - 7] == '_')) {
						legalJumpMovesList.add(new int[] { index + 7, index - 7 });
					}
				} else if (playerThisTurn == Player.BLACK && (curPiece == 'r' || curPiece == 'R')) {
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == '_')) {
						if ((this.board[index + 9] == 'b') || (this.board[index + 9] == 'B')) {
							legalJumpMovesList.add(new int[] { index + 9, index - 9 });
						}
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == '_')) {
						if ((this.board[index + 7] == 'b') || (this.board[index + 7] == 'B')) {
							legalJumpMovesList.add(new int[] { index + 7, index - 7 });
						}
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'B')
							&& (this.board[index + 9] == '_')) {
						legalJumpMovesList.add(new int[] { index - 9, index + 9 });
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'B')
							&& (this.board[index + 7] == '_')) {
						legalJumpMovesList.add(new int[] { index - 7, index + 7 });
					}
				}
			}
			if (legalJumpMovesList.isEmpty()) {
				legalMoves = null;
			} else {
				legalMoves = new Move[legalJumpMovesList.size()];
				int i = 0;
				for (int[] move : legalJumpMovesList) {
					legalMoves[i] = createMove(move, true);
					i++;
				}
			}
		}

		private void updateLegalSimpleMoves() {
			ArrayList<int[]> legalSimpleMovesList = new ArrayList<int[]>();
			for (int index = 0; index < 64; index++) {
				if (this.board[index] == '_') {
					if (playerThisTurn == Player.RED) {
						if ((0 < index - 9) && (this.board[index - 9] == 'r')) {
							legalSimpleMovesList.add(new int[] { index - 9, index });
						}
						if ((0 < index - 7) && (this.board[index - 7] == 'r')) {
							legalSimpleMovesList.add(new int[] { index - 7, index });
						}
						if ((0 < index - 9) && (this.board[index - 9] == 'R')) {
							legalSimpleMovesList.add(new int[] { index - 9, index });
						}
						if ((0 < index - 7) && (this.board[index - 7] == 'R')) {
							legalSimpleMovesList.add(new int[] { index - 7, index });
						}
						if ((index + 9 < 64) && (this.board[index + 9] == 'R')) {
							legalSimpleMovesList.add(new int[] { index + 9, index });
						}
						if ((index + 7 < 64) && (this.board[index + 7] == 'R')) {
							legalSimpleMovesList.add(new int[] { index + 7, index });
						}
					} else if (playerThisTurn == Player.BLACK) {
						if ((index + 9 < 64) && (this.board[index + 9] == 'b')) {
							legalSimpleMovesList.add(new int[] { index + 9, index });
						}
						if ((index + 7 < 64) && (this.board[index + 7] == 'b')) {
							legalSimpleMovesList.add(new int[] { index + 7, index });
						}
						if ((index + 9 < 64) && (this.board[index + 9] == 'B')) {
							legalSimpleMovesList.add(new int[] { index + 9, index });
						}
						if ((index + 7 < 64) && (this.board[index + 7] == 'B')) {
							legalSimpleMovesList.add(new int[] { index + 7, index });
						}
						if ((0 < index - 9) && (this.board[index - 9] == 'B')) {
							legalSimpleMovesList.add(new int[] { index - 9, index });
						}
						if ((0 < index - 7) && (this.board[index - 7] == 'B')) {
							legalSimpleMovesList.add(new int[] { index - 7, index });
						}
					}
				}
			}

			if (legalSimpleMovesList.isEmpty()) {
				legalMoves = null;
			} else {
				legalMoves = new Move[legalSimpleMovesList.size()];
				int i = 0;
				for (int[] move : legalSimpleMovesList) {
					legalMoves[i] = createMove(move, false);
					i++;
				}
			}
		}

		// Check if the move as the effect of turning a piece into a king.
		private boolean checkKing(int[] move) {
			if (playerThisTurn == Player.RED && board[move[0]] == 'r' && move[1] > 55) {
				return true;
			} else if (playerThisTurn == Player.BLACK && board[move[0]] == 'b' && move[1] < 8) {
				return true;
			} else {
				return false;
			}
		}

	}
}

// Class for strategies used when playing games
abstract class Strategy {
	public abstract CheckersGame.Move getMove(CheckersGame.Board board) throws Exception;

	// Strategy for manually inputting moves
	public static class HumanInputStrategy extends Strategy {
		Scanner inputScanner = new Scanner(System.in);

		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) {
			int fromIndex;
			int toIndex;
			CheckersGame.Move m = null;
			while (m == null || !board.isLegalMove(m)) {
				System.out.print("Your move: ");
				fromIndex = inputScanner.nextInt();
				toIndex = inputScanner.nextInt();
				System.out.println();
				m = board.indexesToLegalMove(new int[] { fromIndex, toIndex });
			}
			return m;
		}
	}

	// Strategy for playing the first legal move
	public static class FirstMoveStrategy extends Strategy {
		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) {
			return board.legalMoves[0];
		}
	}

	// Strategy for playing a random legal move
	public static class RandomStrategy extends Strategy {
		Random rng = new Random();

		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) {
			return board.legalMoves[rng.nextInt(board.legalMoves.length)];
		}
	}

	// Strategy that plays randomly, prefers to win, avoids being captured, prefers
	// to king a piece
	public static class RandomLookAheadStrategy extends Strategy {
		Random rng = new Random();

		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) {
			ArrayList<CheckersGame.Move> goodMoves = new ArrayList<>();
			for (CheckersGame.Move move : board.legalMoves) {
				CheckersGame.Board opponentBoard = board.applyMove(move);
				if (opponentBoard.legalMoves == null) {
					goodMoves.add(move);
					break;
				}
			}
			if (goodMoves.isEmpty()) {
				for (CheckersGame.Move move : board.legalMoves) {
					if (move.jump) {
						goodMoves.add(move);
					}
				}
			}
			if (goodMoves.isEmpty()) {
				for (CheckersGame.Move move : board.legalMoves) {
					CheckersGame.Board opponentBoard = board.applyMove(move);
					boolean opponentCanJump = false;
					for (CheckersGame.Move opponentMove : opponentBoard.legalMoves) {
						if (opponentMove.jump) {
							opponentCanJump = true;
							break;
						}
					}
					if (!opponentCanJump) {
						goodMoves.add(move);
					}
				}
			}
			if (goodMoves.isEmpty()) {
				for (CheckersGame.Move move : board.legalMoves) {
					if (move.queen) {
						goodMoves.add(move);
					}
				}
			}
			if (goodMoves.isEmpty()) {
				return board.legalMoves[rng.nextInt(board.legalMoves.length)];
			}
			return goodMoves.get(rng.nextInt(goodMoves.size()));
		}
	}

	// Strategy for getting a move from the remote analytic
	// Perhaps move some of the methods to the Analytic class?
	public static class RemoteDeepLearningStrategy extends Strategy {
		private String apiUrl;
		private String xmlAnalyticStartTag;
		private String xmlAnalyticEndTag = "</analytic>";
		private String xmlInputStartTag = "<input>";
		private String xmlInputEndTag = "</input>";
		private String xmlResultStartTag = "<data-analytic-result>";
		private String xmlResultEndTag = "</data-analytic-result>";
		private String xmlOutputStartTag = "<output>";
		private String xmlOutputEndTag = "</output>";

		private static int[] networkOutputConversionMap = new int[] { -1, 0, -1, 1, -1, 2, -1, 3, 4, -1, 5, -1, 6, -1,
				7, -1, -1, 8, -1, 9, -1, 10, -1, 11, 12, -1, 13, -1, 14, -1, 15, -1, -1, 16, -1, 17, -1, 18, -1, 19, 20,
				-1, 21, -1, 22, -1, 23, -1, -1, 24, -1, 25, -1, 26, -1, 27, 28, -1, 29, -1, 30, -1, 31, -1 };

		public RemoteDeepLearningStrategy(String[] apiDetails) {
			this(apiDetails[0], apiDetails[1], apiDetails[2], apiDetails[3], apiDetails[4]);
		}

		public RemoteDeepLearningStrategy(String apiUrl, String user, String password, String applicationId,
				String instanceId) {
			this.apiUrl = apiUrl;
			xmlAnalyticStartTag = "<analytic application='" + applicationId + "' user='" + user + "' password='"
					+ password + "' id='" + instanceId + "'>";
		}

		private double[] getNetworkOutput(double[] networkInput) throws Exception {
			StringBuilder analyticSB = new StringBuilder();
			analyticSB.append(xmlAnalyticStartTag);
			analyticSB.append(xmlInputStartTag);
			analyticSB.append(networkInput[0]);
			for (int i = 1; i < networkInput.length; i++) {
				analyticSB.append(',');
				analyticSB.append(networkInput[i]);
			}
			analyticSB.append(xmlInputEndTag);
			analyticSB.append(xmlAnalyticEndTag);
			String outputString = Utils.httpPOST(apiUrl + "/test-data-analytic", "application/xml",
					analyticSB.toString());
//			System.out.println(analyticSB.toString());
//			System.out.println(outputString);
			outputString = outputString.split(xmlResultStartTag + xmlOutputStartTag)[1];
			outputString = outputString.split(xmlOutputEndTag + xmlResultEndTag)[0];
			String[] networkOutputStrings = outputString.split(",");
			double[] networkOutput = new double[128];
			for (int i = 0; i < 128; i++) {
				networkOutput[i] = Double.parseDouble(networkOutputStrings[i]);
			}
			return networkOutput;
		}

		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) throws Exception {
			double[] networkOutput = getNetworkOutput(Analytic.generateNetworkInput(board));
			CheckersGame.Move bestMove = null;
			double bestScore = -1.0;
			for (CheckersGame.Move legalMove : board.legalMoves) {
				CheckersGame.Move m = null;
				if (legalMove.playerThisTurn == CheckersGame.Player.BLACK) {
					m = legalMove;
				} else if (legalMove.playerThisTurn == CheckersGame.Player.RED) {
					m = legalMove.rotateMove();
				}
				int index;
				if (m.move[1] == m.move[0] + 7 || m.move[1] == m.move[0] + 14) {
					index = networkOutputConversionMap[m.move[0]];
				} else if (m.move[1] == m.move[0] + 9 || m.move[1] == m.move[0] + 18) {
					index = networkOutputConversionMap[m.move[0]] + 32;
				} else if (m.move[1] == m.move[0] - 7 || m.move[1] == m.move[0] - 14) {
					index = networkOutputConversionMap[m.move[0]] + 64;
				} else if (m.move[1] == m.move[0] - 9 || m.move[1] == m.move[0] - 18) {
					index = networkOutputConversionMap[m.move[0]] + 96;
				} else {
					throw new IllegalArgumentException("Invalid move");
				}
				if (networkOutput[index] > bestScore) {
					bestScore = networkOutput[index];
					bestMove = legalMove;
				}
			}
			return bestMove;
		}
	}

	// Strategy that can picks moves from two different strategies randomly
	public static class RandomCombinationStrategy extends Strategy {
		Random rng = new Random();
		Strategy s1;
		Strategy s2;
		double prob1;
		double prob2;

		public RandomCombinationStrategy(Strategy s1, double probability1, Strategy s2, double probability2) {
			this.s1 = s1;
			this.s2 = s2;
		}

		@Override
		public CheckersGame.Move getMove(CheckersGame.Board board) throws Exception {
			if (rng.nextDouble() * (prob1 + prob2) <= prob1) {
				return s1.getMove(board);
			} else {
				return s2.getMove(board);
			}
		}

	}
}

// Class for training the remote analytic
class Analytic {
	private String apiUrl;
	private String xmlAnalyticStartTag;
	private String xmlAnalyticEndTag = "</analytic-training-data>";
	private String xmlDataStartTag = "<data>";
	private String xmlDataEndTag = "</data>";
	private String xmlInputStartTag = "<input>";
	private String xmlInputEndTag = "</input>";
	private String xmlOutputStartTag = "<output>";
	private String xmlOutputEndTag = "</output>";
	private String xmlResetTags;

	// Use a big buffer for the API calls, since each API call is costly
	private StringBuilder analyticBuffer;
	private int bufferSize;
	private final int bufferCapacity = 5000; // how many inputs we want per API call

	private static int[] networkOutputConversionMap = new int[] { -1, 0, -1, 1, -1, 2, -1, 3, 4, -1, 5, -1, 6, -1, 7,
			-1, -1, 8, -1, 9, -1, 10, -1, 11, 12, -1, 13, -1, 14, -1, 15, -1, -1, 16, -1, 17, -1, 18, -1, 19, 20, -1,
			21, -1, 22, -1, 23, -1, -1, 24, -1, 25, -1, 26, -1, 27, 28, -1, 29, -1, 30, -1, 31, -1 };

	public Analytic(String[] apiDetails) {
		this(apiDetails[0], apiDetails[1], apiDetails[2], apiDetails[3], apiDetails[4]);
	}

	public Analytic(String apiUrl, String user, String password, String applicationId, String instanceId) {
		this.apiUrl = apiUrl;
		xmlAnalyticStartTag = "<analytic-training-data application='" + applicationId + "' user='" + user
				+ "' password='" + password + "' instance='" + instanceId + "'>";
		xmlResetTags = "<analytic application='" + applicationId + "' user='" + user + "' password='" + password
				+ "' id='" + instanceId + "'></analytic>";
		analyticBuffer = new StringBuilder();
		analyticBuffer.append(xmlAnalyticStartTag);
		bufferSize = 0;
	}

	// Reset the analytic network
	public void reset() throws Exception {
		Utils.httpPOST(apiUrl + "/reset-data-analytic", "application/xml", xmlResetTags);
	}

	// Learn the board and move
	public void train(CheckersGame.Board board, CheckersGame.Move move) throws Exception {
		double[] networkInput = generateNetworkInput(board);
		double[] networkOutput = generateNetworkOutput(move, board.legalMoves);

//		for (double num : networkInput) {
//			System.out.print(num);
//			System.out.print(',');
//		}
//		System.out.println();
//		for (double num : networkOutput) {
//			System.out.print(num);
//			System.out.print(',');
//		}
//		System.out.println();

		analyticBuffer.append(xmlDataStartTag);
		analyticBuffer.append(xmlInputStartTag);
		analyticBuffer.append(networkInput[0]);
		for (int i = 1; i < networkInput.length; i++) {
			analyticBuffer.append(',');
			analyticBuffer.append(networkInput[i]);
		}
		analyticBuffer.append(xmlInputEndTag);
		analyticBuffer.append(xmlOutputStartTag);
		analyticBuffer.append(networkOutput[0]);
		for (int i = 1; i < networkOutput.length; i++) {
			analyticBuffer.append(',');
			analyticBuffer.append(networkOutput[i]);
		}
		analyticBuffer.append(xmlOutputEndTag);
		analyticBuffer.append(xmlDataEndTag);
		bufferSize++;
		if (bufferSize >= bufferCapacity) {
			flushBuffer();
		}
	}

	// Flush the buffer
	public void flushBuffer() throws Exception {
		if (bufferSize != 0) {
			analyticBuffer.append(xmlAnalyticEndTag);
			Utils.httpPOST(apiUrl + "/train-data-analytic", "application/xml", analyticBuffer.toString());
			analyticBuffer.setLength(0);
			analyticBuffer.append(xmlAnalyticStartTag);
			bufferSize = 0;
		}
	}

	// Convert the CheckersGame.Move into the expected output of the analytic
	private static double[] generateNetworkOutput(CheckersGame.Move m, CheckersGame.Move[] legalMoves) {
		int[] move = null;
		if (m.playerThisTurn == CheckersGame.Player.BLACK) {
			move = m.move;
		} else if (m.playerThisTurn == CheckersGame.Player.RED) {
			move = m.rotateMove().move;
		}
		double[] networkOutput = new double[128];
		Arrays.fill(networkOutput, -1.0);

		// the move is legal, but it is not the one we're looking for
		for (CheckersGame.Move legalM : legalMoves) {
			int[] legalMove = null;
			if (legalM.playerThisTurn == CheckersGame.Player.BLACK) {
				legalMove = legalM.move;
			} else if (legalM.playerThisTurn == CheckersGame.Player.RED) {
				legalMove = legalM.rotateMove().move;
			}
			if (legalMove[1] == legalMove[0] + 7 || legalMove[1] == legalMove[0] + 14) {
				networkOutput[networkOutputConversionMap[legalMove[0]]] = -0.5;
			} else if (legalMove[1] == legalMove[0] + 9 || legalMove[1] == legalMove[0] + 18) {
				networkOutput[networkOutputConversionMap[legalMove[0]] + 32] = -0.5;
			} else if (legalMove[1] == legalMove[0] - 7 || legalMove[1] == legalMove[0] - 14) {
				networkOutput[networkOutputConversionMap[legalMove[0]] + 64] = -0.5;
			} else if (legalMove[1] == legalMove[0] - 9 || legalMove[1] == legalMove[0] - 18) {
				networkOutput[networkOutputConversionMap[legalMove[0]] + 96] = -0.5;
			} else {
				throw new IllegalArgumentException("Invalid move");
			}
		}

		// the move we're looking for
		if (move[1] == move[0] + 7 || move[1] == move[0] + 14) {
			networkOutput[networkOutputConversionMap[move[0]]] = 1.0;
		} else if (move[1] == move[0] + 9 || move[1] == move[0] + 18) {
			networkOutput[networkOutputConversionMap[move[0]] + 32] = 1.0;
		} else if (move[1] == move[0] - 7 || move[1] == move[0] - 14) {
			networkOutput[networkOutputConversionMap[move[0]] + 64] = 1.0;
		} else if (move[1] == move[0] - 9 || move[1] == move[0] - 18) {
			networkOutput[networkOutputConversionMap[move[0]] + 96] = 1.0;
		} else {
			throw new IllegalArgumentException("Invalid move");
		}

		return networkOutput;
	}

	// Convert the CheckersGame.Board into the expected input of the analytic
	// Also used by Strategy.RemoteDeepLearningStrategy
	protected static double[] generateNetworkInput(CheckersGame.Board b) {
		char[] boardArray = null;
		if (b.playerThisTurn == CheckersGame.Player.BLACK) {
			boardArray = b.board;
		} else if (b.playerThisTurn == CheckersGame.Player.RED) {
			boardArray = b.rotateBoard().board;
		}
		char[] convertedBoardArray = new char[32];
		int i = 0;
		for (char piece : boardArray) {
			if (piece != '/') {
				convertedBoardArray[i] = piece;
				i++;
			}
		}
		double[] networkInput = new double[32];
		for (int j = 0; j < 32; j++) {
			if (convertedBoardArray[j] == 'b') {
				networkInput[j] = 0.5;
			} else if (convertedBoardArray[j] == 'B') {
				networkInput[j] = 1.0;
			} else if (convertedBoardArray[j] == 'r') {
				networkInput[j] = -0.5;
			} else if (convertedBoardArray[j] == 'R') {
				networkInput[j] = -1.0;
			}
		}
		return networkInput;
//		double[] networkInput = new double[64];
//		for (int j = 0; j < 32; j++) {
//			if (convertedBoardArray[j] == 'r') {
//				networkInput[j] = 0.5;
//			}
//			else if (convertedBoardArray[j] == 'R') {
//				networkInput[j] = 1.0;
//			}
//		}
//		for (int j = 0; j < 32; j++) {
//			if (convertedBoardArray[j] == 'b') {
//				networkInput[j + 32] = 0.5;
//			}
//			else if (convertedBoardArray[j] == 'B') {
//				networkInput[j + 32] = 1.0;
//			}
//		}
//		return networkInput;
	}

}