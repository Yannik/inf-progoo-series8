package programming.set8.christchess;

import acm.graphics.GPoint;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

public class ChessData {

    private int player;

    private int turnCount;

    private ArrayList<ChessPiece> pieces = new ArrayList<>();

    private static int[] DEFAULT_POSITIONS = {
            ChessPiece.ROOK, ChessPiece.KNIGHT, ChessPiece.BISHOP, ChessPiece.QUEEN,
            ChessPiece.KING, ChessPiece.BISHOP, ChessPiece.KNIGHT, ChessPiece.ROOK
    };

    private final HashMap<GPoint, BiPredicate<ChessPiece, GPoint>> pawnValidationLambdas = new HashMap<>();

    public void initNewGame() {
        this.player = ChessPiece.PLAYER1;

        // add pieces
        for (int x = 0; x < ChessView.columns.length; x++) {
            // pawns
            addNewPiece(ChessPiece.PAWN, ChessPiece.PLAYER1, x, 6);
            addNewPiece(ChessPiece.PAWN, ChessPiece.PLAYER2, x, 1);

            // other pieces
            addNewPiece(DEFAULT_POSITIONS[x], ChessPiece.PLAYER1, x, 7);
            addNewPiece(DEFAULT_POSITIONS[x], ChessPiece.PLAYER2, x, 0);
        }
    }

    public ChessData() {
        BiPredicate<ChessPiece, GPoint> schlagen = (chessPiece, target) ->
            fieldHasEnemyPiece(chessPiece.getPlayer(), (int)target.getX(), (int)target.getY());

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

    public ChessPiece addNewPiece(int type, int player, int x, int y) {
        ChessPiece piece = new ChessPiece(type, player, x, y);
        this.pieces.add(piece);
        return piece;
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
        return pieces.stream().filter(p -> p.getX() == x && p.getY() == y).findFirst().orElse(null);
    }

    public ChessPiece getPieceAt(String str) {
        return this.getPieceAt(stringToX(str), stringToY(str));
    }

    public static int stringToY(String str) {
        if (str.length() < 2)
            return -1;
        return '8' - str.charAt(1);
    }

    public static int stringToX(String str) {
        if (str.length() < 2)
            return -1;
        return str.charAt(0) - 'a';
    }

    public boolean isValidSelection(ChessPiece piece) {
        if (piece == null || piece.getPlayer() != this.player) {
            return false;
        }

        return IntStream.rangeClosed(0,7).anyMatch( x -> {
            return IntStream.rangeClosed(0,7).anyMatch( y -> {
                return this.isValidMove(piece, x, y);
            });
        });
    }

    public boolean isValidMoveForPiece(ChessPiece piece, int x, int y) {

        if (fieldHasOwnPiece(piece.getPlayer(), x, y)) {
            return false;
        }

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
                return false;
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
        return !(newData.isInCheck() == piece.getPlayer());
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

    private boolean isValidKnightMove(ChessPiece piece, int x, int y) {
        int absDeltaX = Math.abs(piece.getX() - x);
        int absDeltaY = Math.abs(piece.getY() - y);

        return (absDeltaX == 1 && absDeltaY == 2) || (absDeltaX == 2 && absDeltaY == 1);
    }

    private boolean isPawnInStartingLine(ChessPiece piece) {
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

        BiPredicate<ChessPiece, GPoint> lambda;
        if ((lambda = pawnValidationLambdas.get(new GPoint(deltaX, deltaY))) == null) {
            return false;
        }

        return lambda.test(piece, new GPoint(x, y));

    }

    private boolean isValidKingMove(ChessPiece piece, int x, int y) {
        if (piece.getX() == x && piece.getY() == y) {
            return false;
        }

        int absDeltaX = Math.abs(piece.getX() - x);
        int absDeltaY = Math.abs(piece.getY() - y);

        return absDeltaX <= 1 && absDeltaY <= 1;

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

            if (currX == targetX && currY == targetY) {
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

        ChessPiece captured = pieces
                .stream()
                .filter( p -> p != piece && p.getX() == x && p.getY() == y )
                .findFirst()
                .orElse(null);

        pieces.remove(captured);

        return captured;
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
        return pieces
                .stream()
                .filter(piece -> piece.getType() == ChessPiece.KING)
                .filter(king -> {
                    return pieces.stream().anyMatch(p -> this.isValidMoveForPiece(p, king.getX(), king.getY()));
                })
                .findFirst()
                .map(ChessPiece::getPlayer)
                .orElse(ChessPiece.NO_PLAYER);
    }

    public int isCheckmate() {
        int playerInCheck = isInCheck();

        return pieces
                .stream()
                .filter(p -> p.getPlayer() == playerInCheck)
                .filter(this::isValidSelection)
                .mapToInt(p -> ChessPiece.NO_PLAYER)
                .findFirst()
                .orElse(playerInCheck);
    }
}
