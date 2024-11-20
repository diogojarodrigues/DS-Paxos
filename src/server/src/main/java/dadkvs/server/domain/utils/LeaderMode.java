package dadkvs.server.domain.utils;

import static dadkvs.server.Constants.N_LIDER_SERVERS;
import static dadkvs.server.domain.utils.UtilsFunctions.waitt;

public class LeaderMode {

  private Boolean leader;

  public LeaderMode(int my_id) {
    this.leader = my_id < N_LIDER_SERVERS ? true : false;
  }

  public boolean isLeader() { return this.leader; }

  public void changeLeaderState(boolean isLider) {
    if (isLider) {
      leader();
    } else {
      unleader();
    }
  }

  private void unleader() {
    synchronized (this) {
      this.leader = false;
    }
    System.out.println("    [LEADER] I am no longer a leader...");
  }

  private void leader() {
    synchronized (this) {
      this.leader = true;
      this.notifyAll();
    }
    System.out.println("    [LEADER] I am now a leader...");
  }

  public void waitUntilIsLider() {
    synchronized (this) {
      while (!this.leader) {
        waitt(this);
      }
    }
  }
}
