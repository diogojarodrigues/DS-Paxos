package dadkvs.server.comunications;

/* these imported classes are generated by the contract */
import dadkvs.DadkvsMain;
import dadkvs.DadkvsMainServiceGrpc;
import dadkvs.server.domain.DadkvsServerState;
import dadkvs.server.domain.VersionedValue;
import dadkvs.server.domain.classes.TransactionRecord;
import io.grpc.stub.StreamObserver;

public class DadkvsMainServiceImpl extends DadkvsMainServiceGrpc.DadkvsMainServiceImplBase {

    DadkvsServerState server_state;
    int timestamp;

    public DadkvsMainServiceImpl(DadkvsServerState state) {
        this.server_state = state;
        this.timestamp = 0;
    }

    @Override
    public void read(DadkvsMain.ReadRequest request, StreamObserver<DadkvsMain.ReadReply> responseObserver) {
        server_state.getFrezeMode().waitUntilUnfrezed();
        server_state.getSlowMode().delay();

        int reqid = request.getReqid();
        int key = request.getKey();
        VersionedValue vv = this.server_state.read(key);

        DadkvsMain.ReadReply response = DadkvsMain.ReadReply.newBuilder()
                .setReqid(reqid)
                .setValue(vv.getValue())
                .setTimestamp(vv.getVersion())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void committx(DadkvsMain.CommitRequest request, StreamObserver<DadkvsMain.CommitReply> responseObserver) {
        server_state.getFrezeMode().waitUntilUnfrezed();
        server_state.getSlowMode().delay();

        int reqid = request.getReqid();
        int key1 = request.getKey1();
        int version1 = request.getVersion1();
        int key2 = request.getKey2();
        int version2 = request.getVersion2();
        int writekey = request.getWritekey();
        int writeval = request.getWriteval();

        this.timestamp++;
        TransactionRecord txrecord = new TransactionRecord(key1, version1, key2, version2, writekey, writeval, this.timestamp);
        boolean result = this.server_state.commit(reqid, txrecord);

        DadkvsMain.CommitReply response = DadkvsMain.CommitReply.newBuilder().setReqid(reqid).setAck(result).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
