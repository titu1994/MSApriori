import java.util.Arrays;

/**
 * @author Somshubra Majumdar
 *         <p>
 *         Holder class of a single transaction.
 *         Provides convenience method to check if two transactions are same,
 *         or if a transaction contains a specific Item object.
 * @date 26-Jan-17
 */
public class Transaction {

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
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean itemsetEquals(Item data[], Item check[]) {
        if (data.length == check.length) {
            boolean same = true;

            for (int i = 0; i < data.length; i++) {
                if (data[i].itemID != check[i].itemID) {
                    same = false;
                    break;
                }
            }

            return same;
        } else {
            return false;
        }

    }

    public static boolean containsAllItems(Item itemID[], Item checkItemID[]) {
        int count = 0;

        for (Item id : itemID) {
            for (Item checkID : checkItemID) {
                if (id.itemID == checkID.itemID) {
                    count++;
                    break;
                }
            }
        }

        return count == checkItemID.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
