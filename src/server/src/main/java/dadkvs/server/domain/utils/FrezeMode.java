package dadkvs.server.domain.utils;

import static dadkvs.server.domain.utils.UtilsFunctions.waitt;

public class FrezeMode {

  private Boolean freze;

  public FrezeMode() {
    this.freze = false;
  }

  public void freze() {
    synchronized (this) {
      this.freze = true;
    }
    System.out.println("[REPLICA] Frezing server...");
  }

  public void unfreze() {
    synchronized (this) {
      this.freze = false;
      this.notifyAll();
    }
    System.out.println("[REPLICA] Unfrezing server...");
  }

  public void waitUntilUnfrezed() {
    synchronized (this) {
      while (this.freze) {
        waitt(this);
      }
    }
  }
}
