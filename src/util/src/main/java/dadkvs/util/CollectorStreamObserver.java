package dadkvs.util;

import io.grpc.stub.StreamObserver;

public class CollectorStreamObserver<T> implements StreamObserver<T> {

    dadkvs.util.GenericResponseCollector<T> collector;
    boolean done;

    public CollectorStreamObserver(GenericResponseCollector<T> c) {
        collector = c;
        done = false;
    }

    @Override
    public void onNext(T value) {
        // Handle the received response of type T
        // System.out.print("Received response:\n" + value);
        if (done == false) {
            collector.addResponse(value);
            done = true;
        }
    }

    @Override
    public void onError(Throwable t) {
        // Handle error
        // System.err.println("Error occurred: " + t.getMessage() + "\n");
        if (done == false) {
            collector.addNoResponse();
            done = true;
        }
    }

    @Override
    public void onCompleted() {
        // Handle stream completion
        // System.out.println("Stream completed\n");
        if (done == false) {
            collector.addNoResponse();
            done = true;
        }
    }
}
