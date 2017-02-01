import java.util.*;

/**
 * Created by Yue on 05-Apr-16.
 *
 * Implementation of standard Apriori algorithm.
 */
public class Apriori {

    private static String items[] = {"134", "235", "1235", "25"};
    private static Pair counts[];
    private static Pair initialPairs[];
    private static Pair prevCounts[];
    private static Pair checkCounts[];
    private static Rule rules[];

    private static int minSupport = 0;
    private static int itemSetCount = 1;
    private static ArrayList<Integer[]> rulePermutations;

    public static void initCounter() {
        rulePermutations = new ArrayList<>();
        counts = new Pair[5]; // Only 4 data points, useful for indexing of last object
        checkCounts = new Pair[items.length];

        int x = 0, y = 0;
        for (int c = 1; c < 6; c++) {
            counts[x] = new Pair();
            counts[x].item = new int[]{c}; // initialize counts from 1 to 5. Acts as counts of 1st iteration of candidates
            counts[x++].support = 0; // initialize support of counts to 0
        }

        int cindex = 0;
        x = 0;

        for (String item : items) {
            char[] data = item.toCharArray();
            checkCounts[x] = new Pair();
            checkCounts[x].item = new int[data.length];
            y = 0;

            for (char c : data) { // for each char in each string input
                cindex = ((int) c) - 49; // 48 is 0, 49 is 1, ...
                counts[cindex].support++; // increase support val of 1, 2, 3, ... input data points
                checkCounts[x].item[y] = cindex + 1; // initialize to the value of input (1, 3, 4), (2, 3, 5) and so on
                y++; // Next char
            }
            x++; // Next string
        }

        initialPairs = new Pair[5]; // Actual first candidates
        System.arraycopy(counts, 0, initialPairs, 0, counts.length); // same as counts so copy
    }

    public static void removeLessSupport() {
        ArrayList<Pair> pairs = new ArrayList<>();
        for (Pair p : counts)
            if (p.support >= minSupport)
                pairs.add(p); // select only those pairs which have value above min support

        counts = pairs.toArray(new Pair[0]); // replace counts with reduced set
    }

