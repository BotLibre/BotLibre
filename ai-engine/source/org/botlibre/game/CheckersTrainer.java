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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.botlibre.analytics.deeplearning.NeuralNetwork;
import org.botlibre.util.Utils;

/**
 * Checkers game. This class let's an AI player play another AI player using
 * various different strategies. It includes a basic (take first move), random,
 * look ahead (perfect player), reinforced learning (learn good/bad moves), and
 * deep learning. It includes some test code to verify and compare strategies.
 */
public class CheckersTrainer {
	private static char Empty = '_';

	static NeuralNetwork network = new NeuralNetwork(new int[] { 128, 256, 64 });

	enum Strategy {
		Basic, Random, LookAhead, RandomLookAhead, DeepLearning
	}

	protected char[] board;
	protected List<String> boards;
	protected char turn;
	protected int turns = 0;
	protected char[] players;
	protected Strategy[] strategies;
	protected boolean over;
	protected char winner;
	protected boolean debug = false;
	protected boolean breakOnRWin = false;
	protected boolean breakOnBWin = false;
	protected boolean breakOnTie = false;
	
	protected Strategy learningStrategy = Strategy.DeepLearning;
	protected boolean learn;
	protected boolean learnR;
	protected boolean learnB;
	protected boolean learnWins = true;
	protected boolean learnLosses = false;
	protected boolean learnTies = false;
	protected boolean remote = true;

	public static void main(String[] args) throws Exception {
		resetAnalytic();
		
		CheckersTrainer game = new CheckersTrainer();
		game.debug = true;
		
		game.strategies[0] = Strategy.Random;
		game.strategies[1] = Strategy.RandomLookAhead;
		game.learn = true;
		game.learnR = false;
		game.learnB = true;
		game.playGames(100);
		
		game.strategies[0] = Strategy.Random;
		game.strategies[1] = Strategy.DeepLearning;
		game.learn = false;
		game.playGames(100);
	}
	
	public static void resetAnalytic() throws Exception {
		Utils.httpPOST("http://localhost:8080/botlibreplatform/rest/api/reset-data-analytic", "application/xml",
				"<analytic application='4216385210050617360' user='admin' password='password' id='101'></analytic>");
	}
	
	public void playGames(int games) throws Exception {
		int[] wins = this.autoplay(games);
		printWinStats(games, wins);
	}
	
	
	public static void printWinStats(int games, int[] wins) {
		System.out.println("");
		System.out.println("R wins: " + wins[0]);
		System.out.println("B wins: " + wins[1]);
		System.out.println("Ties: " + (games - wins[0] - wins[1]));
	}

	public CheckersTrainer() {
		newGame();
		this.strategies = new Strategy[2];
	}

	public void printBoard() {
		String board = String.valueOf(this.board);
		printBoard(board);
	}

	public static void printBoard(String board) {
		System.out.println("");
		System.out.println(board.substring(0, 8));
		System.out.println(board.substring(8, 16));
		System.out.println(board.substring(16, 24));
		System.out.println(board.substring(24, 32));
		System.out.println(board.substring(32, 40));
		System.out.println(board.substring(40, 48));
		System.out.println(board.substring(48, 56));
		System.out.println(board.substring(56, 64));
	}

	public void endTurn() {
		if (this.turn == 'R') {
			this.turn = 'B';
		} else {
			this.turn = 'R';
		}
	}

	public void newGame() {
		this.board = new char[] { '/', 'r', '/', 'r', '/', 'r', '/', 'r', 'r', '/', 'r', '/', 'r', '/', 'r', '/', '/',
				'r', '/', 'r', '/', 'r', '/', 'r', Empty, '/', Empty, '/', Empty, '/', Empty, '/', '/', Empty, '/',
				Empty, '/', Empty, '/', Empty, 'b', '/', 'b', '/', 'b', '/', 'b', '/', '/', 'b', '/', 'b', '/', 'b',
				'/', 'b', 'b', '/', 'b', '/', 'b', '/', 'b', '/' };
		this.boards = new ArrayList<String>();
		this.turn = 'R';
		this.turns = 0;
		this.players = new char[2];
		this.players[0] = 'R';
		this.players[1] = 'B';
		this.winner = Empty;
		this.over = false;
	}

