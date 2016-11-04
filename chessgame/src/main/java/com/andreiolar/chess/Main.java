package com.andreiolar.chess;

import com.andreiolar.chess.ai.SimpleAiPlayerHandler;
import com.andreiolar.chess.gui.ChessGui;
import com.andreiolar.chess.logic.ChessGame;
import com.andreiolar.chess.logic.Piece;

public class Main {

	public static void main(String[] args) {
		ChessGame chessGame = new ChessGame();

		ChessGui chessGui = new ChessGui(chessGame);
		SimpleAiPlayerHandler ai = new SimpleAiPlayerHandler(chessGame);

		chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
		chessGame.setPlayer(Piece.COLOR_BLACK, ai);

		new Thread(chessGame).start();
	}

}
