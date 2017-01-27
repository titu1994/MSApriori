/**
 * @date 26-Jan-17
 * @author Somshubra Majumdar
 *
 * Class of Items in an Itemset (Transaction).
 * Convenience method to allow sort according to MinSupport (via Arrays.sort())
 */
public class Item implements Comparable<Item> {

    public final int itemID;
    public final double minSupport;

    public Item(int itemID, double minSupport) {
        this.itemID = itemID;
        this.minSupport = minSupport;
    }

    @Override
    public int compareTo(Item o) {
        return (int) ((this.minSupport - o.minSupport) * 100);
    }

    @Override
    public String toString() {
        return "{Item ID: " + itemID + ", " + "MinSup: " + minSupport + "}";
    }
}
