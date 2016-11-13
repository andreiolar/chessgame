package com.andreiolar.chess.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to define the behavior of a chess game.
 * 
 * @author Andrei Olar
 **/
public class ChessGame implements Runnable {

	public int gameState = -1;
	public static final int GAME_STATE_WHITE = 0;
	public static final int GAME_STATE_BLACK = 1;
	public static final int GAME_STATE_END_BLACK_WON = 2;
	public static final int GAME_STATE_END_WHITE_WON = 3;

	private List<Piece> pieces = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<Piece>();

	private MoveValidator moveValidator;
	private IPlayerHandler blackPlayerHandler;
	private IPlayerHandler whitePlayerHandler;
	private IPlayerHandler activePlayerHandler;

	/**
	 * Constructor. Used to create all the chess pieces.
	 **/
	public ChessGame() {
		this.moveValidator = new MoveValidator(this);

		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_1, Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);

		int currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, currentColumn);
			currentColumn++;
		}

		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, Piece.ROW_8, Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);

		currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, currentColumn);
			currentColumn++;
		}
	}

	/**
	 * Used to set the player.
	 * 
	 * @param pieceColor
	 *            The color to set to player.
	 * 
	 * @param playerHandler
	 *            The player handler. Could be player or AI.
	 **/
	public void setPlayer(int pieceColor, IPlayerHandler playerHandler) {
		switch (pieceColor) {
			case Piece.COLOR_BLACK :
				this.blackPlayerHandler = playerHandler;
				break;
			case Piece.COLOR_WHITE :
				this.whitePlayerHandler = playerHandler;
				break;
			default :
				throw new IllegalArgumentException("Invalid pieceColor: " + pieceColor);
		}
	}

	/**
	 * Used to start the game and defines the work flow of the running the game.
	 **/
	public void startGame() {
		// Check if all players are ready
		System.out.println("ChessGame: waiting for players");
		while (this.blackPlayerHandler == null || this.whitePlayerHandler == null) {
			// Players are still missing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}

		// Set start player
		this.activePlayerHandler = this.whitePlayerHandler;

		// Start game flow
		System.out.println("ChessGame: starting game flow");
		while (!isGameEndConditionReached()) {
			waitForMove();
			swapActivePlayer();
		}

		System.out.println("ChessGame: game ended");
		// ChessConsole.printCurrentGameState(this);
		if (this.gameState == ChessGame.GAME_STATE_END_BLACK_WON) {
			System.out.println("Black won!");
		} else if (this.gameState == ChessGame.GAME_STATE_END_WHITE_WON) {
			System.out.println("White won!");
		} else {
			throw new IllegalStateException("Illegal end state: " + this.gameState);
		}
	}

	/**
	 * Swaps the active players.
	 **/
	private void swapActivePlayer() {
		if (this.activePlayerHandler == this.whitePlayerHandler) {
			this.activePlayerHandler = this.blackPlayerHandler;
		} else {
			this.activePlayerHandler = this.whitePlayerHandler;
		}

		this.changeGameState();
	}

	/**
	 * Used to wait for the player to move, and execute the move.
	 **/
	private void waitForMove() {
		Move move = null;

		do {
			move = this.activePlayerHandler.getMove();
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			if (move != null && !this.moveValidator.isMoveValid(move, true)) {
				System.out.println("Provided move was invalid: " + move);

				// ChessConsole.printCurrentGameState(this);
				move = null;
				System.exit(0);
			}
		} while (move == null);

		// Execute move
		boolean success = this.movePiece(move);
		if (success) {
			this.blackPlayerHandler.moveSuccessfullyExecuted(move);
			this.whitePlayerHandler.moveSuccessfullyExecuted(move);
		} else {
			throw new IllegalStateException("Move was valid, but failed to execute it");
		}
	}

	/**
	 * Creates and adds the chess piece.
	 **/
	private void createAndAddPiece(int color, int type, int row, int column) {
		Piece piece = new Piece(color, type, row, column);
		this.pieces.add(piece);
	}

	/**
	 * Used to move a piece.
	 * 
	 * @param move
	 *            The move to be done.
	 **/
	public boolean movePiece(Move move) {
		move.capturedPiece = this.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

		Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);

		// Check if the move is capturing an opponent piece
		int opponentColor = (piece.getColor() == Piece.COLOR_BLACK ? Piece.COLOR_WHITE : Piece.COLOR_BLACK);
		if (isNonCapturedPieceAtLocation(opponentColor, move.targetRow, move.targetColumn)) {
			// handle captured piece
			Piece opponentPiece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
			this.pieces.remove(opponentPiece);
			this.capturedPieces.add(opponentPiece);
			opponentPiece.isCaptured(true);
		}

		piece.setRow(move.targetRow);
		piece.setColumn(move.targetColumn);

		return true;
	}

	/**
	 * Used to undo a move. Only used in AI implementation to search for different routes.
	 **/
	public void undoMove(Move move) {
		Piece piece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

		piece.setRow(move.sourceRow);
		piece.setColumn(move.sourceColumn);

		if (move.capturedPiece != null) {
			move.capturedPiece.setRow(move.targetRow);
			move.capturedPiece.setColumn(move.targetColumn);
			move.capturedPiece.isCaptured(false);
			this.capturedPieces.remove(move.capturedPiece);
			this.pieces.add(move.capturedPiece);
		}

		if (piece.getColor() == Piece.COLOR_BLACK) {
			this.gameState = ChessGame.GAME_STATE_BLACK;
		} else {
			this.gameState = ChessGame.GAME_STATE_WHITE;
		}
	}

	/**
	 * Check whether the game ended.
	 * 
	 * @return Returns true if end condition is reached, false otherwise.
	 **/
	private boolean isGameEndConditionReached() {
		for (Piece piece : this.capturedPieces) {
			if (piece.getType() == Piece.TYPE_KING) {
				return true;
			}
		}

		return false;
	}

	public Piece getNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column && piece.isCaptured() == false) {
				return piece;
			}
		}

		return null;
	}

	private boolean isNonCapturedPieceAtLocation(int color, int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column && piece.isCaptured() == false && piece.getColor() == color) {
				return true;
			}
		}

		return false;
	}

	public boolean isNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column && piece.isCaptured() == false) {
				return true;
			}
		}

		return false;
	}

	public int getGameState() {
		return this.gameState;
	}

	public List<Piece> getPieces() {
		return this.pieces;
	}

	/**
	 * Used to change the game state.
	 **/
	public void changeGameState() {
		if (this.isGameEndConditionReached()) {
			if (this.gameState == ChessGame.GAME_STATE_BLACK) {
				this.gameState = ChessGame.GAME_STATE_END_BLACK_WON;
			} else if (this.gameState == ChessGame.GAME_STATE_WHITE) {
				this.gameState = ChessGame.GAME_STATE_END_WHITE_WON;
			}

			return;
		}

		switch (this.gameState) {
			case GAME_STATE_BLACK :
				this.gameState = GAME_STATE_WHITE;
				break;
			case GAME_STATE_WHITE :
				this.gameState = GAME_STATE_BLACK;
				break;
			case GAME_STATE_END_WHITE_WON :
			case GAME_STATE_END_BLACK_WON :
				break;
			default :
				throw new IllegalStateException("Unknown game state: " + this.gameState);
		}
	}

	public MoveValidator getMoveValidator() {
		return this.moveValidator;
	}

	public IPlayerHandler getActivePlayerHandler() {
		return activePlayerHandler;
	}

	/**
	 * Runs the game logic.
	 **/
	@Override
	public void run() {
		this.startGame();
	}

}
