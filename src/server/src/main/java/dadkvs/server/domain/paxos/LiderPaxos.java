package dadkvs.server.domain.paxos;

import static dadkvs.server.Constants.N_ACCEPTORS;

public class LiderPaxos {

  private final int index;
  private int timestamp;

  public LiderPaxos(int index, int server_id) {
    this.index = index;
    this.timestamp = server_id - N_ACCEPTORS;
  }

  public int getIndex() { return this.index; }
  public int getTimestamp() { return this.timestamp; }

  public int nextTimestamp() {
    this.timestamp += N_ACCEPTORS;
    return this.timestamp;
  }
  
}
