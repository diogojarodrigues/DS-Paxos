package dadkvs.server.domain.paxos;

import static dadkvs.server.Constants.MAJORITY;

import java.util.AbstractMap.SimpleEntry;

public class Paxos {

  private final int index;            // Sequence number
  private int value = -1;             // Request id
  private int read_timestamp = -1;
  private int write_timestamp = -1;
  private SimpleEntry<Integer, Integer> responses = new SimpleEntry<>(-1, -1); // Value - Count

  public Paxos(int index) {
    this.index = index;
  }

  public int getIndex() { return index; }
  public int getValue(boolean accpeted) { return accpeted ? value : -1; }
  public int getWriteTimestamp(boolean accpeted) { return accpeted ? this.write_timestamp : -1; }

  public boolean hasEnoughResponses(int timestamp) {
    synchronized (responses) { // Synchronize on the responses object itself
        if (timestamp > responses.getKey()) {
            responses = new SimpleEntry<>(timestamp, 1);
        } else if (timestamp == responses.getKey()) {
            responses.setValue(responses.getValue() + 1); // Update the value directly
        }

        // System.out.println("[REPLICA] Learn paxos round " + index + " with timestamp " + timestamp + " (" + responses.getValue() + "/" + MAJORITY + ")");
        return responses.getValue() == MAJORITY;
    }
  }

  public synchronized boolean hasNotPromisedToHigherLider(int timestamp) {
    boolean accept = timestamp >= this.read_timestamp;

    if (accept) this.read_timestamp = timestamp;

    return accept;
  }

  public synchronized boolean hasNotPromisedToHigherLider(int timestamp, int value) {
    boolean accept = timestamp >= this.read_timestamp;

    if (accept) {
      this.read_timestamp = timestamp;
      this.write_timestamp = timestamp;
      this.value = value;
    }

    return accept;
  }
}
