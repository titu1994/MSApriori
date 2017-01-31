# Multiple Minimum Support Apriori Algorithm for Association Rule Mining

Multiple support apriori algorithm in Java, which computes just the first step of association rule mining. 

Additional Parameters:

- Must Have : Item IDs which must be present in any given itemset
- Cannot Be Together : Item IDs which cannot be together in the same itemset
- Support Difference Constraint : Difference between max(MIS(i)) and min(MIS(i)) cannot be larger than a constant 'phi'
