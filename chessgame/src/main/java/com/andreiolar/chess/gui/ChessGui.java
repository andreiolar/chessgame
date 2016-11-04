package com.andreiolar.chess.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.andreiolar.chess.logic.ChessGame;
import com.andreiolar.chess.logic.IPlayerHandler;
import com.andreiolar.chess.logic.Move;
import com.andreiolar.chess.logic.MoveValidator;
import com.andreiolar.chess.logic.Piece;

public class ChessGui extends JPanel implements IPlayerHandler {

	private static final long serialVersionUID = 7612555717662967894L;

	private static final int BOARD_START_X = 299;
	private static final int BOARD_START_Y = 49;

	private static final int SQUARE_WIDTH = 50;
	private static final int SQUARE_HEIGHT = 50;

	private static final int PIECE_WIDTH = 48;
	private static final int PIECE_HEIGHT = 48;

	private static final int PIECES_START_X = BOARD_START_X + (int) (SQUARE_WIDTH / 2.0 - PIECE_WIDTH / 2.0);
	private static final int PIECES_START_Y = BOARD_START_Y + (int) (SQUARE_HEIGHT / 2.0 - PIECE_HEIGHT / 2.0);

	private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int) (PIECE_WIDTH / 2.0);
	private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int) (PIECE_HEIGHT / 2.0);

	private Image imgBackground;
	private JLabel gameStateLabel;

	private ChessGame chessGame;
	private List<GuiPiece> guiPieces = new ArrayList<>();

	private GuiPiece dragPiece;

	private Move lastMove;
	private Move currentMove;

	private boolean draggingGamePiecesEnabled;

	public ChessGui(ChessGame chessGame) {
		this.setLayout(null);

		// Load and set background images
		URL urlBackgroundImg = getClass().getResource("/images/bo.png");
		this.imgBackground = new ImageIcon(urlBackgroundImg).getImage();

		// Create chess game
		this.chessGame = chessGame;

		for (Piece piece : this.chessGame.getPieces()) {
			createAndAddGuiPiece(piece);
		}

		// Add mouse listener to enable drag and drop
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this.guiPieces, this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);

		// Label to display the game state
		String labelText = this.getGameStateAsText();
		this.gameStateLabel = new JLabel(labelText);
		gameStateLabel.setBounds(0, 30, 80, 30);
		gameStateLabel.setForeground(Color.WHITE);
		this.add(gameStateLabel);

		// Create application frame and set it visible
		JFrame f = new JFrame();
		f.setSize(80, 80);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setSize(this.imgBackground.getWidth(null), this.imgBackground.getHeight(null));
	}

	private String getGameStateAsText() {
		String state = "unknown";
		switch (this.chessGame.getGameState()) {
			case ChessGame.GAME_STATE_BLACK :
				state = "black";
				break;
			case ChessGame.GAME_STATE_END_WHITE_WON :
				state = "white won";
				break;
			case ChessGame.GAME_STATE_END_BLACK_WON :
				state = "black won";
				break;
			case ChessGame.GAME_STATE_WHITE :
				state = "white";
				break;
		}
		return state;
	}

	private void createAndAddGuiPiece(Piece piece) {
		Image img = this.getImageForPiece(piece.getColor(), piece.getType());
		GuiPiece guiPiece = new GuiPiece(img, piece);
		this.guiPieces.add(guiPiece);
	}

	private Image getImageForPiece(int color, int type) {
		String filename = "";
		filename += (color == Piece.COLOR_WHITE ? "w" : "b");

		switch (type) {
			case Piece.TYPE_BISHOP :
				filename += "b";
				break;
			case Piece.TYPE_KING :
				filename += "k";
				break;
			case Piece.TYPE_KNIGHT :
				filename += "n";
				break;
			case Piece.TYPE_PAWN :
				filename += "p";
				break;
			case Piece.TYPE_QUEEN :
				filename += "q";
				break;
			case Piece.TYPE_ROOK :
				filename += "r";
				break;
		}

		filename += ".png";

		URL urlPieceImg = getClass().getResource("/images/" + filename);
		return new ImageIcon(urlPieceImg).getImage();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(this.imgBackground, 0, 0, null);

		for (GuiPiece guiPiece : this.guiPieces) {
			if (!guiPiece.isCaptured()) {
				g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
			}
		}

		// Draw last move
		if (!isUserDraggingPiece() && this.lastMove != null) {
			int highlightSourceX = convertColumnToX(this.lastMove.sourceColumn);
			int highlightSourceY = convertRowToY(this.lastMove.sourceRow);
			int highlightTargetX = convertColumnToX(this.lastMove.targetColumn);
			int highlightTargetY = convertRowToY(this.lastMove.targetRow);

			g.setColor(Color.YELLOW);
			g.drawRoundRect(highlightSourceX + 4, highlightSourceY + 4, SQUARE_WIDTH - 8, SQUARE_HEIGHT - 8, 10, 10);
			g.drawRoundRect(highlightTargetX + 4, highlightTargetY + 4, SQUARE_WIDTH - 8, SQUARE_HEIGHT - 8, 10, 10);
		}

		// Draw valid target locations if user is dragging a piece
		if (isUserDraggingPiece()) {
			MoveValidator moveValidator = this.chessGame.getMoveValidator();

			// Iterate the complete board to check if target locations are valid
			for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
				for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
					int sourceRow = this.dragPiece.getPiece().getRow();
					int sourceColumn = this.dragPiece.getPiece().getColumn();

					// Check if target location is valid
					if (moveValidator.isMoveValid(new Move(sourceRow, sourceColumn, row, column), false)) {
						int highlightX = convertColumnToX(column);
						int highlightY = convertRowToY(row);

						g.setColor(Color.BLACK);
						g.drawRoundRect(highlightX + 5, highlightY + 5, SQUARE_WIDTH - 8, SQUARE_HEIGHT - 8, 10, 10);

						g.setColor(Color.GREEN);
						g.drawRoundRect(highlightX + 4, highlightY + 4, SQUARE_WIDTH - 8, SQUARE_HEIGHT - 8, 10, 10);
					}
				}
			}
		}

		this.gameStateLabel.setText(this.getGameStateAsText());
	}

	private boolean isUserDraggingPiece() {
		return this.dragPiece != null;
	}

	public int getGameState() {
		return this.chessGame.getGameState();
	}

	public static int convertColumnToX(int column) {
		return PIECES_START_X + SQUARE_WIDTH * column;
	}

	public static int convertRowToY(int row) {
		return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
	}

	public static int convertXToColumn(int x) {
		return (x - DRAG_TARGET_SQUARE_START_X) / SQUARE_WIDTH;
	}

	public static int convertYToRow(int y) {
		return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y) / SQUARE_HEIGHT;
	}

	public void setNewPieceLocation(GuiPiece dragPiece, int x, int y) {
		int targetRow = ChessGui.convertYToRow(y);
		int targetColumn = ChessGui.convertXToColumn(x);

		Move move = new Move(dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn(), targetRow, targetColumn);
		if (this.chessGame.getMoveValidator().isMoveValid(move, true)) {
			this.currentMove = move;
		} else {
			dragPiece.resetToUnderlyingPiecePosition();
		}
	}

	public void setDragPiece(GuiPiece guiPiece) {
		this.dragPiece = guiPiece;
	}

	public GuiPiece getDragPiece() {
		return this.dragPiece;
	}

	@Override
	public Move getMove() {
		this.draggingGamePiecesEnabled = true;
		Move moveForExecution = this.currentMove;
		this.currentMove = null;

		return moveForExecution;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		GuiPiece guiPiece = this.getGuiPieceAt(move.targetRow, move.targetColumn);
		if (guiPiece == null) {
			throw new IllegalStateException("No gui piece at " + move.targetRow + "/" + move.targetColumn);
		}

		guiPiece.resetToUnderlyingPiecePosition();

		this.lastMove = move;
		this.draggingGamePiecesEnabled = false;
		this.repaint();
	}

	public boolean isDraggingGamePiecesEnabled() {
		return draggingGamePiecesEnabled;
	}

	private GuiPiece getGuiPieceAt(int targetRow, int targetColumn) {
		for (GuiPiece guiPiece : this.guiPieces) {
			if (guiPiece.getPiece().getRow() == targetRow && guiPiece.getPiece().getColumn() == targetColumn && guiPiece.isCaptured() == false) {
				return guiPiece;
			}
		}

		return null;
	}

	public static void main(String[] args) {
		ChessGame chessGame = new ChessGame();
		ChessGui chessGui = new ChessGui(chessGame);
		chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
		chessGame.setPlayer(Piece.COLOR_BLACK, chessGui);
		new Thread(chessGame).start();

	}
}