	public int[] autoplay(int games) throws Exception {
		int index = 0;
		int[] wins = new int[2];
		wins[0] = 0;
		wins[1] = 0;
		while (index < games) {
			autoplay();
			if (this.winner == 'R') {
				wins[0] = wins[0] + 1;
				if (this.breakOnRWin) {
					break;
				}
			} else if (this.winner == 'B') {
				wins[1] = wins[1] + 1;
				if (this.breakOnBWin) {
					break;
				}
			} else if (this.winner == Empty) {
				if (this.breakOnTie) {
					break;
				}
			}
			index++;
		}
		return wins;
	}

	public void autoplay() throws Exception {
		newGame();

		while (!this.over) {
			play();
			endTurn();
			if (debug) {
				printBoard();
				System.out.println("");
			}
		}

		if (learn) {
			if (this.learningStrategy == Strategy.DeepLearning) {
				boolean winner = true;
				boolean tie = this.winner == Empty;
				StringWriter analyticWriter = new StringWriter();
				analyticWriter.write(
						"<analytic-training-data application='4216385210050617360' user='admin' password='password' instance='101'> <data> ");
				boolean analyticAddedFlag = false;
				if ((winner && this.learnWins) || (tie && this.learnTies)) {
					for (int index = this.boards.size(); index > 0; index--) {
						int player = index % 2;
						boolean learnFromPlayer = (player == 1 && learnR) || (player == 0 && learnB);
						if (learnFromPlayer) {
							String inputBoard = "/r/r/r/rr/r/r/r//r/r/r/r_/_/_/_//_/_/_/_b/b/b/b//b/b/b/bb/b/b/b/";
							if (index >= 2) {
								inputBoard = this.boards.get(index - 2);
							}
							String outputBoard = this.boards.get(index - 1);
							double[] inputs = network.getInputs();
							double[] outputs = network.getOutputs();
							double[] expectedOutputs = new double[outputBoard.length()];
							int expectedInput = -1;
							for (int position = 0; position < expectedOutputs.length; position++) {
								char input = inputBoard.charAt(position);
								char output = outputBoard.charAt(position);
								if ((input != output) && (((player == 1) && ((input == 'r') || (input == 'R')))
										|| ((player == 0) && ((input == 'b') || (input == 'B'))))) {
									expectedInput = position;
									expectedOutputs[position] = 1.0;
									break;
								}
							}
							if (this.debug) {
								System.out.println("Learn: " + inputBoard + " -> " + outputBoard);
								System.out.println("Expected output: " + network.printLayer(expectedOutputs));
							}

							int repeats = 1;
							if (winner) {
								repeats = 2;
							}
							for (int count = 0; count < repeats; count++) {
								for (int position = 0; position < inputBoard.length(); position++) {
									char letter = inputBoard.charAt(position);
									if (letter == 'R') {
										inputs[position] = 1.0;
										inputs[position + 64] = 0.0;
									} else if (letter == 'r') {
										inputs[position] = 0.5;
										inputs[position + 64] = 0.0;
									} else if (letter == 'B') {
										inputs[position] = 0.0;
										inputs[position + 64] = 1.0;
									} else if (letter == 'b') {
										inputs[position] = 0.0;
										inputs[position + 64] = 0.5;
									} else {
										inputs[position] = 0.0;
										inputs[position + 64] = 0.0;
									}
								}
								if (remote) {
									double[] inputvalues = inputs;
									double[] outputvalues = expectedOutputs;
									analyticWriter.write("<input>");
									for (int inputCount = 0; inputCount < (inputvalues.length); inputCount++) {
										analyticWriter.write(String.valueOf(inputvalues[inputCount]));
										if (inputCount == inputvalues.length - 1) {
											break;
										}
										analyticWriter.write(",");
									}
									analyticWriter.write("</input> <output>");
									for (int outputCount = 0; outputCount < (outputvalues.length); outputCount++) {
										analyticWriter.write(String.valueOf(outputvalues[outputCount]));
										if (outputCount == outputvalues.length - 1) {
											break;
										}
										analyticWriter.write(",");
									}
									analyticWriter.write("</output> ");
									analyticAddedFlag = true;

								} else if (!remote) {
									network.forwardPropagate();
									if (debug) {
										System.out.println("Input: " + network.printLayer(network.getInputs()));
										System.out.println("Output: " + network.printLayer(network.getOutputs()));
									}
									double best = -1.0;
									int bestPosition = -1;
									for (int position = 0; position < outputs.length; position++) {
										double value = outputs[position];
										if (value > best) {
											best = value;
											bestPosition = position;
										}
									}
									// Only learn if the move was different than the network would have made.
									// For some reason this improves learning.
									if (expectedInput != bestPosition) {
										network.backPropagate(expectedOutputs);
									}
								}
							}
						}
					}
					// winner = ! winner;
				}
				if (remote && analyticAddedFlag) {
					analyticWriter.write(" </data> </analytic-training-data>");
					String result = Utils.httpPOST(
							"http://localhost:8080/botlibreplatform/rest/api/train-data-analytic",
							"application/xml", analyticWriter.toString());
					if (debug) {
						System.out.println(result);
					}
				}
			}
		}
	}

