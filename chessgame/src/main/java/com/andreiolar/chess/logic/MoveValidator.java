package com.andreiolar.chess.logic;

public class MoveValidator {

	private ChessGame chessGame;
	private Piece sourcePiece;
	private Piece targetPiece;
	private boolean debug;

	public MoveValidator(ChessGame chessGame) {
		this.chessGame = chessGame;
	}

	public boolean isMoveValid(Move move, boolean debug) {
		this.debug = debug;
		int sourceRow = move.sourceRow;
		int sourceColumn = move.sourceColumn;
		int targetRow = move.targetRow;
		int targetColumn = move.targetColumn;

		sourcePiece = chessGame.getNonCapturedPieceAtLocation(sourceRow, sourceColumn);
		targetPiece = chessGame.getNonCapturedPieceAtLocation(targetRow, targetColumn);

		// Source piece does not exist
		if (sourcePiece == null) {
			log("Source piece does not exists");
			return false;
		}

		// Source piece has the right color?
		if (sourcePiece.getColor() == Piece.COLOR_WHITE && this.chessGame.getGameState() == ChessGame.GAME_STATE_WHITE) {
			// OK
		} else if (sourcePiece.getColor() == Piece.COLOR_BLACK && this.chessGame.getGameState() == ChessGame.GAME_STATE_BLACK) {
			// OK
		} else {
			log("It's not your turn: " + "pieceColor=" + Piece.getColorString(sourcePiece.getColor()) + "gameState=" + this.chessGame.getGameState());
			// ChessConsole.printCurrentGameState(this.chessGame);
			return false;
		}

		// Check if target location within boundaries
		if (targetRow < Piece.ROW_1 || targetRow > Piece.ROW_8 || targetColumn < Piece.COLUMN_A || targetColumn > Piece.COLUMN_H) {
			log("target row or column out of scope");
			return false;
		}

		// Validate piece movement rules
		boolean validPieceMove = false;
		switch (sourcePiece.getType()) {
			case Piece.TYPE_BISHOP :
				validPieceMove = isValidBishopMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			case Piece.TYPE_KING :
				validPieceMove = isValidKingMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			case Piece.TYPE_KNIGHT :
				validPieceMove = isValidKnightMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			case Piece.TYPE_PAWN :
				validPieceMove = isValidPawnMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			case Piece.TYPE_QUEEN :
				validPieceMove = isValidQueenMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			case Piece.TYPE_ROOK :
				validPieceMove = isValidRookMove(sourceRow, sourceColumn, targetRow, targetColumn);
				break;
			default :
				break;
		}

		if (!validPieceMove) {
			return false;
		}

		// TODO: Andrei Olar - Handle stalemate and checkmate

		return true;
	}

