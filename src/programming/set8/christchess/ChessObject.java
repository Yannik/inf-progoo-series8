package programming.set8.christchess;

import acm.graphics.GObject;

public class ChessObject<E extends GObject> {
    private final E object;
    private final int x;
    private final int y;

    public ChessObject(E object, int x, int y) {

        this.object = object;
        this.x = x;
        this.y = y;
    }


    public E getObject() {
        return this.object;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
