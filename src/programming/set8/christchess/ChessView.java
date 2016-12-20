package programming.set8.christchess;

import acm.graphics.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ChessView {

    private final GCanvas gc;
    private final double chessfieldWidth;
    private final double colNameWidth;
    public static String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H"};
    public static String[] rows = {"8", "7", "6", "5", "4", "3", "2", "1" };

    private final Font labelFont;

    private ArrayList<GObject> pieceObjects = new ArrayList<>();

    public ArrayList<ChessObject<GRect>> squares = new ArrayList<>();

    public ChessView(GCanvas gc) {
        this.gc = gc;
        this.colNameWidth = ((double) Math.min(gc.getHeight(), gc.getWidth())) * 0.1;
        this.chessfieldWidth = ((double) Math.min(gc.getHeight(), gc.getWidth()) - colNameWidth * 2) / ChessView.columns.length;
        labelFont = new Font(null, 0 , (int)(chessfieldWidth * 0.3));
    }

    public void init(ChessData data) {
        gc.removeAll();
        drawLabels();
        drawChessboard();
        update(data);
    }

    public void update(ChessData data) {
        Iterator<GObject> i = gc.iterator();
        while (i.hasNext()) {
            GObject object = i.next();
            if (pieceObjects.contains(object)) {
                i.remove();
            }
        }
        pieceObjects.clear();
        for (ChessPiece piece : data.getPieces()) {
            drawPiece(piece);
        }
    }

    public void updateValidMoves(ChessData data, ChessPiece piece) {
        // TODO: clean null check
        for (ChessObject<GRect> square : squares) {
            GRect squareObject = square.getObject();
            if (piece == null) {
                squareObject.setFillColor(getSquareColor(square.getY(), square.getX()));
            } else
            if (data.isValidMove(piece, square.getX(), square.getY())) {
                if (data.getPieceAt(square.getX(), square.getY()) == null) {
                    squareObject.setFillColor(Color.GREEN);
                } else {
                    squareObject.setFillColor(Color.RED);
                }
            } else {
                squareObject.setFillColor(getSquareColor(square.getY(), square.getX()));
            }
        }
    }

    public void drawPiece(ChessPiece piece) {
        String unicodeChar = piece.toString();

        GLabel labelPiece = new GLabel(unicodeChar);
        labelPiece.setFont(Font.decode("SansSerif-" + (int)Math.floor(chessfieldWidth * 0.9)));
        pieceObjects.add(labelPiece);

        gc.add(labelPiece,
                colNameWidth + chessfieldWidth * piece.getX() + elementHorizontalCenter(labelPiece, chessfieldWidth),
                colNameWidth + chessfieldWidth * piece.getY() + elementVerticalCenter(labelPiece, chessfieldWidth));
    }

    private Color getSquareColor(int row, int column) {
        if ((row + column) % 2 == 1) {
            return Color.LIGHT_GRAY;
        }
        return Color.WHITE;
    }

    public void drawChessboard() {
        for (int rowNumber = 0; rowNumber < rows.length; rowNumber++) {
            for (int columnNumber = 0; columnNumber < columns.length; columnNumber++) {
                Color color = getSquareColor(rowNumber, columnNumber);
                drawSquare(columnNumber, rowNumber, color);
            }

        }
    }

    public void drawLabels() {
        printColumnNames(0);
        printColumnNames(colNameWidth + chessfieldWidth * rows.length);

        for (int rowNumber = 0; rowNumber < rows.length; rowNumber++) {
            GLabel preLabel = new GLabel(rows[rowNumber]);
            preLabel.setFont(labelFont);
            GLabel postLabel = new GLabel(rows[rowNumber]);
            postLabel.setFont(labelFont);
            gc.add(preLabel,
                    elementHorizontalCenter(preLabel, colNameWidth),
                    colNameWidth + chessfieldWidth * rowNumber + elementVerticalCenter(preLabel, chessfieldWidth));
            gc.add(postLabel,
                    elementHorizontalCenter(postLabel, colNameWidth) + colNameWidth + chessfieldWidth * columns.length,
                    colNameWidth + chessfieldWidth * rowNumber + elementVerticalCenter(postLabel, chessfieldWidth));

        }
    }

    /**
     * Draws the square identified by {@code x} and {@code y}
     * in the color {@code color}.
     *
     * @param x
     *            file index
     * @param y
     *            rank index
     * @param color
     *            chosen color
     */
    public void drawSquare(int x, int y, Color color) {
        double coordinateX = colNameWidth + chessfieldWidth * x;
        double coordinateY = colNameWidth + chessfieldWidth * y;

        GRect field = new GRect(chessfieldWidth, chessfieldWidth);
        field.setFilled(true);
        field.setFillColor(color);
        gc.add(field, coordinateX, coordinateY);
        squares.add(new ChessObject<>(field, x, y));
    }

    public void printColumnNames(double height) {
        for (int i=0; i < columns.length; i++) {
            GLabel label = new GLabel(columns[i]);
            label.setFont(labelFont);
            gc.add(label,
                    colNameWidth + chessfieldWidth * i + elementHorizontalCenter(label, chessfieldWidth),
                    height + elementVerticalCenter(label, colNameWidth));
        }
    }

    public static double elementHorizontalCenter(GObject object, double width) {
        return (width - object.getWidth()) / 2;
    }

    public static double elementVerticalCenter(GObject object, double height) {
        if (object instanceof GLabel) {
            return (height + ((GLabel)object).getAscent()) / 2;
        }
        return (height - object.getHeight()) / 2;
    }


}
