package dadkvs.server.domain.utils;

import static dadkvs.server.domain.utils.UtilsFunctions.waitt;

import java.util.Map;
import java.util.HashMap;

public class TransactionMonitor {

    private class currentTransactionSeqNumber {
        private int value;

        public currentTransactionSeqNumber() {
            this.value = 0;
        }

        public void increment() {
            synchronized (this) {
                this.value++;
                this.notifyAll();
            }
        }

        public void waitUntil(int value) {
            synchronized (this) {
                while (this.value < value) {
                    // System.out.println("[REPLICA] Waiting for tx: " + this.value + " to finish, target: " + value);
                    waitt(this);
                }
            }
        }
    }

    private currentTransactionSeqNumber currentTransactionSeqNumber;
    private final Map<Integer, Integer> assignedTransactions;   // Request Id - Transaction Sequence Number

    public TransactionMonitor() {
        assignedTransactions = new HashMap<>();
        currentTransactionSeqNumber = new currentTransactionSeqNumber();
    }

    // Transactions Order

    public void incrementCurrentTransactionSeqNumber() {
        this.currentTransactionSeqNumber.increment();
    }

    public void waitForPriorTransactionsToFinish(int reqid) {
        int transactionIndex;

        synchronized (assignedTransactions) {
            transactionIndex = assignedTransactions.get(reqid);
        }

        currentTransactionSeqNumber.waitUntil(transactionIndex);
    }

    // Assigned transactions

    public void assignTransaction(int requestId, int transactionSequenceNumber) {
        synchronized (assignedTransactions) {
            assignedTransactions.put(requestId, transactionSequenceNumber);
            assignedTransactions.notifyAll();       // awake all thereads stopped in waitForTransactionToBeAssigned
        }
    }

    public void waitForTransactionToBeAssigned(int requestId) {
        synchronized (assignedTransactions) {
            while (!assignedTransactions.containsKey(requestId)) {
                // System.out.println("[REPLICA] Waiting for tx: " + requestId + " to have seqNum assigned");
                waitt(assignedTransactions);
            }
        }
    }
}