	public boolean play() throws Exception {
		if (checkGameOver()) {
			return true;
		}
		move();
		if (checkGameOver()) {
			return true;
		}
		return false;
	}

	public boolean checkGameOver() {
		int redCount = 0;
		int blackCount = 0;
		for (int index = 0; index < this.board.length; index++) {
			int square = this.board[index];
			if ((square == 'r') || (square == 'R')) {
				redCount++;
			} else if ((square == 'b') || (square == 'B')) {
				blackCount++;
			}
		}
		if (redCount == 0) {
			this.over = true;
			this.winner = 'B';
			return true;
		}
		if (blackCount == 0) {
			this.over = true;
			this.winner = 'R';
			return true;
		}
		if (turns == 1000) {
			this.over = true;
			this.winner = Empty;
			// throw new RuntimeException("Draw");
		}
		return false;
	}

	public int playerIndex() {
		for (int index = 0; index < this.players.length; index++) {
			if (this.players[index] == this.turn) {
				return index;
			}
		}
		return -1;
	}

	public void move() throws Exception {
		Strategy strategy = this.strategies[playerIndex()];
		int[] move = null;
		if (strategy == Strategy.Basic) {
			move = basicMove();
		} else if ((strategy == Strategy.LookAhead) || (strategy == Strategy.RandomLookAhead)) {
			move = lookAhead();
		} else if (strategy == Strategy.DeepLearning) {
			move = deepLearningMove();
		} else if (strategy == Strategy.Random) {
			move = randomMove();
		}
		if (move != null) {
			this.board[move[1]] = this.board[move[0]];
			this.board[move[0]] = Empty;
			turns++;
			if (Math.abs(move[0] - move[1]) > 10) {
				this.board[(move[0] + move[1]) / 2] = Empty;
				checkQueen(move);
				checkJump(move);
				turns = 0;
			}
			checkQueen(move);
		} else {
			this.over = true;
			endTurn();
			this.winner = this.turn;
		}
		String boardString = new String(this.board);
		this.boards.add(boardString);
	}

	public int[] randomMove() {
		List<int[]> moves = validMoves();
		if (moves.isEmpty()) {
			return null;
		}
		return Utils.random(moves);
	}

	public int[] basicMove() {
		List<int[]> moves = validMoves();
		if (moves.isEmpty()) {
			return null;
		}
		if (this.turn == 'R') {
			return moves.get(moves.size() - 1);
		}
		return moves.get(0);
	}

