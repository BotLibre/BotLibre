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
package org.botlibre.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.botlibre.analytics.deeplearning.NeuralNetwork;
import org.botlibre.util.Utils;

/**
 * Tic Tac Toe game.
 * This class let's an AI player play another AI player using various different strategies.
 * It includes a basic (take first move), random, look ahead (perfect player), reinforced learning (learn good/bad moves), and deep learning.
 * It includes some test code to verify and compare strategies.
 */
public class TicTacToe {
	static int[][] EndGames = new int[][] {
		{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
		{0, 3, 6}, {1, 4, 7}, {2, 5, 8},
		{0, 4, 8}, {2, 4, 6}
	};
	static char Empty = '_';
	
	static Map<String, Float> boardScores = new HashMap<String, Float>();
	static Map<String, Integer> lookAheadCache = new HashMap<String, Integer>();
	// works with re-learning
	static NeuralNetwork network = new NeuralNetwork(new int[] { 18, 36, 36, 36, 9 });
	//static NeuralNetwork network = new NeuralNetwork(new int[] { 18, 36, 9 });
	
	enum Strategy { Basic, Random, LookAhead, ReinforcedLearning, DeepLearning }
	
	protected char[] board;
	protected List<String> boards;
	protected char turn;
	protected char[] players;
	protected Strategy[] strategies;
	protected boolean over;
	protected char winner;
	protected float[] scores;
	protected float opponentScore;
	protected boolean debug = false;
	protected int debugCount = 0;
	protected boolean breakOnXWin = false;
	protected boolean breakOnOWin = false;
	
	protected Strategy learningStrategy;
	protected boolean learn = true;
	protected boolean learnX = false;
	protected boolean learnO = true;
	protected boolean learnWins = true;
	protected boolean learnLosses = true;
	protected boolean learnTies = true;
	
	public static void main(String[] args) {
		TicTacToe game = new TicTacToe();
		//game.board = new char[] {'X', '_', '_', '_', 'O', '_', '_', '_', '_'};
		//System.out.println(game.findWinningMove());

		network.setLearningRate(new double[] { 0.002 }); // default of 0.1 does not work well.
		// also re-learning values that would be forwardPropagated leads to poor performance,
		network.setMomentum(0.8);
		game.learn = false;
		game.strategies[0] = Strategy.Basic;
		game.strategies[1] = Strategy.DeepLearning;
		
		//game.debug = true;
		//game.autoplay();
		
		for (int count = 0; count < 100; count++) {
			game.strategies[0] = Strategy.Random;
			game.strategies[1] = Strategy.LookAhead;
			game.learningStrategy = Strategy.DeepLearning;
			game.learn = true;
			int games = 10000;
			int[] wins = game.autoplay(games);
	
			if (count == 0) {
				System.out.println("");
				System.out.println("X wins: " + wins[0]);
				System.out.println("O wins: " + wins[1]);
				System.out.println("Ties: " + (games - wins[0] - wins[1]));
				System.out.println("Count: " + game.debugCount);
				System.out.println("Learned boards: " + boardScores.size());
			}
			
			//game.learn = true;
			//game.strategies[0] = Strategy.Random;
			//wins = game.autoplay(games);
			game.learn = false;
			game.strategies[0] = Strategy.Random;
			game.strategies[1] = Strategy.DeepLearning;
			//game.debug = true;
			//game.breakOnXWin = true;
			wins = game.autoplay(games);
			
			//game.debug = true;
			//game.autoplay();
			
			System.out.println("");
			System.out.println("X wins: " + wins[0]);
			System.out.println("O wins: " + wins[1]);
			System.out.println("Ties: " + (games - wins[0] - wins[1]));
			System.out.println("Count: " + game.debugCount);
			System.out.println("Learned boards: " + boardScores.size());
		}
	}
	
	public TicTacToe() {
		newGame();
		this.strategies = new Strategy[2];
		strategies[0] = Strategy.Random;
		strategies[1] = Strategy.Random;
	}
	
	public void printBoard() {
		String board = String.valueOf(this.board);
		printBoard(board);
	}
	
	public static void printBoard(String board) {
		System.out.println("");
		System.out.println(board.substring(0, 3));
		System.out.println(board.substring(3, 6));
		System.out.println(board.substring(6, 9));
	}
	
	public void endTurn() {
		if (this.turn == 'X') {
			this.turn = 'O';
		} else {
			this.turn = 'X';
		}
	}
	
	public void newGame() {
		this.board = new char[] {Empty, Empty, Empty, Empty, Empty, Empty, Empty, Empty, Empty};
		this.boards = new ArrayList<String>();
		this.turn = 'X';
		this.players = new char[2];
		this.players[0] = 'X';
		this.players[1] = 'O';
		this.winner = Empty;
		this.over = false;
		this.scores = new float[2];
		this.scores[0] = 0.0f;
		this.scores[1] = 0.0f;
	}
	
	public int[] autoplay(int games) {
		int index = 0;
		int[] wins = new int[2];
		wins[0] = 0;
		wins[1] = 0;
		while (index < games) {
			autoplay();
			if (this.winner == 'X') {
				wins[0] = wins[0] + 1;
				if (this.breakOnXWin) {
					break;
				}
			} else if (this.winner == 'O') {
				wins[1] = wins[1] + 1;
				if (this.breakOnOWin) {
					break;
				}
			}
			index++;
		}
		return wins;
	}
	
	public void autoplay() {
		newGame();
		while (!this.over) {
			play();
			endTurn();
			if (debug) {
				printBoard();
				System.out.println("");
			}
		}
		boolean wellPlayed = true; //this.scores[0] > 0 && this.scores[1] > 0;
		if (learn) {
			if (this.learningStrategy == Strategy.ReinforcedLearning) {
				float increament = 1.0f / this.boards.size();
				boolean winner = true;
				boolean tie = this.winner == Empty;
				for (int index = this.boards.size(); index > 0; index--) {
					float multiplier = increament * index;
					int player = index % 2;
					boolean learnFromPlayer = (player == 1 && learnX) || player == 0 && learnO;
					if (learnFromPlayer) {
						String boardString = this.boards.get(index - 1);
						Float currentScore = boardScores.get(boardString);
						float score = 0.0f;
						if (currentScore == null) {
							if (tie && this.learnTies) {
								score = 0.1f;
							} else if (winner && this.learnWins) {
								score = 1.0f * multiplier;
							} else if (this.learnLosses) {
								score = -1.0f * multiplier;
							}
						} else {
							if (tie && this.learnTies) {
								if (currentScore >= 0.0f) {
									score = currentScore - (currentScore * 0.5f * multiplier);
									if (score < 0.1) {
										score = 0.1f;
									}
								} else {
									score = currentScore - (currentScore * 0.5f * multiplier);
									if (score > -0.1) {
										score = 0.1f;
									}
								}
							} else if (winner && this.learnWins && wellPlayed) {
								if (currentScore >= 0.0f) {
									score = currentScore + ((1.0f - currentScore) * 0.5f * multiplier);
								} else {
									score = currentScore - (currentScore * 0.5f * multiplier);
									if (score > -0.1f) {
										score = score * -1.0f;
									}
								}
							} else if (this.learnLosses) {
								if (currentScore <= 0.0f) {
									score = currentScore - ((1.0f + currentScore) * 0.5f * multiplier);
								} else {
									score = currentScore - (currentScore * 0.5f * multiplier);
									if (score < 0.1f) {
										score = score * -1.0f;
									}
								}
							}
						}
						if (debug) {
							printBoard(boardString);
							System.out.println("Learn: " + score);
						}
						if (score < -1) {
							throw new Error();
						}
						boardScores.put(boardString, score);
					}
					winner = ! winner;
				}
			} else if (this.learningStrategy == Strategy.DeepLearning) {
				boolean winner = true;
				boolean tie = this.winner == Empty;
				for (int index = this.boards.size(); index > 0; index--) {
					if ((winner && this.learnWins) || (tie && this.learnTies)) {
						int player = index % 2;
						boolean learnFromPlayer = (player == 1 && learnX) || player == 0 && learnO;
						if (learnFromPlayer) {
							String inputBoard = "_________";
							if (index >= 2) {
								inputBoard = this.boards.get(index - 2);
							}
							String outputBoard = this.boards.get(index - 1);
							double[] inputs = network.getInputs();
							double[] outputs = network.getOutputs();
							double[] expectedOutputs = new double[outputBoard.length()];
							int expectedMove = -1;
							for (int position = 0; position < expectedOutputs.length; position++) {
								char input = inputBoard.charAt(position);
								char output = outputBoard.charAt(position);
								if (input != output) {
									expectedMove = position;
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
									if (letter == 'X') {
										inputs[position] = 1.0;
										inputs[position + 9] = 0.0;
									} else if (letter == 'O') {
										inputs[position] = 0.0;
										inputs[position + 9] = 1.0;
									} else {
										inputs[position] = 0.0;
										inputs[position + 9] = 0.0;
									}
								}
								network.forwardPropagate();
								if (debug) {
									System.out.println("Input: " + network.printLayer(network.getInputs()));
									System.out.println("Output: " + network.printLayer(network.getOutputs()));
								}
								double best = -1.0;
								int bestPosition = -1;
								for (int position = 0; position < outputs.length; position++) {
									double value = outputs[position];
									if (this.board[position] == '_' && value > best) {
										best = value;
										bestPosition = position;
									}
								}
								// Only learn if the move was different than the network would have made.
								// For some reason this improves learning.
								if (expectedMove != bestPosition) {
									network.backPropagate(expectedOutputs);
								}
							}
						}
					}
					winner = ! winner;
				}
			}
		}
	}
	
	public boolean play() {
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
		for (int[] endGame : EndGames) {
			boolean match = true;
			char player = Empty;
			for (int index = 0; index < endGame.length; index++) {
				int position = endGame[index];
				char square = this.board[position];
				if (square == Empty) {
					match = false;
					break;
				}
				if (player == Empty) {
					player = square;
				}
				if (player != square) {
					match = false;
					break;
				}
			}
			if (match) {
				this.over = true;
				this.winner = player;
				return true;
			}
		}
		boolean openMove = false;
		for (int index = 0; index < this.board.length; index++) {
			if (this.board[index] == Empty) {
				openMove = true;
				break;
			}
		}
		if (!openMove) {
			this.over = true;
			this.winner = Empty;
			return true;
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
	
	public void move() {
		Strategy strategy = this.strategies[playerIndex()];
		int move = -1;
		if (strategy == Strategy.Basic) {
			move = basicMove();
		} else if (strategy == Strategy.LookAhead) {
			move = lookAhead();
		} else if (strategy == Strategy.ReinforcedLearning) {
			move = reinforcedLearningMove();
		} else if (strategy == Strategy.DeepLearning) {
			move = deepLearningMove();
		} else if (strategy == Strategy.Random) {
			move = randomMove();
		}
		if (move != -1) {
			this.board[move] = this.turn;
		} else {
			throw new RuntimeException("No moves");
		}
		String boardString = new String(this.board);
		Float score = boardScores.get(boardString);
		if (score != null) {
			float totalScore = this.scores[playerIndex()];
			totalScore = (totalScore / 2.0f) + score;
			this.scores[playerIndex()] = totalScore;
		}
		this.boards.add(boardString);
	}
	
	public int randomMove() {
		List<Integer> moves = validMoves();
		if (moves.isEmpty()) {
			return -1;
		}
		return Utils.random(moves);
	}
	
	public int lookAhead() {
		Integer cached = lookAheadCache.get(new String(this.board));
		if (cached != null) {
			return cached;
		}
		List<Integer> moves = validMoves();
		List<Integer> goodMoves = new ArrayList<Integer>(moves);
		if (moves.isEmpty()) {
			return -1;
		}
		int goodMove = findWinningMove();
		if (goodMove == -1) {
			for (int move : moves) {
				this.board[move] = this.turn;
				endTurn();
				int loosingMove = findWinningMove();
				endTurn();
				if (loosingMove != -1) {
					goodMoves.remove((Object)move);
				}
				this.board[move] = Empty;
			}
			if (!goodMoves.isEmpty()) {
				//int move = Utils.random(goodMoves);
				goodMove = goodMoves.get(0);
			}
		}
		lookAheadCache.put(new String(this.board), goodMove);
		return goodMove;
	}

	public int findWinningMove() {
		List<Integer> moves = validMoves();
		if (moves.isEmpty()) {
			return -1;
		}
		// Check for winning move.
		for (int move : moves) {
			this.board[move] = this.turn;
			if (checkGameOver()) {
				this.board[move] = Empty;
				this.over = false;
				if (this.winner != -1 && this.winner != Empty) {
					this.winner = Empty;
					return move;
				}
				this.winner = Empty;
			}
			this.board[move] = Empty;
		}
		// Check for n levels
		for (int move : moves) {
			this.board[move] = this.turn;
			Boolean willWin = null;
			boolean willLoose = false;
			for (int theirMove : moves) {
				if (theirMove == move) {
					continue;
				}
				endTurn();
				this.board[theirMove] = this.turn;
				if (checkGameOver()) {
					endTurn();
					this.board[theirMove] = Empty;
					this.over = false;
					if (this.winner != -1) {
						this.winner = Empty;
						willLoose = true;
						break;
					}
					this.winner = Empty;
				} else {
					endTurn();
				}
				int winningMove = findWinningMove();
				this.board[theirMove] = Empty;
				if (winningMove == -1) {
					willWin = false;
					break;
				} else if (willWin == null) {
					willWin = true;
				}
			}
			this.board[move] = Empty;
			if (!willLoose && (willWin == Boolean.TRUE)) {
				return move;
			}
		}
		return -1;
	}
	
	public int reinforcedLearningMove() {
		List<Integer> moves = validMoves();
		int bestMove = -1;
		float bestMoveScore = 0;
		for (int move : moves) {
			this.board[move] = this.turn;
			String boardString = new String(this.board);
			this.board[move] = Empty;
			Float score = boardScores.get(boardString);
			if (score == null) {
				score = 0.001f;
			}
			if (bestMove == -1 || score > bestMoveScore) {
				bestMove = move;
				bestMoveScore = score;
			}
			if (debug) {
				printBoard(boardString);
				System.out.println("Score: " + score);
			}
		}
		return bestMove;
	}
	
	public int deepLearningMove() {
		double[] inputs = network.getInputs();
		for (int position = 0; position < this.board.length; position++) {
			char letter = this.board[position];
			if (letter == 'X') {
				inputs[position] = 1.0;
				inputs[position + 9] = 0.0;
			} else if (letter == 'O') {
				inputs[position] = 0.0;
				inputs[position + 9] = 1.0;
			} else {
				inputs[position] = 0.0;
				inputs[position + 9] = 0.0;
			}
		}
		network.forwardPropagate();
		double[] outputs = network.getOutputs();
		if (debug) {
			System.out.println(network.printLayer(network.getOutputs()));
		}
		double best = -1.0;
		int bestPosition = -1;
		for (int position = 0; position < outputs.length; position++) {
			double value = outputs[position];
			if (this.board[position] == '_' && value > best) {
				best = value;
				bestPosition = position;
			}
		}
		return bestPosition;
	}
	
	public int basicMove() {
		for (int index = 0; index < this.board.length; index++) {
			if (this.board[index] == Empty) {
				return index;
			}
		}
		return -1;		
	}
	
	public List<Integer> validMoves() {
		List<Integer> moves = new ArrayList<Integer>();
		for (int index = 0; index < this.board.length; index++) {
			if (this.board[index] == Empty) {
				moves.add(index);
			}
		}
		return moves;		
	}
	
}
