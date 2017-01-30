import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @date 24-Jan-17
 * @author Somshubra Majumdar, Omid Memarrast
 *
 * Deals with IO related tasks. Reading of input, parsing, supplying formated data to
 * MSApriorori and writing results.
 */
public class IOUtils {

    // Relative path of default files
    public static final String INPUT_TRANSACTION_PATH = "data\\input-data.txt";
    public static final String INPUT_PARAMETERS_PATH = "data\\parameter-file.txt";

    // Array of all transactions
    public static Transaction[] transactions;

    // Map of Items (item ids) to their minimum supports
    public static LinkedHashMap<Integer, Double> minSupports;

    // Support difference constraint
    public static double supportDifferenceConstraint = 0.0;

    // Array of specifit transactions that cannot be together
    public static Transaction[] cannotBeTogether;

    // Array of item ids that must be present
    public static int mustContain[];

    // Number of transactions present in transaction set
    public static int transactionCount = 0;

    // Number of unique item IDs
    public static int itemIDCount = 0;

    public static Transaction[] loadTransactions(String transactionsPath, String parameterPath) {
        ArrayList<Transaction> transactionList = new ArrayList<>();

        try {
            loadParameters(parameterPath);
            System.out.println("Loaded parameter set");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader bb = new BufferedReader(new FileReader(transactionsPath));

            String line = "";
            String parts[] = null;
            int intParts[] = null;

            while((line = bb.readLine()) != null) {
                line = line.substring(1, line.length() - 1); // removed '{' and '}' chars]
                line = line.replace(",", "").trim();
                parts = line.split(" ");

                intParts = new int[parts.length];
                for(int i = 0; i < intParts.length; i++) {
                    intParts[i] = Integer.parseInt(parts[i]);
                }

                Item items[] = new Item[intParts.length];
                for(int i = 0; i < intParts.length; i++) {
                    items[i] = new Item(intParts[i], minSupports.get(intParts[i]));
                }

                transactionList.add(new Transaction(items));
            }

            System.out.println("Loaded transaction set.");

        } catch (IOException e) {
            System.out.println("Could not load transaction set.");
            e.printStackTrace();
        }
        transactions = transactionList.toArray(new Transaction[0]);

        transactionCount = transactions.length;

        return transactions;
    }

    private static void loadParameters(String parameterPath) throws FileNotFoundException {
        minSupports = new LinkedHashMap<>();

        ArrayList<Transaction> cannotBeTogetherList = new ArrayList<>();
        ArrayList<Integer> mustContainList = new ArrayList<>();

        try {
            BufferedReader bb = new BufferedReader(new FileReader(parameterPath));
            String line = "";

            while((line = bb.readLine()) != null) {
                if (line.contains("MIS")) {
                    addMinimumItemSupport(line);
                }
                else if (line.contains("SDC")) {
                    setSupportDifferenceConstraint(line);
                }
                else if (line.contains("cannot_be_together")) {
                    addCannotBeTogether(line, cannotBeTogetherList);
                }
                else if (line.contains("must-have")) {
                    addMustHave(line, mustContainList);
                }
            }

            cannotBeTogether = cannotBeTogetherList.toArray(new Transaction[0]);

             /* Use stream API to convert list to Stream<Integer>
                Then convert Stream<Integer> to IntStream (via mapToInt)
                Here, i -> i means : Lambda parameter Integer i -> auto unbox to primitive int i
                Collect array via toArray() */
            mustContain = mustContainList.stream().mapToInt(i -> i).toArray();

        } catch (IOException e) {
            System.out.println("Could not load parameters.");
            e.printStackTrace();
        }
    }

    private static void addMinimumItemSupport(String line) {
        line = line.substring(4); // removes MIS( from line
        String parts[] = line.split("=");

        // Item ID cleanup
        int itemID = Integer.parseInt(parts[0].replace(")", "").replace(" ", ""));

        // MIS of Item
        double mis = Double.parseDouble(parts[1].replace(" ", ""));

        minSupports.put(itemID, mis);

        itemIDCount++;
    }

    private static void setSupportDifferenceConstraint(String line) {
        line = line.substring(4);
        line = line.replace("=", "").replace(" ", "");

        supportDifferenceConstraint = Double.parseDouble(line);
    }

    private static void addCannotBeTogether(String line, ArrayList<Transaction> cannotBeTogetherList) {
        line = line.replace("cannot_be_together:", "");
        line = line.replace(" ", "");

        Pattern pattern = Pattern.compile("\\{([^}]+)+\\}");
        Matcher matcher = pattern.matcher(line);

        String partIDs[] = null;
        int intPartIDs[] = null;

        while (matcher.find()) {
            int matchCount = matcher.groupCount();

            for (int matchIndex = 1; matchIndex <= matchCount; matchIndex++) {
                String part = matcher.group(matchIndex);
                partIDs = part.split(",");
                intPartIDs = new int[partIDs.length];

                for(int i = 0; i < partIDs.length; i++) {
                    intPartIDs[i] = Integer.parseInt(partIDs[i]);
                }

                Item items[] = new Item[intPartIDs.length];
                for(int i = 0; i < intPartIDs.length; i++) {
                    items[i] = new Item(intPartIDs[i], minSupports.get(intPartIDs[i]));
                }

                cannotBeTogetherList.add(new Transaction(items));
            }
        }
    }


    private static void addMustHave(String line, ArrayList<Integer> mustHaveList) {
        line = line.replace("must-have: ", "").trim();
        String parts[] = line.split("or");

        for (String part : parts) {
            part = part.trim();
            mustHaveList.add(Integer.parseInt(part));
        }
    }


//    public static void main(String[] args) {
//        // Test functionality of IOUtils model
//        Transaction[] transactions = IOUtils.loadTransactions(IOUtils.INPUT_TRANSACTION_PATH, IOUtils.INPUT_PARAMETERS_PATH);
//
//        System.out.println("\nTransactions : ");
//        for (Transaction transaction : transactions) {
//            System.out.println(transaction);
//        }
//
//        System.out.println("\nSupport Difference Constraint : " + supportDifferenceConstraint);
//
//        System.out.println("\nCannot Be Together : ");
//
//        for (Transaction t : cannotBeTogether) {
//            System.out.println(t);
//        }
//
//        System.out.println("\nMust Contain : " + Arrays.toString(mustContain));
//    }


}
