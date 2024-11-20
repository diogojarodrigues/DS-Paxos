package dadkvs.server.comunications;

import static dadkvs.server.Constants.*;

import java.util.List;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import dadkvs.util.CollectorStreamObserver;
import dadkvs.util.GenericResponseCollector;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import dadkvs.DadkvsPaxos;
import dadkvs.DadkvsPaxosServiceGrpc;
import dadkvs.server.domain.classes.*;

public class DadkvsPaxosClient {
    List<DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub> async_stubs;

	public DadkvsPaxosClient() {
		this.async_stubs = initComms();
	}

	public List<PhaseOneReply> prepareBroadcast(PhaseOneRequest req, int config) {
        DadkvsPaxos.PhaseOneRequest request = DadkvsPaxos.PhaseOneRequest
                .newBuilder()
                .setPhase1Config(req.getPhase1config())
                .setPhase1Index(req.getPhase1index())
                .setPhase1Timestamp(req.getPhase1timestamp())
                .build();

        List<DadkvsPaxos.PhaseOneReply> replies = broadcast(
            request,
            (stub, observer) -> stub.phaseone(request, observer),
            "Sent phaseOne request to server",
            MAJORITY,
            config,
            false
        );

        return replies.stream()
                      .map(reply -> new PhaseOneReply(reply.getPhase1Config(), reply.getPhase1Index(), reply.getPhase1Value(), reply.getPhase1Timestamp(), reply.getPhase1Accepted()))
                      .toList();
    }

    public List<PhaseTwoReply> acceptBroadcast(PhaseTwoRequest req, int config) {
        DadkvsPaxos.PhaseTwoRequest request = DadkvsPaxos.PhaseTwoRequest
                .newBuilder()
                .setPhase2Config(req.getPhase2config())
                .setPhase2Index(req.getPhase2index())
                .setPhase2Timestamp(req.getPhase2timestamp())
                .setPhase2Value(req.getPhase2value())
                .build();

        List<DadkvsPaxos.PhaseTwoReply> replies = broadcast(
            request,
            (stub, observer) -> stub.phasetwo(request, observer),
            "Sent phaseTwo request to server",
            MAJORITY,
            config,
            false
        );

        return replies.stream()
                      .map(reply -> new PhaseTwoReply(reply.getPhase2Config(), reply.getPhase2Index(), reply.getPhase2Accepted()))
                      .toList();
    }

    public List<LearnPhaseReply> learnBroadcast(LearnPhaseRequest req, int config) {
        DadkvsPaxos.LearnRequest request = DadkvsPaxos.LearnRequest
                .newBuilder()
                .setLearnconfig(req.getLearnconfig())
                .setLearnindex(req.getLearnindex())
                .setLearntimestamp(req.getLearntimestamp())
                .setLearnvalue(req.getLearnvalue())
                .build();

        List<DadkvsPaxos.LearnReply> replies = broadcast(
            request,
            (stub, observer) -> stub.learn(request, observer),
            "Sent learn request to server",
            NONE,
            config,
            true
        );

        return replies.stream()
                      .map(reply -> new LearnPhaseReply(reply.getLearnconfig(), reply.getLearnindex(), reply.getLearnaccepted()))
                      .toList();
    }

    // PRIVATE METHODS

    private List<DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub> initComms() {
        List<DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub> async_stubs = new ArrayList<>();

        for (int id = 0; id < N_SERVERS; id++) {
            String aux_target;
            ManagedChannel aux_channel;

            aux_target = "localhost:" + (BASE_PORT + id);
            aux_channel = ManagedChannelBuilder.forTarget(aux_target).usePlaintext().build();
            async_stubs.add(DadkvsPaxosServiceGrpc.newStub(aux_channel));
        }

        return async_stubs;
    }

    private <R, T> List<T> broadcast(
        R request,
        BiConsumer<DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub, CollectorStreamObserver<T>> rpcCall,
        String message,
        int num_responses,
        int config,
        boolean broadcast_to_all
    ) {
        ArrayList<T> responses = new ArrayList<T>();
        final int TOTAL_RESPONSES = broadcast_to_all ? N_SERVERS : N_ACCEPTORS;

        GenericResponseCollector<T> collector = new GenericResponseCollector<T>(responses, TOTAL_RESPONSES);

        for (int index = 0; index < N_SERVERS; index++) {
            // If we are not broadcasting to all servers, we should only broadcast to acceptors
            if (
                !broadcast_to_all &&
                (index < config || index >= config + N_ACCEPTORS)
            ) {
                // If the server is not an acceptor, skip
                continue;
            }

            // System.out.println("\t" + message + " " + (BASE_PORT + index));

            DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub stub = async_stubs.get(index);
            CollectorStreamObserver<T> observer = new CollectorStreamObserver<>(collector);
            rpcCall.accept(stub, observer);
        }

        collector.waitForTarget(num_responses);

        return responses;
    }

}
