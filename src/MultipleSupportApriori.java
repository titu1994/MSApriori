import java.util.*;

/**
 * @author Omid, Somshubra Majumdar
 *         <p>
 *         Implementation of Multiple Support Apriori algorithm.
 * @date 24-Jan-17
 */
public class MultipleSupportApriori {

    private static Candidate counts[];
    private static Candidate globalItemCounts[];

    private static ArrayList<Candidate> kItemsetList;
    private static ArrayList<Integer[]> permutations;

    private static ArrayList<Integer> reducedIndicesSet;
    private static ArrayList<Integer[]> validSets;

    private static int itemSetCount = 1;

    public static void initCounter() {
        counts = new Candidate[IOUtils.itemIDCount];
        globalItemCounts = new Candidate[IOUtils.transactionCount];

        kItemsetList = new ArrayList<>();
        permutations = new ArrayList<>();
        reducedIndicesSet = new ArrayList<>();
        validSets = new ArrayList<>();

        // Temporary map which provides mapping from Item ID to 'counts' array index
        LinkedHashMap<Integer, Integer> countIndexMap = new LinkedHashMap<>();

        int x = 0, y = 0;
        for (Map.Entry<Integer, Double> entry : IOUtils.minSupports.entrySet()) {
            counts[x] = new Candidate();
            counts[x++].items = new Item[]{new Item(entry.getKey(), entry.getValue())};

            countIndexMap.put(entry.getKey(), x - 1); // Preserve item id mapping to index of counts array
        }

        int cindex = 0;
        x = 0;

        for (Transaction t : IOUtils.transactions) {
            globalItemCounts[x] = new Candidate();
            globalItemCounts[x].items = new Item[t.items.length];
            y = 0;

            for (Item item : t.items) { // for each char in each string input
                cindex = countIndexMap.get(item.itemID); // returns the index in counts array for a specific item id
                counts[cindex].candidateSupport++; // increase support val of 1, 2, 3, ... input data points
                globalItemCounts[x].items[y] = item.clone(); // initialize to the value of input (1, 3, 4), (2, 3, 5) and so on
                y++; // Next item
            }
            x++; // Next transaction
        }

        // Calculate initial minimum support for each item id
        for (Candidate count : counts) {
            count.frequency = (int) count.candidateSupport;
            count.candidateSupport /= IOUtils.transactionCount;
        }
    }

    public static void computeCandidates() {
        ArrayList<Candidate> candidates = new ArrayList<>();

        for (int i = 0; i < counts.length; i++) { // search each and every pair of counts
            for (int j = i + 1; j < counts.length; j++) { // with all other candidates
                Item itemset[] = new Item[itemSetCount + 1]; // Increase size of itemset by 1
                Item itemsI[] = counts[i].items; // Convenience to select Current itemset
                Item itemsJ[] = counts[j].items; // Convenience to select all other itemset iteratively

                System.arraycopy(itemsI, 0, itemset, 0, itemsI.length); // Copy old itemsI, only increase by 1

                if (itemsJ.length > 1) { // need to generalize
                    int noOfPosToCheck = itemsJ.length - 1;
                    boolean test = true; // test to see if

                    for (int k = 0; k < noOfPosToCheck; k++) {
                        if (!itemsI[k].equals(itemsJ[k])) { // break if intermediate values are different between I and J
                            test = false;
                            break;
                        }
                    }

                    if (test) {
                        itemset[itemset.length - 1] = itemsJ[itemsJ.length - 1]; // merge I with J (last element of J), remaining same
                    } else {
                        continue; // search next candidate if fit to merge
                    }
                } else {
                    // special case for 1st iteration, where Item J has only 1 item.
                    // generates : {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 3}, {2, 4}, {2, 5},
                    // {3, 4}, {3, 5}, {4, 5}
                    itemset[itemset.length - 1] = counts[j].items[0]; // select next item after this one to be merged
                }

                Arrays.sort(itemset); // sort by minsupport values

                // Compute tail count
                int tailCount = 0;
                itemsI = Arrays.copyOfRange(itemset, 1, itemset.length); // Select only {c} - {c[1]}

                for (Transaction t : IOUtils.transactions) {
                    if (Transaction.containsAllItems(t.items, itemsI)) {
                        tailCount++;
                    }
                }

                // Compute support count of pair
                int supportCount = 0;

                // Compare the new item set to see if old one contains these items, computing support count for k-itemset
                for (Candidate p : globalItemCounts) {
                    if (Candidate.containsAll(p.items, itemset)) {
                        supportCount++;
                    }
                }

                Candidate p = new Candidate();
                p.candidateSupport = supportCount;
                p.frequency = supportCount;
                p.items = itemset;
                p.tailcount = tailCount;

                candidates.add(p); // Append the k-itemset
            }
        }

        for (Candidate p : candidates) {
            p.candidateSupport /= IOUtils.transactionCount; // normalize the pair support value to [0, 1]
        }

        counts = candidates.toArray(new Candidate[0]); // counts now updates with all of the new candidates
        itemSetCount++; // marks the k in k-itemset
    }


