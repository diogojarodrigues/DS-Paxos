package dadkvs.server.domain.utils;

public class UtilsFunctions {

  public static void sleep(int time) {
    try {
        Thread.sleep(time);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
  }

  public static void waitt(Object obj) {
    try {
      obj.wait();
    } catch (InterruptedException e1) {
        try {
          obj.wait();
        } catch (InterruptedException e2) {
          System.err.println("COULD NOT WAIT, INTERRUPTED EXCEPTION");
        }
    }
  }
}
