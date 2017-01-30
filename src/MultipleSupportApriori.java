import java.util.*;

/**
 * @date 24-Jan-17
 * @author Omid, Somshubra Majumdar
 *
 * Implementation of Multiple Support Apriori algorithm.
 */
public class MultipleSupportApriori {

    private static Pair counts[];
    private static Pair initialPairs[];
    private static Pair prevCounts[];
    private static Pair globalItemCounts[];

    private static ArrayList<Pair[]> kItemsetList;

    private static int itemSetCount = 1;
    private static double minSupport = 0.0;

    public static void initCounter() {
        counts = new Pair[IOUtils.itemIDCount];
        globalItemCounts = new Pair[IOUtils.transactionCount];

        kItemsetList = new ArrayList<>();

        // Temporary map which provides mapping from Item ID to 'counts' array index
        LinkedHashMap<Integer, Integer> countIndexMap = new LinkedHashMap<>();

        int x = 0, y = 0;
        for (Map.Entry<Integer, Double> entry : IOUtils.minSupports.entrySet()) {
            counts[x] = new Pair();
            counts[x++].items = new Item[]{new Item(entry.getKey(), entry.getValue())};

            countIndexMap.put(entry.getKey(), x - 1); // Preserve item id mapping to index of counts array
        }

        int cindex = 0;
        x = 0;

        for (Transaction t : IOUtils.transactions) {
            globalItemCounts[x] = new Pair();
            globalItemCounts[x].items = new Item[t.items.length];
            y = 0;

            for (Item item : t.items) { // for each char in each string input
                cindex = countIndexMap.get(item.itemID); // returns the index in counts array for a specific item id
                counts[cindex].pairSupport++; // increase support val of 1, 2, 3, ... input data points
                globalItemCounts[x].items[y] = item.clone(); // initialize to the value of input (1, 3, 4), (2, 3, 5) and so on
                y++; // Next item
            }
            x++; // Next transaction
        }

        // Calculate initial minimum support for each item id
        for (Pair count : counts) {
            count.pairSupport /= IOUtils.transactionCount;
        }

        initialPairs = new Pair[IOUtils.itemIDCount]; // Actual first candidates
        System.arraycopy(counts, 0, initialPairs, 0, counts.length); // same as counts so copy
    }

    public static void computeCandidates() {
        ArrayList<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < counts.length; i++) { // search each and every pair of counts
            for (int j = i + 1; j < counts.length; j++) { // with all other pairs
                Item itemset[] = new Item[itemSetCount + 1]; // Increase size of itemset by 1
                Item itemsI[] = counts[i].items; // Convenience to select Current itemset
                Item itemsJ[] = counts[j].items; // Convenience to select all other itemset iteratively

                Arrays.sort(itemsI); // Sort according to Minimum Item Support
                Arrays.sort(itemsJ); // Sort according to Minimum Item Support
                System.arraycopy(itemsI, 0, itemset, 0, itemsI.length); // Copy old itemsI, only increase by 1

                if(itemsJ.length > 1) { // need to generalize
                    int noOfPosToCheck = itemsJ.length - 1;
                    boolean test = true; // test to see if

                    for(int k = 0; k < noOfPosToCheck; k++) {
                        if(!itemsI[k].equals(itemsJ[k])) { // break if intermediate values are different between I and J
                            test = false;
                            break;
                        }
                    }

                    if(test) {
                        itemset[itemset.length - 1] = itemsJ[itemsJ.length - 1]; // merge I with J (last element of J), remaining same
                    }
                    else {
                        continue; // search next candidate if fit to merge
                    }
                }
                else {
                    // special case for 1st iteration, where Item J has only 1 item.
                    // generates : {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 3}, {2, 4}, {2, 5},
                    // {3, 4}, {3, 5}, {4, 5}
                    itemset[itemset.length - 1] = counts[j].items[0]; // select next item after this one to be merged
                }

                int supportCount = 0;

                // Compare the new item set to see if old one contains these items, computing support count for k-itemset
                for(Pair p : globalItemCounts) {
                    if(Pair.containsAll(p.items, itemset)) {
                        supportCount++;
                    }
                }

                Pair p = new Pair();
                p.pairSupport = supportCount;
                p.items = itemset;

                pairs.add(p); // Construct the new merged k-itemset
            }
        }

