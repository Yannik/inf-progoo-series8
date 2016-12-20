package programming.set8.christchess;

import acm.graphics.GPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ChessData {

    private int player;

    private int turnCount;

    private ArrayList<ChessPiece> pieces = new ArrayList<>();

    public static int[] DEFAULT_POSITIONS = {
            ChessPiece.ROOK, ChessPiece.KNIGHT, ChessPiece.BISHOP, ChessPiece.QUEEN,
            ChessPiece.KING, ChessPiece.BISHOP, ChessPiece.KNIGHT, ChessPiece.ROOK
    };

    private final HashMap<GPoint, BiFunction<ChessPiece, GPoint, Boolean>> pawnValidationLambdas = new HashMap<>();

    public void initNewGame() {
        this.player = ChessPiece.PLAYER1;

        // add pieces
        for (int x = 0; x < ChessView.columns.length; x++) {
            // pawns
            this.pieces.add(new ChessPiece(ChessPiece.PAWN, ChessPiece.PLAYER1, x, 6));
            this.pieces.add(new ChessPiece(ChessPiece.PAWN, ChessPiece.PLAYER2, x, 1));

            // other pieces
            this.pieces.add(new ChessPiece(DEFAULT_POSITIONS[x], ChessPiece.PLAYER1, x, 7));
            this.pieces.add(new ChessPiece(DEFAULT_POSITIONS[x], ChessPiece.PLAYER2, x, 0));
        }
    }

    public ChessData() {
        BiFunction<ChessPiece, GPoint, Boolean> schlagen = (chessPiece, target) -> {
            return fieldHasEnemyPiece(chessPiece.getPlayer(), (int)target.getX(), (int)target.getY());
        };

        pawnValidationLambdas.put(new GPoint(-1,1), schlagen);
        pawnValidationLambdas.put(new GPoint(1,1), schlagen);

        pawnValidationLambdas.put(new GPoint(0, 1), (chessPiece, target) -> {
            return this.getPieceAt((int)target.getX(), (int)target.getY()) == null;
        });


        pawnValidationLambdas.put(new GPoint(0, 2), (chessPiece, target) -> {
            return isPawnInStartingLine(chessPiece) && this.getPieceAt((int)target.getX(), (int)target.getY()) == null;
        });

    }

    public ChessData(ChessData data) {
        for (ChessPiece piece: data.getPieces()) {
            this.addNewPiece(new ChessPiece(piece));
        }

        this.setActivePlayer(data.getActivePlayer());

    }

    public void addNewPiece(int type, int player, int x, int y) {
        this.pieces.add(new ChessPiece(type, player, x, y));
    }

    public void addNewPiece(ChessPiece piece) {
        this.pieces.add(piece);
    }

    public void removeAllPieces() {
        this.pieces.clear();
    }

    public List<ChessPiece> getPieces() {
        return this.pieces;
    }

    public int getActivePlayer() {
        return this.player;
    }

    public int getTurn() {
        return this.turnCount;
    }

    public ChessPiece getPieceAt(int x, int y) {
        for (ChessPiece piece : pieces) {
            if (piece.getX() == x && piece.getY() == y) {
                return piece;
            }
        }
        return null;
    }

    public ChessPiece getPieceAt(String str) {
        return this.getPieceAt(stringToX(str), stringToY(str));
    }

    public static int stringToY(String str) {
        // when casting the car returned by str.charAt to int, this will return the ascii
        // value of the char instead of the numerical value. to counter this, we deduct
        // the value of the char '0'
        return 8 - (str.charAt(1) - '0');
    }

    public static int stringToX(String str) {
        // 'a' == 97, 'b' == 98
        return (int) str.charAt(0) - 97;
    }

    public boolean isValidSelection(ChessPiece piece) {
        if (piece == null || piece.getPlayer() != this.player) {
            return false;
        }
        boolean foundValidMove = false;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (this.isValidMove(piece, x, y)) {
                    foundValidMove = true;
                }
            }
        }
        return foundValidMove;
    }

    public boolean isValidMoveForPiece(ChessPiece piece, int x, int y) {
        switch (piece.getType()) {
            case ChessPiece.ROOK:
                return isValidRookMove(piece, x, y);
            case ChessPiece.BISHOP:
                return isValidBishopMove(piece, x, y);
            case ChessPiece.QUEEN:
                return isValidQueenMove(piece, x, y);
            case ChessPiece.KING:
                return isValidKingMove(piece, x, y);
            case ChessPiece.PAWN:
                return isValidPawnMove(piece, x, y);
            case ChessPiece.KNIGHT:
                return isValidKnightMove(piece, x, y);
            default:
                return true;
        }
    }
    public boolean isValidMove(ChessPiece piece, int x, int y) {
        boolean isValidMoveForPiece = isValidMoveForPiece(piece, x, y);

        if (!isValidMoveForPiece) {
            return false;
        }

        // it is necessary to create a deep copy of the chessdata object
        // i first tried to save the pieces coordinates, then move the piece
        // then run the inCheck method, then move the piece back, but this
        // does cause additional headaches if there is an enemy piece at
        // the new x/y because this will produce invalid inCheck results
        ChessData newData = new ChessData(this);
        newData.movePieceTo(newData.getPieceAt(piece.getX(), piece.getY()), x, y);

        // to be valid, the move must not leave the king in check
        // this could be achieved by either moving the king or capturing the
        // piece that would put the player in check, or moving into the way of the
        // piece. also, this prevents that the king puts himself into check.
        boolean putsPlayerInCheck = (newData.isInCheck() == piece.getPlayer());
        return !putsPlayerInCheck;
    }

    // we explicitly pass the own player to this function,
    // as using the player whos turn it is would produce invalid
    // results if we are simulating the next turn of the other
    // player (for checking that a player does not put himself
    // into check)
    private boolean fieldHasOwnPiece(int player, int x, int y) {
        ChessPiece piece =  this.getPieceAt(x, y);
        return piece != null && piece.getPlayer() == player;
    }

    private boolean fieldHasEnemyPiece(int player, int x, int y) {
        ChessPiece piece =  this.getPieceAt(x, y);
        return piece != null && piece.getPlayer() != player;
    }

    public boolean isValidKnightMove(ChessPiece piece, int x, int y) {
        int deltaX = piece.getX() - x;
        int deltaY = piece.getY() - y;

        List<GPoint> validOffsets = new ArrayList<>();
        /*
        validOffsets.add(new GPoint(1, 2));
        validOffsets.add(new GPoint(2, 1));
        validOffsets.add(new GPoint(-1, 2));
        validOffsets.add(new GPoint(-2, 1));
        ....
       */
        BiConsumer<Integer, Integer> addPermutation = (xMod, yMod) -> {
            validOffsets.add(new GPoint(1*xMod, 2*yMod));
            validOffsets.add(new GPoint(2*xMod, 1*yMod));
        };

        addPermutation.accept(1, 1);
        addPermutation.accept(-1, 1);
        addPermutation.accept(1, -1);
        addPermutation.accept(-1,- 1);

       for (GPoint validOffset : validOffsets) {
           if (deltaX == validOffset.getX() && deltaY == validOffset.getY() && !fieldHasOwnPiece(piece.getPlayer(), x, y)) {
               return true;
           }
       }
       return false;

    }

    public boolean isPawnInStartingLine(ChessPiece piece) {
        if (piece.getPlayer() == ChessPiece.PLAYER1 && piece.getY() == 6) {
            return true;
        }
        if (piece.getPlayer() == ChessPiece.PLAYER2 && piece.getY() == 1) {
            return true;
        }
        return false;
    }

    private boolean isValidPawnMove(ChessPiece piece, int x, int y) {
        int playerModifier = 1;
        if (piece.getPlayer() == ChessPiece.PLAYER2) {
            playerModifier = -1;
        }

        int deltaX = (piece.getX() - x) * playerModifier;
        int deltaY = (piece.getY() - y) * playerModifier;

        BiFunction<ChessPiece, GPoint, Boolean> lambda;
        if ((lambda = pawnValidationLambdas.get(new GPoint(deltaX, deltaY))) == null) {
            return false;
        }

        return lambda.apply(piece, new GPoint(x, y));

    }

    private boolean isValidKingMove(ChessPiece piece, int x, int y) {
        if (piece.getX() == x && piece.getY() == y) {
            return false;
        }

        int absDeltaX = Math.abs(piece.getX() - x);
        int absDeltaY = Math.abs(piece.getY() - y);

        return absDeltaX <= 1 && absDeltaY <= 1 && !fieldHasOwnPiece(piece.getPlayer(), x, y);

    }

    private boolean isValidQueenMove(ChessPiece piece, int x, int y) {
        return isValidBishopMove(piece, x, y) || isValidRookMove(piece, x, y);
    }

    // Läufer
    private boolean isValidBishopMove(ChessPiece piece, int x, int y) {
        return isValidRunMove(piece, 1, 1, x, y) ||
                isValidRunMove(piece, 1, -1, x, y) ||
                isValidRunMove(piece, -1, -1, x, y) ||
                isValidRunMove(piece, -1, 1, x, y);
    }

    // Turm
    private boolean isValidRookMove(ChessPiece piece, int x, int y) {
        return isValidRunMove(piece, 1, 0, x, y) ||
                isValidRunMove(piece, -1, 0, x, y) ||
                isValidRunMove(piece, 0, 1, x, y) ||
                isValidRunMove(piece, 0, -1, x, y);
    }

    private boolean isValidRunMove(ChessPiece piece, int xOffset, int yOffset, int targetX, int targetY) {
        int currX = piece.getX();
        int currY = piece.getY();
        for (int i = 0; i < 8; i++) {
            currX += xOffset;
            currY += yOffset;

            if (currX == targetX && currY == targetY && !fieldHasOwnPiece(piece.getPlayer(), currX, currY)) {
                return true;
            }

            if (this.getPieceAt(currX, currY) != null) {
                return false;
            }
        }
        return false;
    }

    public ChessPiece movePieceTo(ChessPiece piece, int x, int y) {
        turnCount++;
        piece.moveTo(x, y);

        Iterator<ChessPiece> i = pieces.iterator();
        while (i.hasNext()) {
            ChessPiece otherPiece = i.next();
            if (otherPiece != piece && otherPiece.getX() == x && otherPiece.getY() == y) {
                i.remove();
                return otherPiece;
            }
        }
        return null;
    }

    public void setActivePlayer(int player) {
        this.player = player;
    }

    public void togglePlayer() {
        if (this.player == ChessPiece.PLAYER1) {
            this.player = ChessPiece.PLAYER2;
        } else {
            this.player = ChessPiece.PLAYER1;
        }
    }

    public int isInCheck() {
        for (ChessPiece piece: pieces) {
            if (piece.getType() == ChessPiece.KING) {
                for (ChessPiece controlPiece : pieces) {
                    if (piece.getPlayer() == controlPiece.getPlayer()) {
                        continue;
                    }

                    if (this.isValidMoveForPiece(controlPiece, piece.getX(), piece.getY())) {
                        return piece.getPlayer();
                    }
                }
            }
        }
        return ChessPiece.NO_PLAYER;
    }

    public int isCheckmate() {
        int playerInCheck = isInCheck();

        if (playerInCheck != ChessPiece.NO_PLAYER) {
            // check if any of the players pieces can make a move that prevents being in check
            for (ChessPiece piece: pieces) {
                if (piece.getPlayer() == playerInCheck) {
                    if (!isValidSelection(piece)) { // only valid if the piece can move without leaving the player in check
                        return playerInCheck;
                    } else {
                        return ChessPiece.NO_PLAYER;
                    }
                }
            }
        }
        return ChessPiece.NO_PLAYER;
    }
}
