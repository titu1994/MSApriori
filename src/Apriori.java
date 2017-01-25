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
        counts = new Pair[5];
        checkCounts = new Pair[items.length];

        int x = 0, y = 0;
        for (int c = 1; c < 6; c++) {
            counts[x] = new Pair();
            counts[x].item = new int[]{c};
            counts[x++].support = 0;
        }

        int cindex = 0;
        x = 0;

        for (String item : items) {
            char[] data = item.toCharArray();
            checkCounts[x] = new Pair();
            checkCounts[x].item = new int[data.length];
            y = 0;

            for (char c : data) {
                cindex = ((int) c) - 49;
                counts[cindex].support++;
                checkCounts[x].item[y] = cindex + 1;
                y++;
            }
            x++;
        }

        initialPairs = new Pair[5];
        System.arraycopy(counts, 0, initialPairs, 0, counts.length);
    }

    public static void removeLessSupport() {
        ArrayList<Pair> pairs = new ArrayList<>();
        for (Pair p : counts)
            if (p.support >= minSupport)
                pairs.add(p);

        counts = pairs.toArray(new Pair[0]);
    }

    public static void computeCandidates() {
        ArrayList<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < counts.length; i++) {
            for (int j = i + 1; j < counts.length; j++) {
                int itemset[] = new int[itemSetCount + 1];
                int itemsI[] = counts[i].item;
                int itemsJ[] = counts[j].item;

                Arrays.sort(itemsI);
                Arrays.sort(itemsJ);
                System.arraycopy(itemsI, 0, itemset, 0, itemsI.length); // Copy old itemsI

                if(itemsJ.length > 1) {
                    int noOfPosToCheck = itemsJ.length - 1;
                    boolean test = true;

                    for(int k = 1; k <= noOfPosToCheck; k++) {
                        if(itemsI[itemsI.length - k] != itemsJ[k - 1]) {
                            test = false;
                            break;
                        }
                    }

                    if(test) {
                        itemset[itemset.length - 1] = itemsJ[itemsJ.length - 1];
                    }
                    else {
                        continue;
                    }
                }
                else {
                    itemset[itemset.length - 1] = counts[j].item[0];
                }

                int supportCount = 0;

                // Compare the new item set to see if old one contains these items
                for(Pair p : checkCounts) {
                    if(Pair.containsAll(p.item, itemset)) {
                        supportCount++;
                    }
                }

                Pair p = new Pair();
                p.support = supportCount;
                p.item = itemset;

                pairs.add(p);
            }
        }

        prevCounts = counts;
        counts = pairs.toArray(new Pair[0]);
        itemSetCount++;
    }

    public static void getRules() {
        ArrayList<Integer> list = new ArrayList<>();

        for(int x : counts[0].item)
            list.add(x);

        permute(list, 0);

        rules = new Rule[rulePermutations.size()];
        int rulePos[] = counts[0].item;

        for(int i = 0; i < rulePos.length; i++) {
            rules[i] = new Rule();
            rules[i].rules = new String[] {"" + rulePos[i] };
            rules[i].associations = new String[] {"" + rulePos[(i+1)%3], "" + rulePos[(i+2)%3]};
        }

        for(int i = 0; i < rulePos.length; i++) {
            rules[i+3] = new Rule();

            for(int j = i + 1; j <= rulePos.length; j++) {
                rules[i+3].rules = new String[] {"" + rulePos[(i+1)%3], "" + rulePos[(i+2)%3]};
                rules[i+3].associations = new String[] {"" + rulePos[(i)%3]};
            }
        }
    }

    public static void computeSupportRule() {
        int ruleSupport = counts[0].support;

        int ruleMap[][] = new int[rulePermutations.size()][];
        int associationMap[][] = new int[rulePermutations.size()][];

        for(int i = 0; i < rules.length; i++) {
            ruleMap[i] = Arrays.stream(rules[i].rules).mapToInt(Integer::parseInt).toArray();
            associationMap[i] = Arrays.stream(rules[i].associations).mapToInt(Integer::parseInt).toArray();
        }

        for(int ruleIndex = 0; ruleIndex < rulePermutations.size(); ruleIndex++) {
            if(ruleMap[ruleIndex].length == 1) {
                int support = initialPairs[ruleMap[ruleIndex][0] - 1].support;
                rules[ruleIndex].confidence = ruleSupport / (double) support;            }
            else {
                for (Pair prevCount : prevCounts) {
                    if (prevCount.item[0] == ruleMap[ruleIndex][1] &&
                            prevCount.item[1] == ruleMap[ruleIndex][0]) {
                        int support = prevCount.support;
                        rules[ruleIndex].confidence = ruleSupport / (double) support;
                        break;
                    }
                    else if(prevCount.item[0] == ruleMap[ruleIndex][0] &&
                            prevCount.item[1] == ruleMap[ruleIndex][1]) {
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


        while(counts.length > 1) {
            removeLessSupport();
            System.out.println("Reduction : " + Arrays.toString(counts));

            if(counts.length > 1) {
                computeCandidates();
                System.out.println("Candidates : " + Arrays.toString(counts));
            }
        }

        System.out.println("Final itemset : " + Arrays.toString(counts));

        getRules();
        computeSupportRule();

        System.out.print("\nEnter minimum confidence : ");
        double minConfidence = sc.nextDouble();

        removeMinimumConfidenceRules(minConfidence);

    }

}
