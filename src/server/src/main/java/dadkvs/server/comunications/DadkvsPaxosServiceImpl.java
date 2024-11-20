package dadkvs.server.comunications;

import dadkvs.DadkvsPaxos;
import dadkvs.DadkvsPaxosServiceGrpc;
import dadkvs.server.domain.DadkvsServerState;
import dadkvs.server.domain.classes.*;
import io.grpc.stub.StreamObserver;

public class DadkvsPaxosServiceImpl extends DadkvsPaxosServiceGrpc.DadkvsPaxosServiceImplBase {

    DadkvsServerState server_state;

    public DadkvsPaxosServiceImpl(DadkvsServerState state) {
        this.server_state = state;
    }

    @Override
    public void phaseone(DadkvsPaxos.PhaseOneRequest request, StreamObserver<DadkvsPaxos.PhaseOneReply> responseObserver) {
        server_state.getFrezeMode().waitUntilUnfrezed();
        server_state.getSlowMode().delay();

        PhaseOneRequest aux_req = new PhaseOneRequest(request.getPhase1Config(), request.getPhase1Index(), request.getPhase1Timestamp());
        PhaseOneReply aux_rep = this.server_state.phaseOne(aux_req);

        DadkvsPaxos.PhaseOneReply reply = DadkvsPaxos.PhaseOneReply
                .newBuilder()
                .setPhase1Config(aux_rep.getPhase1config())
                .setPhase1Index(aux_rep.getPhase1index())
                .setPhase1Accepted(aux_rep.getPhase1accepted())
                .setPhase1Value(aux_rep.getPhase1value())
                .setPhase1Timestamp(aux_rep.getPhase1timestamp())
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void phasetwo(DadkvsPaxos.PhaseTwoRequest request, StreamObserver<DadkvsPaxos.PhaseTwoReply> responseObserver) {
        server_state.getFrezeMode().waitUntilUnfrezed();
        server_state.getSlowMode().delay();

        PhaseTwoRequest aux_req = new PhaseTwoRequest(request.getPhase2Config(), request.getPhase2Index(), request.getPhase2Value(), request.getPhase2Timestamp());
        PhaseTwoReply aux_rep = this.server_state.phaseTwo(aux_req);

        DadkvsPaxos.PhaseTwoReply reply = DadkvsPaxos.PhaseTwoReply
                .newBuilder()
                .setPhase2Config(aux_rep.getPhase2config())
                .setPhase2Index(aux_rep.getPhase2index())
                .setPhase2Accepted(aux_rep.getPhase2accepted())
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void learn(DadkvsPaxos.LearnRequest request, StreamObserver<DadkvsPaxos.LearnReply> responseObserver) {
        server_state.getFrezeMode().waitUntilUnfrezed();
        server_state.getSlowMode().delay();

        LearnPhaseRequest aux_req = new LearnPhaseRequest(request.getLearnconfig(), request.getLearnindex(), request.getLearnvalue(), request.getLearntimestamp());
        LearnPhaseReply aux_rep = this.server_state.learnPhase(aux_req);

        DadkvsPaxos.LearnReply reply = DadkvsPaxos.LearnReply
                .newBuilder()
                .setLearnconfig(aux_rep.getLearnconfig())
                .setLearnindex(aux_rep.getLearnindex())
                .setLearnaccepted(aux_rep.getLearnaccepted())
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
