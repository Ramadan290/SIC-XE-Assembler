package OperandPkg;

/**
 * Each literal is an entry to the Literal Table
 *
 *
 */
public class Literal {
    public static int currentStaticAddress = 1;

    public String name, value;
    public int length, address;

    /**
     * Helper method that returns formatted attributes of a Literal
     */
    public String toString() {
        String output = String.format("%1$-20s%2$-20s %3$-7d %4$s",
                name, value.toUpperCase(), length, Integer.toHexString(address).toUpperCase());

        return output;
    }
}