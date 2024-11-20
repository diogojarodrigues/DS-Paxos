package dadkvs.server.domain.classes;

public class LearnPhaseReply {

  private int learnconfig;
  private int learnindex;
  private boolean learnaccepted;

  public LearnPhaseReply(int learnconfig, int learnindex, boolean learnaccepted) {
    this.learnconfig = learnconfig;
    this.learnindex = learnindex;
    this.learnaccepted = learnaccepted;
  }

  public int getLearnconfig() { return learnconfig; }
  public int getLearnindex() { return learnindex; }
  public boolean getLearnaccepted() { return learnaccepted; }

}