	public int[] lookAhead() {
		/*
		 * Integer cached = lookAheadCache.get(new String(this.board)); if (cached !=
		 * null) { return cached; }
		 */
		List<int[]> moves = validMoves();
		List<int[]> goodMoves = new ArrayList<int[]>(moves);
		if (moves.isEmpty()) {
			return null;
		}
		int[] goodMove = findBestMove();
		if (goodMove == null) {
			for (int[] move : moves) {
				boolean queen = false;
				this.board[move[1]] = this.board[move[0]];
				this.board[move[0]] = Empty;
				if (Math.abs(move[0] - move[1]) > 10) {
					if ((this.board[(move[0] + move[1]) / 2] == 'R') || (this.board[(move[0] + move[1]) / 2] == 'B')) {
						queen = true;
					}
					this.board[(move[0] + move[1]) / 2] = Empty;
				}
				if (this.turn == 'R') {
					if (((0 < (move[1] - 7)) && ((move[1] + 7) < 64)
							&& ((this.board[move[1] + 7] == 'b') || (this.board[move[1] + 7] == 'B'))
							&& (this.board[move[1] - 7] == Empty))
							|| ((0 < (move[1] - 9)) && ((move[1] + 9) < 64)
									&& ((this.board[move[1] + 9] == 'b') || (this.board[move[1] + 9] == 'B'))
									&& (this.board[move[1] - 9] == Empty))) {
						goodMoves.remove((Object) move);
					}
					if (((0 < (move[1] - 7)) && ((move[1] + 7) < 64) && (this.board[move[1] - 7] == 'B')
							&& (this.board[move[1] + 7] == Empty))
							|| ((0 < (move[1] - 9)) && ((move[1] + 9) < 64) && (this.board[move[1] - 9] == 'B')
									&& (this.board[move[1] + 9] == Empty))) {
						goodMoves.remove((Object) move);
					}
				}
				if (this.turn == 'B') {
					if (((0 < (move[1] - 7)) && ((move[1] + 7) < 64)
							&& ((this.board[move[1] - 7] == 'r') || (this.board[move[1] - 7] == 'R'))
							&& (this.board[move[1] + 7] == Empty))
							|| ((0 < (move[1] - 9)) && ((move[1] + 9) < 64)
									&& ((this.board[move[1] - 9] == 'r') || (this.board[move[1] - 9] == 'R'))
									&& (this.board[move[1] + 9] == Empty))) {
						goodMoves.remove((Object) move);
					}
					if (((0 < (move[1] - 7)) && ((move[1] + 7) < 64) && (this.board[move[1] + 7] == 'R')
							&& (this.board[move[1] - 7] == Empty))
							|| ((0 < (move[1] - 9)) && ((move[1] + 9) < 64) && (this.board[move[1] + 9] == 'R')
									&& (this.board[move[1] - 9] == Empty))) {
						goodMoves.remove((Object) move);
					}
				}
				this.board[move[0]] = this.board[move[1]];
				this.board[move[1]] = Empty;
				if (Math.abs(move[0] - move[1]) > 10) {
					if (this.turn == 'R') {
						this.board[(move[0] + move[1]) / 2] = 'b';
						if (queen) {
							this.board[(move[0] + move[1]) / 2] = 'B';
						}
					}
					if (this.turn == 'B') {
						this.board[(move[0] + move[1]) / 2] = 'r';
						if (queen) {
							this.board[(move[0] + move[1]) / 2] = 'R';
						}
					}
				}
			}
			if (!goodMoves.isEmpty()) {
				if (this.turn == 'R') {
					goodMove = goodMoves.get(goodMoves.size() - 1);
				} else {
					goodMove = goodMoves.get(0);
				}
				if (this.strategies[playerIndex()] == Strategy.RandomLookAhead) {
					goodMove = Utils.random(goodMoves);
				}

			}
			if (goodMoves.isEmpty()) {
				if (this.turn == 'R') {
					goodMove = moves.get(moves.size() - 1);
				} else {
					goodMove = moves.get(0);
				}
				if (this.strategies[playerIndex()] == Strategy.RandomLookAhead) {
					goodMove = Utils.random(goodMoves);
				}

			}
		}
		// lookAheadCache.put(new String(this.board), goodMove[0]);
		return goodMove;
	}

