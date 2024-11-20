package dadkvs.server.domain.paxos;

import static dadkvs.server.domain.utils.UtilsFunctions.waitt;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;

public class ConsensusMonitor {

  private TreeSet<Integer> pendingValuesList;       // Request Id
  private TreeSet<Integer> consensusFinishedList;   // Index
  private Map<Integer, Paxos> paxos_instances;      // Index - Paxos Instance

  public ConsensusMonitor() {
    this.pendingValuesList = new TreeSet<>();
    this.consensusFinishedList = new TreeSet<>();
    this.paxos_instances = new HashMap<>();
  }

  // METHODS TO HANDLE CONSENSUS ROUNDS

  public LiderPaxos startPaxosRound(int server_id) {
    int newIndex;

    synchronized (consensusFinishedList) {
      newIndex = consensusFinishedList.size();
    }

    return new LiderPaxos(newIndex, server_id);
  }

  public synchronized void finishPaxosRound(int index, int value) {
    // We need to remove the value from the pending list before adding it to the consensus finished list
    // Otherwise, a new paxos instance could be started with the same value (duplicated transaction)

    synchronized (pendingValuesList) {
      if (!pendingValuesList.contains(value)) return;
      pendingValuesList.remove(value);
    }

    synchronized (consensusFinishedList) {
      if (consensusFinishedList.contains(index)) return;
      consensusFinishedList.add(index);
    }

    System.out.println("[REPLICA] Assign sequence number " + index + " to request " + value + "\n[REPLICA] Pending txs: " + pendingValuesList + "\n");
  }

  // METHODS TO HANDLE PENDING TRANSACTIONS

  public int selectTransaction() {
    synchronized(pendingValuesList) {
      return pendingValuesList.first();
    }
  }

  public void addPendingValue(int reqid) {
    synchronized (pendingValuesList) {
      pendingValuesList.add(reqid);
      pendingValuesList.notifyAll();        // Awake all threads stopped waitForPendingValues
    }

    System.out.println("[REPLICA] ADD PENDING TRANSACTION: " + reqid + "\n[REPLICA] Pending txs: " + pendingValuesList + "\n");
  }

  public void waitForPendingValues() {
    System.out.println("    [LEADER] Waiting for pending txs to arrive...");

    synchronized (pendingValuesList) {
      while (pendingValuesList.isEmpty()) {
        waitt(pendingValuesList);             // Awake when a new transaction arrives (addPendingValue)
      }
    }
  }

  // OTHER METHODS

  public Paxos getPaxosInstanceOrCreate(int index) {
    synchronized (paxos_instances) {
      Paxos paxos = paxos_instances.get(index);

      if (paxos != null) return paxos;

      paxos = new Paxos(index);
      paxos_instances.put(index, paxos);

      return paxos;
    }
  }

}
