package com.andreiolar.chess;

import com.andreiolar.chess.gui.ChessGui;
import com.andreiolar.chess.logic.ChessGame;
import com.andreiolar.chess.logic.Piece;

/**
 * Main class used to start the game.
 * 
 * @author Andrei Olar
 **/
public class Main {

	public static void main(String[] args) {
		ChessGame chessGame = new ChessGame();

		new ChessGui(chessGame);

		chessGame.setPlayer(Piece.COLOR_WHITE, null);
		chessGame.setPlayer(Piece.COLOR_BLACK, null);

		new Thread(chessGame).start();
	}

}