	public int[] findBestMove() {
		List<int[]> bestMoves = validMoves();
		if (bestMoves.isEmpty()) {
			return null;
		}
		// Check for winning move.
		for (int[] bestMove : bestMoves) {
			this.board[bestMove[1]] = this.board[bestMove[0]];
			this.board[bestMove[0]] = Empty;
			if (Math.abs(bestMove[0] - bestMove[1]) > 10) {
				this.board[bestMove[0]] = this.board[bestMove[1]];
				this.board[bestMove[1]] = Empty;
				return bestMove;
			}
			this.board[bestMove[0]] = this.board[bestMove[1]];
			this.board[bestMove[1]] = Empty;
		}
		// Check for n levels
		/*
		 * for (int[] move : moves) { this.board[move[1]] = this.turn; Boolean willWin =
		 * null; boolean willLoose = false; for (int[] theirMove : moves) { if
		 * (theirMove == move) { continue; } endTurn(); this.board[theirMove[1]] =
		 * this.turn; if (checkGameOver()) { endTurn(); this.board[theirMove[1]] =
		 * Empty; this.over = false; if (this.winner != -1) { this.winner = Empty;
		 * willLoose = true; break; } this.winner = Empty; } else { endTurn(); } int[]
		 * winningMove = findBestMove(); this.board[theirMove[1]] = Empty; if
		 * (winningMove == null) { willWin = false; break; } else if (willWin == null) {
		 * willWin = true; } } this.board[move[1]] = Empty; if (!willLoose && (willWin
		 * == Boolean.TRUE)) { return move; } }
		 */
		return null;
	}

	public int[] deepLearningMove() throws Exception {
		double[] inputs = network.getInputs();
		for (int position = 0; position < this.board.length; position++) {
			char letter = this.board[position];
			if (letter == 'R') {
				inputs[position] = 1.0;
				inputs[position + 64] = 0.0;
			} else if (letter == 'r') {
				inputs[position] = 0.5;
				inputs[position + 64] = 0.0;
			} else if (letter == 'B') {
				inputs[position] = 0.0;
				inputs[position + 64] = 1.0;
			} else if (letter == 'b') {
				inputs[position] = 0.0;
				inputs[position + 64] = 0.5;
			} else {
				inputs[position] = 0.0;
				inputs[position + 64] = 0.0;
			}
		}

		if (!remote) {
			network.forwardPropagate();
		}

		double[] outputs = network.getOutputs();
		if (debug) {
			System.out.println(network.printLayer(network.getOutputs()));
		}

		if (remote) {
			double[] inputvalues = inputs;
			StringWriter analyticWriter = new StringWriter();
			analyticWriter.write(
					"<analytic application='4216385210050617360' user='admin' password='password' id='101'> <input>");
			for (int inputCount = 0; inputCount < (inputvalues.length); inputCount++) {
				analyticWriter.write(String.valueOf(inputvalues[inputCount]));
				if (inputCount == inputvalues.length - 1) {
					break;
				}
				analyticWriter.write(",");
			}
			analyticWriter.write("</input> </analytic>");

			String result = Utils.httpPOST("http://localhost:8080/botlibreplatform/rest/api/test-data-analytic",
					"application/xml", analyticWriter.toString());

			// As of now, result is in the form
			// "<data-analytic-result><output>0.01,0.03,...,-0.02</output></data-analytic-result>"
			// remove XML tags
			result = result.replace("<data-analytic-result><output>", "");
			result = result.replace("</output></data-analytic-result>", "");

			if (debug) {
				System.out.println(result);
			}
			String[] stringOutputs = result.split(",");
			double[] doubleOutputs = new double[stringOutputs.length];
			// for (int count = 0; count < (stringOutputs.length - 1); count++ ) {
			for (int count = 0; count < stringOutputs.length; count++) {
				doubleOutputs[count] = Double.parseDouble(stringOutputs[count]);
			}
			outputs = doubleOutputs;
		}

		double best = -1.0;
		int[] bestPosition = null;
		List<int[]> positions = validMoves();
		for (int[] position : positions) {
			double value = outputs[position[0]];
			if (value > best) {
				best = value;
				bestPosition = position;
			}
		}
		return bestPosition;
	}

