package dadkvs.server.domain.utils;

import static dadkvs.server.Constants.DELAY_TIME;
import static dadkvs.server.domain.utils.UtilsFunctions.sleep;

import java.util.Random;

public class SlowMode {

  private boolean slow;

  public SlowMode() {
    this.slow = false;
  }

  public void slow() {
    synchronized (this) {
      this.slow = true;
    }
    System.out.println("[REPLICA] Slowing server...");
  }

  public void unslow() {
    synchronized (this) {
      this.slow = false;
    }
    System.out.println("[REPLICA] Unslowing server...");
  }

  public void delay() {
    synchronized (this) {
      if (!this.slow) return;
    }

    Random rand = new Random();
    int time = rand.nextInt(DELAY_TIME);

    // System.out.println("[REPLICA] delaying for " + time + "ms...");
    sleep(time);
  }

}
