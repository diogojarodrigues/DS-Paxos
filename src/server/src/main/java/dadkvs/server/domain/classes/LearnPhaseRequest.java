package dadkvs.server.domain.classes;

public class LearnPhaseRequest {

  private int learnconfig;
  private int learnindex;
  private int learnvalue;
  private int learntimestamp;

  public LearnPhaseRequest(int learnconfig, int learnindex, int learnvalue, int learntimestamp) {
    this.learnconfig = learnconfig;
    this.learnindex = learnindex;
    this.learnvalue = learnvalue;
    this.learntimestamp = learntimestamp;
  }

  public int getLearnconfig() { return learnconfig; }
  public int getLearnindex() { return learnindex; }
  public int getLearnvalue() { return learnvalue; }
  public int getLearntimestamp() { return learntimestamp; }

}
