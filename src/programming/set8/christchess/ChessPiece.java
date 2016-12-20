package programming.set8.christchess;

import acm.graphics.GPoint;

import java.util.ArrayList;
import java.util.List;

public class ChessPiece {

    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;

    public static final int NO_PLAYER = 0;
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;

    private final int type;
    private final int player;
    private int x;
    private int y;

    public static String[] pieces_player1 = {"\u2659", "\u2658", "\u2657", "\u2656", "\u2655", "\u2654"};
    public static String[] pieces_player2 = {"\u265F", "\u265E", "\u265D", "\u265C", "\u265B", "\u265A"};

    public ChessPiece(int type, int player, int x, int y) {
        this.type = type;
        this.player = player;
        this.x = x;
        this.y = y;
    }

    public ChessPiece(ChessPiece piece) {
        this(piece.getType(), piece.getPlayer(), piece.getX(), piece.getY());
    }

    public List<GPoint> getValidTargetSquares(ChessData data) {
        ArrayList<GPoint> validTargetSquares = new ArrayList<>();
        for (int x = 0; x < ChessView.columns.length; x++) {
            for (int y = 0; y < ChessView.columns.length; y++) {
                if (data.isValidMove(this, x, y)) {
                    validTargetSquares.add(new GPoint(x, y));
                }
            }
        }
        return validTargetSquares;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getPlayer() {
        return this.player;
    }

    public int getType() {
        return this.type;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        if (this.player == ChessPiece.PLAYER1) {
            return ChessPiece.pieces_player1[this.type];
        } else {
            return ChessPiece.pieces_player2[this.type];
        }
    }
}
