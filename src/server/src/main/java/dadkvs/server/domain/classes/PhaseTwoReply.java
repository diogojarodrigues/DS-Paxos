package dadkvs.server.domain.classes;

public class PhaseTwoReply {

  private int phase2config;
  private int phase2index;
  private boolean phase2accepted;

  public PhaseTwoReply(int phase2config, int phase2index, boolean phase2accepted) {
    this.phase2config = phase2config;
    this.phase2index = phase2index;
    this.phase2accepted = phase2accepted;
  }

  public int getPhase2config() { return phase2config; }
  public int getPhase2index() { return phase2index; }
  public boolean getPhase2accepted() { return phase2accepted; }

}