	public List<int[]> validMoves() {
		List<int[]> moves = new ArrayList<int[]>();
		for (int index = 0; index < this.board.length; index++) {
			if (this.board[index] == Empty) {
				int[] move = new int[2];
				move[1] = index;
				if (this.turn == 'R') {
					if ((0 < index - 9) && (this.board[index - 9] == 'r')) {
						move[0] = index - 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (this.board[index - 7] == 'r')) {
						move[0] = index - 7;
						moves.add(move);
					}
					if ((0 < index - 9) && (this.board[index - 9] == 'R')) {
						move[0] = index - 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (this.board[index - 7] == 'R')) {
						move[0] = index - 7;
						moves.add(move);
					}
					if ((index + 9 < 64) && (this.board[index + 9] == 'R')) {
						move[0] = index + 9;
						moves.add(move);
					}
					if ((index + 7 < 64) && (this.board[index + 7] == 'R')) {
						move[0] = index + 7;
						moves.add(move);
					}
				}
				if (this.turn == 'B') {
					if ((index + 9 < 64) && (this.board[index + 9] == 'b')) {
						move[0] = index + 9;
						moves.add(move);
					}
					if ((index + 7 < 64) && (this.board[index + 7] == 'b')) {
						move[0] = index + 7;
						moves.add(move);
					}
					if ((index + 9 < 64) && (this.board[index + 9] == 'B')) {
						move[0] = index + 9;
						moves.add(move);
					}
					if ((index + 7 < 64) && (this.board[index + 7] == 'B')) {
						move[0] = index + 7;
						moves.add(move);
					}
					if ((0 < index - 9) && (this.board[index - 9] == 'B')) {
						move[0] = index - 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (this.board[index - 7] == 'B')) {
						move[0] = index - 7;
						moves.add(move);
					}
				}
			}
			if ((this.board[index] == 'b') || (this.board[index] == 'B')) {
				int[] move = new int[2];
				if (this.turn == 'R') {
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'r')
							&& (this.board[index + 9] == Empty)) {
						move[0] = index - 9;
						move[1] = index + 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'r')
							&& (this.board[index + 7] == Empty)) {
						move[0] = index - 7;
						move[1] = index + 7;
						moves.add(move);
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'R')
							&& (this.board[index + 9] == Empty)) {
						move[0] = index - 9;
						move[1] = index + 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'R')
							&& (this.board[index + 7] == Empty)) {
						move[0] = index - 7;
						move[1] = index + 7;
						moves.add(move);
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index + 9] == 'R')
							&& (this.board[index - 9] == Empty)) {
						move[0] = index + 9;
						move[1] = index - 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index + 7] == 'R')
							&& (this.board[index - 7] == Empty)) {
						move[0] = index + 7;
						move[1] = index - 7;
						moves.add(move);
					}
				}
			}
			if ((this.board[index] == 'r') || (this.board[index] == 'R')) {
				int[] move = new int[2];
				if (this.turn == 'B') {
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == Empty)) {
						if ((this.board[index + 9] == 'b') || (this.board[index + 9] == 'B')) {
							move[0] = index + 9;
							move[1] = index - 9;
							moves.add(move);
						}
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == Empty)) {
						if ((this.board[index + 7] == 'b') || (this.board[index + 7] == 'B')) {
							move[0] = index + 7;
							move[1] = index - 7;
							moves.add(move);
						}
					}
					if ((0 < index - 9) && (index + 9 < 64) && (this.board[index - 9] == 'B')
							&& (this.board[index + 9] == Empty)) {
						move[0] = index - 9;
						move[1] = index + 9;
						moves.add(move);
					}
					if ((0 < index - 7) && (index + 7 < 64) && (this.board[index - 7] == 'B')
							&& (this.board[index + 7] == Empty)) {
						move[0] = index - 7;
						move[1] = index + 7;
						moves.add(move);
					}
				}
			}
		}
		return moves;
	}

	public boolean checkJump(int[] move) {
		if (this.turn == 'R') {
			if ((move[1] + 14) < 64) {
				if ((this.board[move[1] + 7] == 'b') || (this.board[move[1] + 7] == 'B')) {
					if (this.board[move[1] + 14] == Empty) {
						this.board[move[1] + 7] = Empty;
						this.board[move[1] + 14] = this.board[move[1]];
						this.board[move[1]] = Empty;
						move[1] = move[1] + 14;
						checkQueen(move);
						checkJump(move);
						return true;
					}
				}
			}
			if ((move[1] + 18) < 64) {
				if ((this.board[move[1] + 9] == 'b') || (this.board[move[1] + 9] == 'B')) {
					if (this.board[move[1] + 18] == Empty) {
						this.board[move[1] + 9] = Empty;
						this.board[move[1] + 18] = this.board[move[1]];
						this.board[move[1]] = Empty;
						move[1] = move[1] + 18;
						checkQueen(move);
						checkJump(move);
						return true;
					}
				}
			}
			if (this.board[move[1]] == 'R') {
				if (0 < move[1] - 14) {
					if ((this.board[move[1] - 7] == 'b') || (this.board[move[1] - 7] == 'B')) {
						if (this.board[move[1] - 14] == Empty) {
							this.board[move[1] - 7] = Empty;
							this.board[move[1] - 14] = this.board[move[1]];
							this.board[move[1]] = Empty;
							move[1] = move[1] - 14;
							checkQueen(move);
							checkJump(move);
							return true;
						}
					}
				}
			}
			if (this.board[move[1]] == 'R') {
				if (0 < move[1] - 18) {
					if ((this.board[move[1] - 9] == 'b') || (this.board[move[1] - 9] == 'B')) {
						if (this.board[move[1] - 18] == Empty) {
							this.board[move[1] - 9] = Empty;
							this.board[move[1] - 18] = this.board[move[1]];
							this.board[move[1]] = Empty;
							move[1] = move[1] - 18;
							checkQueen(move);
							checkJump(move);
							return true;
						}
					}
				}
			}
		}
		if (this.turn == 'B') {
			if (0 < move[1] - 14) {
				if ((this.board[move[1] - 7] == 'r') || (this.board[move[1] - 7] == 'R')) {
					if (this.board[move[1] - 14] == Empty) {
						this.board[move[1] - 7] = Empty;
						this.board[move[1] - 14] = this.board[move[1]];
						this.board[move[1]] = Empty;
						move[1] = move[1] - 14;
						checkQueen(move);
						checkJump(move);
						return true;
					}
				}
			}
			if (0 < move[1] - 18) {
				if ((this.board[move[1] - 9] == 'r') || (this.board[move[1] - 9] == 'R')) {
					if (this.board[move[1] - 18] == Empty) {
						this.board[move[1] - 9] = Empty;
						this.board[move[1] - 18] = this.board[move[1]];
						this.board[move[1]] = Empty;
						move[1] = move[1] - 18;
						checkQueen(move);
						checkJump(move);
						return true;
					}
				}
			}
			if (this.board[move[1]] == 'B') {
				if (move[1] + 14 < 64) {
					if ((this.board[move[1] + 7] == 'r') || (this.board[move[1] + 7] == 'R')) {
						if (this.board[move[1] + 14] == Empty) {
							this.board[move[1] + 7] = Empty;
							this.board[move[1] + 14] = this.board[move[1]];
							this.board[move[1]] = Empty;
							move[1] = move[1] + 14;
							checkQueen(move);
							checkJump(move);
							return true;
						}
					}
				}
			}
			if (this.board[move[1]] == 'B') {
				if (move[1] + 18 < 64) {
					if ((this.board[move[1] + 9] == 'r') || (this.board[move[1] + 9] == 'R')) {
						if (this.board[move[1] + 18] == Empty) {
							this.board[move[1] + 9] = Empty;
							this.board[move[1] + 18] = this.board[move[1]];
							this.board[move[1]] = Empty;
							move[1] = move[1] + 18;
							checkQueen(move);
							checkJump(move);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean checkQueen(int[] move) {
		if ((this.turn == 'R') && (55 < move[1]) && (this.board[move[1]] == 'r')) {
			this.board[move[1]] = 'R';
			return true;
		}
		if ((this.turn == 'B') && (move[1] < 8) && (this.board[move[1]] == 'b')) {
			this.board[move[1]] = 'B';
			return true;
		}
		return false;
	}

}