	private boolean isTargetLocationCapturable() {
		if (targetPiece == null) {
			return false;
		} else if (targetPiece.getColor() != sourcePiece.getColor()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isTargetLocationFree() {
		return targetPiece == null;
	}

	private boolean isValidBishopMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		// Is target location captureable ?
		if (isTargetLocationFree() || isTargetLocationCapturable()) {
			// OK
		} else {
			return false;
		}

		boolean isValid = false;

		// First check if path to target is diagonally
		int diffRow = targetRow - sourceRow;
		int diffColumn = targetColumn - sourceColumn;

		if (diffRow == diffColumn && diffColumn > 0) { // Moving up-right
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, +1, +1);
		} else if (diffRow == -diffColumn && diffColumn > 0) { // Moving down-right
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, -1, +1);
		} else if (diffRow == diffColumn && diffColumn < 0) { // Moving down-left
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, -1, -1);
		} else if (diffRow == -diffColumn && diffColumn < 0) { // Moving up-left
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, +1, -1);
		} else { // Not moving diagonally
			isValid = false;
		}

		return isValid;
	}

	private boolean isValidQueenMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		boolean result = isValidBishopMove(sourceRow, sourceColumn, targetRow, targetColumn);
		result |= isValidRookMove(sourceRow, sourceColumn, targetRow, targetColumn);
		return result;
	}

	private boolean isValidPawnMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		boolean isValid = false;

		if (isTargetLocationFree()) {
			if (sourceColumn == targetColumn) {
				if (sourcePiece.getColor() == Piece.COLOR_WHITE) {
					if (sourceRow + 1 == targetRow) {
						isValid = true;
					} else {
						isValid = false;
					}
				} else {
					if (sourceRow - 1 == targetRow) {
						isValid = true;
					} else {
						isValid = false;
					}
				}
			} else {
				isValid = false;
			}
		} else if (isTargetLocationCapturable()) {
			if (sourceColumn + 1 == targetColumn || sourceColumn - 1 == targetColumn) {
				if (sourcePiece.getColor() == Piece.COLOR_WHITE) {
					if (sourceRow + 1 == targetRow) {
						isValid = true;
					} else {
						isValid = false;
					}
				} else {
					if (sourceRow - 1 == targetRow) {
						isValid = true;
					} else {
						isValid = false;
					}
				}
			} else {
				isValid = false;
			}
		}

		// TODO: May advance two squares on it's first move

		return isValid;
	}

	private boolean isValidKnightMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		if (isTargetLocationFree() || isTargetLocationCapturable()) {
			// OK
		} else {
			return false;
		}

		if (sourceRow + 2 == targetRow && sourceColumn + 1 == targetColumn) {// move up up right
			return true;
		} else if (sourceRow + 1 == targetRow && sourceColumn + 2 == targetColumn) {// move up right right
			return true;
		} else if (sourceRow - 1 == targetRow && sourceColumn + 2 == targetColumn) {// move down right right
			return true;
		} else if (sourceRow - 2 == targetRow && sourceColumn + 1 == targetColumn) {// move down down right
			return true;
		} else if (sourceRow - 2 == targetRow && sourceColumn - 1 == targetColumn) {// move down down left
			return true;
		} else if (sourceRow - 1 == targetRow && sourceColumn - 2 == targetColumn) {// move down left left
			return true;
		} else if (sourceRow + 1 == targetRow && sourceColumn - 2 == targetColumn) {// move up left left
			return true;
		} else if (sourceRow + 2 == targetRow && sourceColumn - 1 == targetColumn) {// move up up left
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidKingMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		if (isTargetLocationFree() || isTargetLocationCapturable()) {
			// OK
		} else {
			return false;
		}

		boolean isValid = true;
		if (sourceRow + 1 == targetRow && sourceColumn == targetColumn) {// up
			isValid = true;
		} else if (sourceRow + 1 == targetRow && sourceColumn + 1 == targetColumn) {// up right
			isValid = true;
		} else if (sourceRow == targetRow && sourceColumn + 1 == targetColumn) {// right
			isValid = true;
		} else if (sourceRow - 1 == targetRow && sourceColumn + 1 == targetColumn) {// down right
			isValid = true;
		} else if (sourceRow - 1 == targetRow && sourceColumn == targetColumn) {// down
			isValid = true;
		} else if (sourceRow - 1 == targetRow && sourceColumn - 1 == targetColumn) {// down left
			isValid = true;
		} else if (sourceRow == targetRow && sourceColumn - 1 == targetColumn) {// left
			isValid = true;
		} else if (sourceRow + 1 == targetRow && sourceColumn - 1 == targetColumn) {// up left
			isValid = true;
		} else {
			System.out.println("moving too far");
			isValid = false;
		}

		return isValid;
	}

	private boolean isValidRookMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
		if (isTargetLocationFree() || isTargetLocationCapturable()) {
			// OK
		} else {
			return false;
		}

		boolean isValid = false;

		// first lets check if the path to the target is straight at all
		int diffRow = targetRow - sourceRow;
		int diffColumn = targetColumn - sourceColumn;

		if (diffRow == 0 && diffColumn > 0) {// right
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, 0, +1);
		} else if (diffRow == 0 && diffColumn < 0) {// left
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, 0, -1);
		} else if (diffRow > 0 && diffColumn == 0) {// up
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, +1, 0);
		} else if (diffRow < 0 && diffColumn == 0) {// down
			isValid = !arePiecesBetweenSourceAndTarget(sourceRow, sourceColumn, targetRow, targetColumn, -1, 0);
		} else { // not moving straight
			isValid = false;
		}

		return isValid;
	}

	private boolean arePiecesBetweenSourceAndTarget(int sourceRow, int sourceColumn, int targetRow, int targetColumn, int rowIncrementPerStep,
			int columnIncrementPerStep) {
		int currentRow = sourceRow + rowIncrementPerStep;
		int currentColumn = sourceColumn + columnIncrementPerStep;

		while (true) {
			if (currentRow == targetRow && currentColumn == targetColumn) {
				break;
			}

			if (currentRow < Piece.ROW_1 || currentRow > Piece.ROW_8 || currentColumn < Piece.COLUMN_A || currentColumn > Piece.COLUMN_H) {
				break;
			}

			if (this.chessGame.isNonCapturedPieceAtLocation(currentRow, currentColumn)) {
				return true;
			}

			currentRow += rowIncrementPerStep;
			currentColumn += columnIncrementPerStep;
		}

		return false;
	}

	private void log(String message) {
		if (debug) {
			System.out.println(message);
		}
	}

}