        prevCounts = counts; // preserve last counts for later use
        counts = pairs.toArray(new Pair[0]); // counts now has all of the new candidates
        itemSetCount++; // marks the k in k-itemset
    }


    public static void removeLessSupport() {
        ArrayList<Pair> pairs = new ArrayList<>();

        for (Pair p : counts) { // Add all pairs who satisfy the min support criterion
            double minimumSupport = 1.0;

            for (Item item : p.items) { // calculate the minimum item support of all items in that pair
                if (item.minSupport <= minimumSupport)
                    minimumSupport = item.minSupport;
            }

            if (p.pairSupport >= minimumSupport)
                pairs.add(p); // select only those pairs which have value above min support
        }

        // Remove all pairs who do not satisfy the support difference constraint
        pairs = handleSupportDifferenceConstraint(pairs);

        // Remove all pairs which have items that cannot be together
        pairs = handleCannotBeTogether(pairs);

//        // Remove all pairs which do not have at least one of the Must Have category
//        pairs = handleAtleastOneMustBePresent(pairs);

        counts = pairs.toArray(new Pair[0]); // replace counts with reduced set
    }

    public static ArrayList<Pair> handleSupportDifferenceConstraint(ArrayList<Pair> pairs) {
        ArrayList<Pair> reducedPairs = new ArrayList<>();

        for (Pair p : pairs) {
            double max = 0.0, min = 1.0;

            for (Item item : p.items) {
                if (item.minSupport >= max)
                    max = item.minSupport;


                if (item.minSupport < min)
                    min = item.minSupport;
            }

            if ((max - min) <= IOUtils.supportDifferenceConstraint)
                reducedPairs.add(p);
        }
        return reducedPairs;
    }

    public static ArrayList<Pair> handleCannotBeTogether(ArrayList<Pair> pairs) {
        ArrayList<Pair> reducedPair = new ArrayList<>();

        for (Pair p : pairs) {
            boolean testIfPairHasNoneOfCannotBeTogetherItems = true;

            for (Transaction t : IOUtils.cannotBeTogether) {
                if (Transaction.containsAllItems(p.items, t.items)) {
                    testIfPairHasNoneOfCannotBeTogetherItems = false;
                    break;
                }
            }

            if (testIfPairHasNoneOfCannotBeTogetherItems)
                reducedPair.add(p);
        }

        return reducedPair;
    }


    public static ArrayList<Pair> handleAtleastOneMustBePresent(ArrayList<Pair> pairs) {
        ArrayList<Pair> reducedPair = new ArrayList<>();

        for (Pair p : pairs) {
            boolean hasAtLeastOneMatchingItem = false;

            for (int id : IOUtils.mustContain) {
                if (p.containsItem(id))
                    hasAtLeastOneMatchingItem |= true;
            }

            if (hasAtLeastOneMatchingItem)
                reducedPair.add(p);
        }

        return reducedPair;
    }


    private static class Pair {

        public Item items[];
        public double pairSupport = 0.0;

        @Override
        public String toString() {
            return "(" + Arrays.toString(items) + ", " + pairSupport + ")";
        }

        @Override
        public boolean equals(Object obj) {
            Pair t = null;
            if (obj instanceof Pair)
                t = (Pair) obj;

            if (t == null)
                return false;

            return Transaction.itemsetEquals(this.items, t.items);
        }

        public static boolean containsAll(final Item array[], final Item check[]) {
            return Transaction.containsAllItems(array, check);
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
    }

    public static void main(String[] args) {
        IOUtils.loadTransactions(IOUtils.INPUT_TRANSACTION_PATH, IOUtils.INPUT_PARAMETERS_PATH);

        initCounter();
        System.out.println("\nInitial : " + Arrays.toString(counts));

        removeLessSupport();
        System.out.println("Reduction : " + Arrays.toString(counts));

        while(counts.length > 1) { // implementation specific, need to change for MSApriori
            System.out.println();

            if(counts.length > 1) {
                computeCandidates();
                System.out.println("Candidates : " + Arrays.toString(counts));
            }

            removeLessSupport();
            System.out.println("Reduction : " + Arrays.toString(counts));
            System.out.println("Total no. of " + itemSetCount + "-frequent itemsets : " + counts.length);
        }

        System.out.println("Final itemset : " + Arrays.toString(counts));
    }

}