    public static void removeLessSupport() {
        ArrayList<Candidate> candidates = new ArrayList<>();

        System.out.println("Before Reduction : " + counts.length);


        if (itemSetCount == 1) {
            double minimumSupport = 0.0;
            for (Candidate c : counts) { // Add all candidates who satisfy the min support criterion
                minimumSupport = c.items[0].minSupport; // sorted in min support order, 1st item guaranteed to be smallest minSupport

                if (c.candidateSupport > minimumSupport)
                    candidates.add(c); // select only those candidates which have value above min support
            }
        }
        else if (itemSetCount == 2) {
            double lSupport = 0.0, hSupport = 0.0;
            for (Candidate c : counts) { // Add all candidates who satisfy the min support criterion
                lSupport = c.items[0].minSupport; // sorted in min support order, 1st item guaranteed to be smallest minSupport
                hSupport = c.items[1].minSupport; // sorted in min support order, 2nd item guaranteed to be larger minSupport

                if (hSupport >= lSupport && (hSupport - lSupport <= IOUtils.supportDifferenceConstraint))
                    candidates.add(c); // select only those candidates which have value above min support
            }
        }
        else {
            double minimumSupport;
            int subsetSize = itemSetCount - 1;
            boolean candidateMISTest = false;
            Item candidateSubset[] = new Item[subsetSize];

            reducedIndicesSet.clear();

            for (int i = 0; i <= subsetSize; i++) {
                reducedIndicesSet.add(i); // [0 to subsetSize-1] will be used for permutations
            }

            for (Candidate c : counts) { // Add all candida
                minimumSupport = c.items[0].minSupport; // sorted in min support order, 1st item guaranteed to be smallest minSupport

                if (c.candidateSupport > minimumSupport){ // support > minSupporttes who satisfy the min support criterion and MS subset check
                    Item C1 = c.items[0];

                    // Generate {k-1}-subsets s of candidate c which satisfy fact that {C1} belongs to s
                    Item[] reducedSet = Arrays.copyOfRange(c.items, 1, c.items.length); // all items other than C1

                    permutations.clear();
                    validSets.clear();

                    permute(reducedIndicesSet, 0); // get all permutations

                    for (Integer[] permutation : permutations) {
                        if (permutation[0] == 0) {
                            validSets.add(permutation); // select only those permutations which have the 1st element indices in 1st position
                        }
                    }

                    candidateSubset[0] = C1; // add 1st element to candidate subset

                    for (int i = 0; i < validSets.size(); i++) {
                        for (int j = 1; j < subsetSize; j++) {
                            // valid set contains [0, 1, 2], [0, 2, 1] for 3rd iteration.
                            // Now valid.get(0) = [0, 1, 2]
                            // Then, (valid.get(0)[1] - 1) = 1 - 1 = 0th index of reducedSet
                            // 0th index of reduced set = 20 for 3rd iteration of algorithm
                            candidateSubset[j] = reducedSet[(validSets.get(i)[j] - 1)]; // candidate subset generated
                        }

                        candidateMISTest = candidateSubset[0].minSupport == candidateSubset[1].minSupport; // test as given in algorithm
                        boolean candidateSubsetExistWithInCandidateGen = false;

                        if (candidateMISTest) { // Here, we do not need to check if C[1] in test as we have constructed subset to have C1 at 1st element
                            for (int k = 0; k < counts.length; k++) {
                                if (Transaction.containsAllItems(counts[k].items, candidateSubset)) {
                                    candidateSubsetExistWithInCandidateGen = true;
                                    break;
                                }
                            }

                            if (candidateSubsetExistWithInCandidateGen) {
                                candidates.add(c);
                            }
                            else {
                                // Delete the candidate (by not inserting it into next step candidates
                            }
                            break;
                        }
                        else {
                            // Just add since it doesnt satisfy the check on line 9 pg 33 (Web Data Mining).
                            // Therefore no need to check anything to remove.
                            candidates.add(c);
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("After primary reduction : " + candidates.size());

        // Remove all candidates who do not satisfy the support difference constraint
        candidates = handleSupportDifferenceConstraint(candidates);
        System.out.println("After removing 'SDC' violations : " + candidates.size());

        // Remove all candidates which have items that cannot be together
        candidates = handleCannotBeTogether(candidates);
        System.out.println("After removing 'cannot be together' violations : " + candidates.size());

        // Remove all candidates which do not have at least one of the Must Have category
        kItemsetList = handleAtleastOneMustBePresent(candidates);
        System.out.println("After removing 'must be at least one' violations : " + kItemsetList.size());

        counts = candidates.toArray(new Candidate[0]); // replace counts with reduced set
    }

    public static ArrayList<Candidate> handleSupportDifferenceConstraint(ArrayList<Candidate> candidates) {
        ArrayList<Candidate> reducedCandidates = new ArrayList<>();

        for (Candidate p : candidates) {
            double max = 0.0, min = 1.0;

            for (Item item : p.items) {
                if (item.minSupport >= max)
                    max = item.minSupport;


                if (item.minSupport < min)
                    min = item.minSupport;
            }

            if ((max - min) <= IOUtils.supportDifferenceConstraint)
                reducedCandidates.add(p);
        }
        return reducedCandidates;
    }

    public static ArrayList<Candidate> handleCannotBeTogether(ArrayList<Candidate> candidates) {
        ArrayList<Candidate> reducedCandidate = new ArrayList<>();

        for (Candidate p : candidates) {
            boolean testIfPairHasNoneOfCannotBeTogetherItems = true;

            for (Transaction t : IOUtils.cannotBeTogether) {
                if (Transaction.containsAllItems(p.items, t.items)) {
                    testIfPairHasNoneOfCannotBeTogetherItems = false;
                    break;
                }
            }

            if (testIfPairHasNoneOfCannotBeTogetherItems)
                reducedCandidate.add(p);
        }

        return reducedCandidate;
    }


    public static ArrayList<Candidate> handleAtleastOneMustBePresent(ArrayList<Candidate> candidates) {
        ArrayList<Candidate> reducedCandidate = new ArrayList<>();

        for (Candidate p : candidates) {
            boolean hasAtLeastOneMatchingItem = false;

            for (int id : IOUtils.mustContain) {
                if (p.containsItem(id)) {
                    hasAtLeastOneMatchingItem = true;
                    break;
                }
            }

            if (hasAtLeastOneMatchingItem)
                reducedCandidate.add(p);
        }

        return reducedCandidate;
    }

    static void permute(ArrayList<Integer> arr, int k){
        for(int i = k; i < arr.size(); i++){
            Collections.swap(arr, i, k);
            permute(arr, k+1);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() -1){
            permutations.add(arr.toArray(new Integer[0]));
        }
    }


    private static class Candidate {

        public Item items[];
        public double candidateSupport = 0.0;
        public int frequency = 0;
        public int tailcount = 0;

        @Override
        public String toString() {
            String itemNames = Arrays.toString(items);
            itemNames = itemNames.replace("[", "{").replace("]", "}");

            String string = "\t" + frequency + " : " + itemNames;

            if (tailcount != 0)
                string = string + ", Tailcount = " + tailcount + "";

            return string;
        }

        @Override
        public boolean equals(Object obj) {
            Candidate t = null;
            if (obj instanceof Candidate)
                t = (Candidate) obj;

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
    }

    public static void main(String[] args) {
        IOUtils.loadTransactions(IOUtils.INPUT_TRANSACTION_PATH, IOUtils.INPUT_PARAMETERS_PATH);

        initCounter();
        System.out.println("\nInitial : " + Arrays.toString(counts));

        removeLessSupport();
        Candidate[] kitemset = kItemsetList.toArray(new Candidate[0]);
        System.out.println("Candidate Prior Reduction : " + Arrays.toString(counts));
        System.out.println("Reduction : " + Arrays.toString(kitemset));

        while (counts.length > 1) { // implementation specific, need to change for MSApriori
            System.out.println();

            if (counts.length > 1) {
                computeCandidates();
                if (counts.length >= 1)
                    System.out.println("Candidates : " + Arrays.toString(counts));
                else {
                    break;
                }
            }

            removeLessSupport();
            kitemset = kItemsetList.toArray(new Candidate[0]);

            System.out.println("Candidate Prior Reduction : " + Arrays.toString(counts));
            System.out.println("Reduction : " + Arrays.toString(kitemset));
            System.out.println("Total no. of " + itemSetCount + "-frequent itemsets : " + kitemset.length);
        }

        //System.out.println("Final itemset : " + Arrays.toString(counts));
    }

}

