import java.util.Arrays;

/**
 * @date 26-Jan-17
 * @author Somshubra Majumdar
 *
 * Holder class of a single transaction.
 * Provides convenience method to check if two transactions are same,
 * or if a transaction contains a specific Item object.
 *
 */
public class Transaction  {

    public final Item[] items;

    public Transaction(Item[] items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction) {
            Transaction t = (Transaction) obj;

            if (items.length == t.items.length) {
                boolean same = true;

                for (int i = 0; i < items.length; i++) {
                    if (items[i].itemID != t.items[i].itemID) {
                        same = false;
                        break;
                    }
                }

                return same;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Checks if this transaction contains a specific Item by its Item ID
     */
    public boolean containsItem(int itemID) {
        for (Item item : items) {
            if (item.itemID == itemID)
                return true;
        }
        return false;
    }

    /**
     * Checks if this transaction contains a specific Item by the Item ID in the provided Item
     */
    public boolean containsItem(Item item) {
        for (Item i : items) {
            if (i.itemID == item.itemID)
                return true;
        }
        return false;
    }

    /**
     * Checks if this transaction contains a specific set of Items provided by a transaction
     *
     * Generally used for checking if a transaction does not contain transactions specified
     * in the "Cannot Be Together" constraint
     */
    public boolean containsAllItems(Transaction t) {
        int count = 0;

        for (Item i : items) {
            for (Item ti : t.items) {
                if (ti.itemID == i.itemID) {
                    count++;
                    break;
                }
            }
        }

        return count == t.items.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
