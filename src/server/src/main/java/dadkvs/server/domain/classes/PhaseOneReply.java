package dadkvs.server.domain.classes;

public class PhaseOneReply implements Comparable<PhaseOneReply> {

  private int phase1config;
  private int phase1index;
  private int phase1value;
  private int phase1timestamp;
  private boolean phase1accepted;

  public PhaseOneReply(int phase1config, int phase1index, int phase1value, int phase1timestamp, boolean phase1accepted) {
    this.phase1config = phase1config;
    this.phase1index = phase1index;
    this.phase1value = phase1value;
    this.phase1timestamp = phase1timestamp;
    this.phase1accepted = phase1accepted;
  }

  public int getPhase1config() { return phase1config; }
  public int getPhase1index() { return phase1index; }
  public int getPhase1value() { return phase1value; }
  public int getPhase1timestamp() { return phase1timestamp; }
  public boolean getPhase1accepted() { return phase1accepted; }

  @Override
  public int compareTo(PhaseOneReply other) {
      return Integer.compare(this.phase1timestamp, other.phase1timestamp);
  }
  
}
