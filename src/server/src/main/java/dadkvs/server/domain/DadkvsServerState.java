package dadkvs.server.domain;

import static dadkvs.server.Constants.N_ACCEPTORS;

import dadkvs.server.domain.paxos.*;
import dadkvs.server.domain.classes.*;
import dadkvs.server.domain.utils.*;

import dadkvs.server.comunications.DadkvsPaxosClient;

import io.grpc.Context;

public class DadkvsServerState {

    private final int my_id;
    LeaderMode leader_mode;
    FrezeMode freze_mode;
    SlowMode slowMode;
    int debug_mode;
    int store_size;
    KeyValueStore store;
    MainLoop main_loop;
    Thread main_loop_worker;
    ConsensusLoop consensus_loop;
    Thread consensus_loop_worker;
    TransactionMonitor transactionMonitor;
    ConsensusMonitor consensusMonitor;
    DadkvsPaxosClient paxosClient;

    public DadkvsServerState(int kv_size, int myself) {
        my_id = myself;
        leader_mode = new LeaderMode(my_id);
        freze_mode = new FrezeMode();
        slowMode = new SlowMode();
        debug_mode = 0;

        store_size = kv_size;
        store = new KeyValueStore(kv_size);

        main_loop = new MainLoop(this);
        main_loop_worker = new Thread(main_loop);

        consensus_loop = new ConsensusLoop(this);
        consensus_loop_worker = new Thread(consensus_loop);

        transactionMonitor = new TransactionMonitor();
        consensusMonitor = new ConsensusMonitor();
        paxosClient = new DadkvsPaxosClient();

        main_loop_worker.start();
        consensus_loop_worker.start();
    }

    public int getMyId() { return this.my_id; }
    public LeaderMode getLeaderMode() { return this.leader_mode; }
    public FrezeMode getFrezeMode() { return this.freze_mode; }
    public SlowMode getSlowMode() { return this.slowMode; }
    public int getDebugMode() { return this.debug_mode; }
    public KeyValueStore getStore() { return this.store; }
    public MainLoop getMainLoop() { return this.main_loop; }
    public ConsensusMonitor getConsensusMonitor() { return this.consensusMonitor; }
    public DadkvsPaxosClient getPaxosClient() { return this.paxosClient; }
    public int getConfig() { return this.store.getConfig(); }

    public boolean matchesConfig(int config) {
        return this.store.getConfig() == config;
    }

    public void changeLeaderMode(boolean value) {
        if (my_id < getConfig() && my_id >= getConfig() + N_ACCEPTORS) {
            // Avoid human errors, this should never happen
            // the console client should never try to make an non-proposer server as leader
            System.out.println("    [LEADER] I can't be a leader on config " + getConfig() + "...");
            return;
        }

        boolean wasLeader = this.leader_mode.isLeader();
        this.leader_mode.changeLeaderState(value);

        if (wasLeader && value == false) {
            this.consensus_loop_worker.interrupt();
        }
    }

    public void changeDebugMode(int value) {
        this.debug_mode = value;

        switch (value) {
            case 1:
                System.out.println("Exiting...");
                System.exit(0);
                break;
            case 2:
                freze_mode.freze();
                break;
            case 3:
                freze_mode.unfreze();
                break;
            case 4:
                slowMode.slow();
                break;
            case 5:
                slowMode.unslow();
                break;
            default:
                break;
        }
    }

    // CLIENT METHODS COMMUNICATIONS

    public VersionedValue read(int key) {
        VersionedValue result = store.read(key);
        // System.out.println("[REPLICA] READ: " + key + " -> " + result.getValue() + " (" + result.getVersion() + ")");
        return result;
    }

    public boolean commit(int reqid, TransactionRecord txrecord) {
        consensusMonitor.addPendingValue(reqid);

        return processTransaction(reqid, txrecord);
    }

    // SERVER METHODS COMMUNICATIONS

    public PhaseOneReply phaseOne(PhaseOneRequest request) {
        int index = request.getPhase1index();
        Paxos paxos = consensusMonitor.getPaxosInstanceOrCreate(index);

        boolean accepted = matchesConfig(request.getPhase1config())
            ? paxos.hasNotPromisedToHigherLider(request.getPhase1timestamp())
            : false;

        int timestamp = paxos.getWriteTimestamp(accepted);
        int value = paxos.getValue(accepted);

        return new PhaseOneReply(getConfig(), index, value, timestamp, accepted);
    }

    public PhaseTwoReply phaseTwo(PhaseTwoRequest request) {
        int index = request.getPhase2index();
        Paxos paxos = consensusMonitor.getPaxosInstanceOrCreate(index);

        boolean accepted = matchesConfig(request.getPhase2config())
            ? paxos.hasNotPromisedToHigherLider(request.getPhase2timestamp(), request.getPhase2value())
            : false;

        // Broadcast the accepted value to the learners, if the value was accepted
        if (accepted) {
            Context ctx = Context.current().fork();
            ctx.run(() -> {
                int value = paxos.getValue(accepted);
                int timestamp = paxos.getWriteTimestamp(accepted);
                LearnPhaseRequest learnRequest = new LearnPhaseRequest(getConfig(), index, value, timestamp);
                paxosClient.learnBroadcast(learnRequest, getConfig());
            });

        }

        return new PhaseTwoReply(getConfig(), index, accepted);
    }

    public LearnPhaseReply learnPhase(LearnPhaseRequest request) {
        int index = request.getLearnindex();

        Paxos paxos = consensusMonitor.getPaxosInstanceOrCreate(index);
        boolean accepted = true;    // The value return from the learner is always accepted
        int timestamp = request.getLearntimestamp();
        int value = request.getLearnvalue();

        if (accepted && paxos.hasEnoughResponses(timestamp)) {
            // Conseusus has been reached, we can add the transaction and start a new one
            consensusMonitor.finishPaxosRound(index, value);
            transactionMonitor.assignTransaction(value, index);
        }

        return new LearnPhaseReply(getConfig(), index, accepted);
    }

    // PRIVATE METHODS (Consensus)

    private boolean processTransaction(int reqid, TransactionRecord txrecord) {
        transactionMonitor.waitForTransactionToBeAssigned(reqid);
        transactionMonitor.waitForPriorTransactionsToFinish(reqid);

        boolean result = store.commit(txrecord);

        if (result && leader_mode.isLeader() && txrecord.getPrepareKey() == 0) {
            // If the server is leader, but it's not in the next configuration, it should stop being leader
            if (my_id < getConfig() || my_id >= getConfig() + N_ACCEPTORS) {
                leader_mode.changeLeaderState(false);
                this.consensus_loop_worker.interrupt();
            }
        }

        System.out.println("[REPLICA] COMMIT: " + reqid + "\n");
        transactionMonitor.incrementCurrentTransactionSeqNumber();
        return result;
    }
}
