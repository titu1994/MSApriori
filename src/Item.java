/**
 * @date 26-Jan-17
 * @author Somshubra Majumdar
 *
 * Class of Items in an Itemset (Transaction).
 * Convenience method to allow sort according to MinSupport (via Arrays.sort())
 */
public class Item implements Comparable<Item>, Cloneable {

    public final int itemID;
    public final double minSupport;

    public Item(int itemID, double minSupport) {
        this.itemID = itemID;
        this.minSupport = minSupport;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof Item) {
            Item t = (Item) obj;

            if (this.itemID == t.itemID)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Item o) {
        int val = (int) ((this.minSupport - o.minSupport) * 100);

        if (val == 0)
            return this.itemID - o.itemID;
        else
            return val;
    }

    @Override
    public String toString() {
        return  "" + itemID;
    }

    public String toString(boolean displayMinSup) {
        if (displayMinSup) {
            return "" + itemID + ", " + "MinSup: " + minSupport;
        }
        else {
            return "" + itemID;
        }
    }

    @Override
    protected Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