    public static void computeCandidates() {
        ArrayList<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < counts.length; i++) { // search each and every pair of counts
            for (int j = i + 1; j < counts.length; j++) { // with all other pairs
                int itemset[] = new int[itemSetCount + 1]; // Increase size of itemset by 1
                int itemsI[] = counts[i].item; // Convenience to select Current itemset
                int itemsJ[] = counts[j].item; // Convenience to select all other itemset iteratively

                Arrays.sort(itemsI); // Sort lexicographically (in this case integer wise)
                Arrays.sort(itemsJ); // Sort lexicographically (in this case integer wise)
                System.arraycopy(itemsI, 0, itemset, 0, itemsI.length); // Copy old itemsI, only increase by 1

                if(itemsJ.length > 1) {
                    int noOfPosToCheck = itemsJ.length - 1;
                    boolean test = true; // test to see if

                    for(int k = 0; k < noOfPosToCheck; k++) {
                        if(itemsI[k] != itemsJ[k]) { // break if intermediate values are different between I and J
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
                    itemset[itemset.length - 1] = counts[j].item[0]; // select next item after this one to be merged
                }

                int supportCount = 0;

                // Compare the new item set to see if old one contains these items, computing support count for k-itemset
                for(Pair p : checkCounts) {
                    if(Pair.containsAll(p.item, itemset)) {
                        supportCount++;
                    }
                }

                Pair p = new Pair();
                p.support = supportCount;
                p.item = itemset;

                pairs.add(p); // Construct the new merged k-itemset
            }
        }

        prevCounts = counts; // preserve last counts for later use
        counts = pairs.toArray(new Pair[0]); // counts now has all of the new candidates
        itemSetCount++; // marks the k in k-itemset
    }

    public static void getRules() {
        ArrayList<Integer> list = new ArrayList<>();

        for(int x : counts[0].item) // Assuming only 1 final candidate. Must be generalized
            list.add(x);

        permute(list, 0);

        rules = new Rule[rulePermutations.size()]; // Calculated size = 6. Next few calculations depend on this being known.
        int rulePos[] = counts[0].item; // [2, 3, 5] in this example

        for(int i = 0; i < rulePos.length; i++) {
            rules[i] = new Rule();
            rules[i].rules = new String[] {"" + rulePos[i] };
            rules[i].associations = new String[] {"" + rulePos[(i+1)%3], "" + rulePos[(i+2)%3]}; // For 3 itemset rule only, must generalize
            // The associations are basically : {1, 2}, {2, 3}, {3, 1}
        }

        for(int i = 0; i < rulePos.length; i++) {
            rules[i+3] = new Rule();

            for(int j = i + 1; j <= rulePos.length; j++) {
                rules[i+3].rules = new String[] {"" + rulePos[(i+1)%3], "" + rulePos[(i+2)%3]};
                rules[i+3].associations = new String[] {"" + rulePos[(i)%3]}; // Make 2 LHS rules
                // These associations are basically : {3 ^ 5 => 2, 5 ^ 2 => 3, 2 ^ 3 = 5. Need to generalize
            }
        }
    }

    public static void computeSupportRule() {
        // This is not according to prof. Liu's algo. He says we already have this, therefore no need to compute again.
        // I don't know how we already have these values. Therefore, I had to calculate again.

        int ruleSupport = counts[0].support; // Support of itemset (2, 3, 5)

        int ruleMap[][] = new int[rulePermutations.size()][]; // dangling array
        int associationMap[][] = new int[rulePermutations.size()][]; // dangling array

        for(int i = 0; i < rules.length; i++) {
            // Translate strings to ints via Streams API
            ruleMap[i] = Arrays.stream(rules[i].rules).mapToInt(Integer::parseInt).toArray();
            associationMap[i] = Arrays.stream(rules[i].associations).mapToInt(Integer::parseInt).toArray();
        }

        for(int ruleIndex = 0; ruleIndex < rulePermutations.size(); ruleIndex++) { // known to be 6, need to make dynamic since I made assumptions for 6
            if(ruleMap[ruleIndex].length == 1) { // 1-itemset support values
                int support = initialPairs[ruleMap[ruleIndex][0] - 1].support; // This is why we preserved the initial 1-itemset candidates
                rules[ruleIndex].confidence = ruleSupport / (double) support;
            }
            else {
                for (Pair prevCount : prevCounts) { // Preserved 2-itemset values, needs to be generalized in a array of arrays
                    if (prevCount.item[0] == ruleMap[ruleIndex][1] && // Check against [(1, 3), (2, 3), (2, 5), (3, 5)]
                            prevCount.item[1] == ruleMap[ruleIndex][0]) { // if matching sub-pairs exist. Needs to generalize
                        int support = prevCount.support;
                        rules[ruleIndex].confidence = ruleSupport / (double) support;
                        break;
                    }
                    else if(prevCount.item[0] == ruleMap[ruleIndex][0] &&
                            prevCount.item[1] == ruleMap[ruleIndex][1]) { // same as above
                        int support = prevCount.support;
                        rules[ruleIndex].confidence = ruleSupport / (double) support;
                        break;
                    }

                }
            }
        }

        System.out.println("\nItemset Confidence : \n" + Arrays.toString(rules));
    }

    public static void permute(List<Integer> arr, int k){
        // Recursively generate combinations of rules
        for(int i = k; i < arr.size(); i++){
            Collections.swap(arr, i, k);
            permute(arr, k+1);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1){
            rulePermutations.add(arr.toArray(new Integer[0]));
        }
    }

    public static void removeMinimumConfidenceRules(double minConfidence) {
        ArrayList<Rule> ruleList = new ArrayList<>();

        for(Rule r : rules) {
            if(r.confidence >= minConfidence)
                ruleList.add(r);
        }

        rules = ruleList.toArray(new Rule[0]);
        System.out.println("Min Confidence Rule List : " + Arrays.toString(rules));
    }

    private static class Pair {

        public int item[];
        int support;

        @Override
        public String toString() {
            return "{" + Arrays.toString(item) + ", " + support + "}";
        }

        @Override
        public boolean equals(Object obj) {
            Pair t = null;
            if (obj instanceof Pair)
                t = (Pair) obj;

            if (t == null)
                return false;
            return Arrays.deepEquals(new Object[]{this}, new Object[]{t});
        }

        public static boolean contains2(final int array[], final int v) {
            for (final int e : array)
                if (e == v)
                    return true;
            return false;
        }

        public static boolean containsAll(final int array[], final int check[]) {
            boolean tests[] = new boolean[check.length];
            boolean finalTest = true;
            int x = 0;

            for(int checkVal : check) {
                //System.out.println(Arrays.toString(array) + " --- " + "check = " + checkVal);
                tests[x++] = contains2(array, checkVal);
            }

            for(boolean test : tests) {
                finalTest = finalTest && test;
            }
            return finalTest;
        }
    }

    private static class Rule {

        String[] rules;
        String[] associations;
        double confidence;

        @Override
        public String toString() {
            String rule = String.join("^", rules);
            String association = String.join("^", associations);
            return rule + " => " + association + "; Confidence = " + confidence;
        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter min support value : ");
        minSupport = sc.nextInt();

        initCounter();
        System.out.println("Initial : " + Arrays.toString(counts));

        int K = 2;
        while(counts.length > 1 && K < 10) { // implementation specific, need to change for MSApriori
            removeLessSupport();
            System.out.println("Reduction : " + Arrays.toString(counts));

            if(counts.length > 1) {
                computeCandidates();
                System.out.println("Candidates : " + Arrays.toString(counts));
            }

            K++; // hardline limit to 10 iterations to avoid combinatorial explosion
        }

        System.out.println("Final itemset : " + Arrays.toString(counts));

        getRules();
        computeSupportRule();

        System.out.print("\nEnter minimum confidence : ");
        double minConfidence = sc.nextDouble();

        removeMinimumConfidenceRules(minConfidence);

    }

}
