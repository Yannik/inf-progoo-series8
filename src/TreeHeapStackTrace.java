import acm.program.*;
import acm.graphics.*;

/*

Part b:
We can reduce the number of stack frames (=drawTree method calls) to the number of
GLine objects by checking the condition `len > 2` before calling drawTree:

        if (len * 0.75 > 2) {
            drawTree(x1, y1, len * 0.75, angle + 30);
        }
        if (len * 0.66 > 2) {
            drawTree(x1, y1, len * 0.66, angle - 50);
        }

This way, drawTree will only be called if an GLine will actually be drawn.

Part c:

->>> Explain the different parts of memory (stack, heap, static segment) and which data are stored in them. List examples from your solution in Part a).
Stack: The stack contains the stack frames, which are created on each method call and hold the local variables of that
       method call, including pointers to objects on the heap.
       The stack starts from the end of the memory allocated for the program.
       In this class, each time drawTree is called, a new stack frame is added to the stack, which contains the variables
       x1 and y1. When the method returns (either due to all statements being executed or due to an early return call),
       the stack frame is removed from the stack, as the local variables are not required anymore.

Heap: All objects that are created are placed on the heap. The heap starts from the beginning of the memory allocated for
      the program.
      In this class, all GLine objects are placed on the heap.

Static segment: The static segment contains data that is not modified during program execution, for example
                instance variables created with the modifier static or enums.
                In this class, this would be the variables initialStackAddress and initialHeapAddress.

Recursion: A method which calls itself is a recursive method. Recursive methods most always have a condition under which
           it will call itself again, but walks up the recursive tree again, or it will result in a loop.

Mutable class: A mutable class is the oppositive of an immutable class, meaning that it's properties can be modified
               beyond the initialization done by the constructor.

Wrapper class: Java has so-called `wrapper classes` for all its primitive types. This means, that for each primitive
               type, there is a corresponding class type. Example: int->Integer.
               This is necessary, as a primitive type can (by definition) not have any methods (while the wrapper objects
               do) and because many java library methods require objects. For example, a List (which has a generic type)
               can only be of type Double (the class wrapper), as generic types can only be subtypes of java.lang.Object.
               The reason why primitive types exist at all is that they are more memory efficient.

Boxing/Unboxing: Boxing/Unboxing is the automatic casting from a primitive type to its corresponding wrapper class.

Garbage collection: Garbage collection is reusing memory that is not needed by the program anymore.
                    Example: If there are no more references (pointers) to an object on the heap, its memory can therefore
                    be freed.
 */

/**
 * This class is used to recursively print a tree
 */
public class TreeHeapStackTrace extends GraphicsProgram {

    // keeps track of the current depth of the recursion
    private int depth = 0;

    // total number of created GLines
    private int GLineCount = 0;

    // 0x100000
    private static int initialHeapAddress = 0x100000;

    private static int initialStackAddress = 0xffffff;

    // total number of calls to drawTree
    private int drawTreeCalls = 0;

    // maximum depth of the recursion
    private int maxDepth = 0;

    public void run() {
        setSize(500, 350);
        //drawTree(250, 350, 3.5, 90);
        drawTree(250, 350, 100, 90);   // ADJUST "TREE PARAMETERS" HERE
    }

    /**
     * This method recursively draws tree parts until the individual part length is < 2
     *
     * @param x0 x coordinate of the tree part
     * @param y0 y coordinate of the tree part
     * @param len the length of the tree part
     * @param angle angle of the tree part
     */
    public void drawTree(double x0, double y0,
                         double len, double angle) {
        drawTreeCalls++;

        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }

        // +1, as the first byte is at the `0`th address on the stack
        String currentStackAdress = toHexString(initialStackAddress - depth * 28 + 1);
        println(String.format("Create drawTree() stack frame at address %s, depth %d",
                currentStackAdress,
                depth
        ));

        if (len > 2) {
            double x1 = x0 + len * GMath.cosDegrees(angle);
            double y1 = y0 - len * GMath.sinDegrees(angle);
            add(new GLine(x0, y0, x1, y1));
            println(String.format(
                    "Create GLine object #%d at address %s",
                    ++GLineCount,
                    toHexString(initialHeapAddress + 20 * (GLineCount - 1))
            ));
            drawTree(x1, y1, len * 0.75, angle + 30);
            drawTree(x1, y1, len * 0.66, angle - 50);
        }


        println(String.format("Delete stack frame at address %s, depth %d",
                currentStackAdress,
                depth
        ));
        depth--;

        if (depth == 0) {
            println();
            println("HEAP:");
            println("Created " + GLineCount + " GLine objects,");

            /* each GLine takes exactly 20 bytes, as we know the number
            of  GLines, we can easily calculate the last heap address */
            println("requiring " + GLineCount * 20 + " bytes of heap space,");

            println(String.format(
                    "from address %s to %s.",
                    toHexString(initialHeapAddress),
                    // -1 because the first byte is 0x10000 and not 0x10001 (count from 0)
                    toHexString(initialHeapAddress + GLineCount * 20 - 1)
            ));

            println();
            println("STACK:");
            println("Created and discarded " + drawTreeCalls + " drawTree() stack frames,");
            println("with maximal depth " + maxDepth + ",");
            println("requiring " + 28 * maxDepth + " bytes of stack space,");
            println(String.format(
                    "from address %s to %s.",
                    // + 1 because the "starting address" is used as first byte as well (count from 0)
                    toHexString(initialStackAddress - 28 * maxDepth + 1),
                    toHexString(initialStackAddress)
            ));

        }
    }

    /**
     * @param decimal a positive decimal value
     * @return the hexadecimal representation of the decimal given as input
     */
    public String toHexString(int decimal) {
        String hexadecimal = "";

        do {
            int remainder = decimal % 16;

            if (remainder < 10) {
                hexadecimal = remainder + hexadecimal;
            } else {
                hexadecimal = (char) ('a' + remainder - 10) + hexadecimal;
            }

        } while ((decimal = decimal / 16) > 0);

        return "0x" + hexadecimal;
    }
}