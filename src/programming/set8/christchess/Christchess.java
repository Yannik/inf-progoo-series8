package programming.set8.christchess;

import acm.graphics.GCanvas;
import acm.program.GraphicsProgram;

/**
 * This class controls a simplified chess game.
 */
@SuppressWarnings("serial")
public class Christchess extends GraphicsProgram {

    @Override
    public void run() {
        // Create the view using the programs canvas and
        // set the size appropriately.
        GCanvas canvas = this.getGCanvas();
        canvas.setSize(1400,1400);
        ChessView chessView = new ChessView(this.getGCanvas());
        setSize(getGCanvas().getWidth(), getGCanvas().getHeight());

        // Create the game data object and
        // initialize a game.
        ChessData chessData = new ChessData();
        chessData.initNewGame();
        chessView.init(chessData);

        // Main game loop
        do {
            ChessPiece piece = null;
            String moveStr;
            int x = -1;
            int y = -1;
            do {
                // Ask the user to select a piece.
                do {
                    String selectStr = readLine((chessData.getActivePlayer() == ChessPiece.PLAYER1 ? "White" : "Black") + ", select a piece: ");
                    piece = chessData.getPieceAt(selectStr);
                } while (!chessData.isValidSelection(piece));

                // Display which squares are valid moves.
                chessView.updateValidMoves(chessData, piece);

                // Ask the user where to move the selected piece.
                do {
                    moveStr = readLine("Move " + piece + " to: ");
                    if (moveStr.equals("c")) break;
                    x = ChessData.stringToX(moveStr);
                    y = ChessData.stringToY(moveStr);
                } while (!chessData.isValidMove(piece, x, y));

                // Reset the visual for the valid moves.
                chessView.updateValidMoves(chessData, null);

            } while (moveStr.equals("c"));

            // Move the piece and check for captured pieces.
            ChessPiece capturedPiece = chessData.movePieceTo(piece, x, y);
            if (capturedPiece != null) {
                println("Capture: " + piece + " captures " + capturedPiece);
            }

            // Change the player and update the board.
            chessData.togglePlayer();
            chessView.update(chessData);

            // Repeat until a player is checkmate.
        } while (chessData.isCheckmate() == ChessPiece.NO_PLAYER);
        println("Player " +
                (chessData.isCheckmate() == ChessPiece.PLAYER1 ? "White" : "Black") +
                " is checkmate.");
    }

}