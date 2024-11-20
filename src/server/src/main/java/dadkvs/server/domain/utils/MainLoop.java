package dadkvs.server.domain.utils;

import static dadkvs.server.domain.utils.UtilsFunctions.waitt;

import dadkvs.server.domain.DadkvsServerState;

public class MainLoop implements Runnable {
    DadkvsServerState server_state;

    private boolean has_work;

    public MainLoop(DadkvsServerState state) {
        this.server_state = state;
        this.has_work = false;
    }

    public void run() {
        while (true) this.doWork();
    }

    public synchronized void doWork() {
        // System.out.println("Main loop do work start");
        this.has_work = false;
        while (this.has_work == false) {
            // System.out.println("Main loop do work: waiting");
            waitt(this);
        }
        // System.out.println("Main loop do work finish");
    }

    public synchronized void wakeup() {
        this.has_work = true;
        notify();
    }
}
