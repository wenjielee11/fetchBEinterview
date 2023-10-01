package com.fetchinterview.fetchinterview.data;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetchinterview.fetchinterview.model.Transaction;

/**
 * This class implements the methods to handle transactions that will be called by the REST controller.
 * Uses in-memory data structures to store and keep track of transactions.
 * 
 * Note: There is a potential bug from the test data given. After adding the transactions,
 * Where one of the transaction is "Dannon, points: -200", the expected result of
 * /balance is "Miller Coors: 5300". This implies that -200 is "spent" when instead, it should not be spent.
 * Negative points should not be spent. As conventionally, they should be deducting the total points. 
 * 
 * Here is the math when spending the points: 
 * 
 * *Assuming negative transaction points CANT be spent: 
 *   5000 - 300 [Dannon] - 200 [UNILEVER] - (ignore negative points) = 4500 points left to buy
 *   Remaining Miller Coors: 10 000 - 4500 =  5500 (actual) =/= 5300 (expected)
 * 
 * * Assuming negative transaction points CAN be spent: 
 *  5000 - 300 [Dannon] - 200[UNILEVER] - (-200) [Dannon with negative points] = 4700
 * Remaining Miller Coors: 10 000 - 4700 = 5300 (expected)
 * 
 * This bug does not impact the total Dannon points because it comes after Miller Coors, and cannot be spent
 * until Miller Coor's points are finished. However, it impacts the arithmetic relationship between 
 * (the total points a user should have) and (the total points the user is spending). To see the bug in action,
 * read spendPoints(). 
 * 
 * This class currently uses the implementation where negative points may be spent to return the expected result stated by the
 * challenge prompt.
 * 
 */
@Service
public class TransactionRepository{
    // Total global points. 
    public long totalPoints;
    // To store the transactions. First node of the linked list contains a dummy transaction node
    // That stores the total accumulated points of its respective payer.
    HashMap<String, LinkedList<Transaction>> transactionMap;
    // To order the transactions based on timestamps.
    PriorityQueue <Transaction> transactionQueue;
    static Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimeStamp);

    public TransactionRepository(){
        transactionMap = new HashMap<String, LinkedList<Transaction>>();
        transactionQueue = new PriorityQueue<>(transactionComparator);
        totalPoints= 0;
    }

    /**
     * This method adds the transaction records into the hashmap and stores them into the prioriry queue
     * for usage in spendPoints. Uses linked lists to store transactions more efficiently. 
     * 
     * @param transactionName name of Payer
     * @param trans transaction object to be appended to the linked list
     * @return a http response indicating if the service can fulfill the request.
     */
    public ResponseEntity<?> addTransaction(String transactionName, Transaction trans){
        if(transactionMap.putIfAbsent(transactionName, new LinkedList<Transaction>()) == null){
            // Creating a dummy node that stores the total points accumulated for this particular payer
            transactionMap.get(transactionName).add(new Transaction("", 0, "2020-11-02T14:00:00Z"));
        };
        // Keeping track of new transactions 
        LinkedList <Transaction> tempList = transactionMap.get(transactionName);
        long tempPoints = tempList.getFirst().points;
        if(tempPoints + trans.points < 0){ // A payer's points can never go below 0
            return ResponseEntity.badRequest().build();
        }
        tempList.getFirst().points += trans.points; // store new points into total points of payer.
        totalPoints += trans.points;
        transactionQueue.add(new Transaction(trans.payer, trans.points, trans.timeStamp));
        return ResponseEntity.ok().build();
    }

    /**
     * This method spends the points accumulated ordered from the oldest time stamp to the newest. 
     * @param points the amount of points a user is spending. 
     * @return a HTTP code indicating if the spending was valid or not.  
     */
    public ResponseEntity<String> spendPoints(long points){
        HashMap <String, Long> result = new HashMap<>();

        // 0 points spent.
        if(points == 0){
            return ResponseEntity.ok("[{\"payer\" : \"NONE\", \"points\" : \"0\""); 
        }
        if(points > totalPoints || points < 0){
            return ResponseEntity.badRequest().body("The user doesn't have enough points.");
        }
        while(points > 0){
            // Gets the oldest payer and points that was stored in the queue
            Transaction oldestTransaction = transactionQueue.peek();
            Transaction payerTotalPoints = transactionMap.get(oldestTransaction.payer).getFirst(); // Gets the node that stores the total points for this payer
                
            long balance = points - oldestTransaction.points;
            if(balance >=0){
                // This Payer's points are just or not enough to cover the spending
                payerTotalPoints.points -= oldestTransaction.points;
                totalPoints -= oldestTransaction.points;
                 // Add it to the collection of payer and points spent. 
                result.put(oldestTransaction.payer, result.getOrDefault(oldestTransaction.payer, 0L) - oldestTransaction.points);
                // Set the new balance.
                points = balance;
                // Move on to the next payer.
                transactionQueue.poll();
            }else{
                // The oldest transaction points are enough to cover the spending. Substract the payer total points with the current spending points.
                // Substract the oldest transaction points with the spending points. do not remove it from the queue as there are still points left.
                oldestTransaction.points -= points;
                payerTotalPoints.points -= points;
                totalPoints -= points;
                 // Add it to the collection of payer and points spent. 
                result.put(oldestTransaction.payer, result.getOrDefault(oldestTransaction.payer, 0L) - points);
                points = 0;
            } 
        }
       
        
        // Creating response body fitting the sample response given in the prompt
        LinkedList<Result> resList = new LinkedList<>();
        Iterator<Entry<String, Long>> it = result.entrySet().iterator();
        while(it.hasNext()){
            Entry<String, Long> mapEntry = it.next();
            resList.add(new Result(mapEntry.getKey(), mapEntry.getValue()));
        }
       
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resList);
            return ResponseEntity.ok(jsonResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    /**
     * This method returns the current point balance of all Payers. 
     * @return a HTTP code signaling the good request and a body of the payers and points.
     */
    public ResponseEntity<String> getBalance(){
        Iterator<Entry<String, LinkedList<Transaction>>> it = transactionMap.entrySet().iterator();
        HashMap<String, Long> result = new HashMap<>(transactionMap.size());

        while(it.hasNext()){
            Entry<String, LinkedList<Transaction>> mapEntry = it.next();
            result.put(mapEntry.getKey(), mapEntry.getValue().getFirst().points);
        }
         ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            return ResponseEntity.ok(jsonResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
    /**
     * Class for Jackson to serialize data. Since it uses reflection, it needs public getter methods. 
     */
    class Result{
        String payer;
        long points;
        
        Result(String payer, long points){
            this.points = points;
            this.payer = payer;
        }
        public String getPayer(){
            return payer;
        }
        public long getPoints(){
            return points;
        } 
        
    }

}






