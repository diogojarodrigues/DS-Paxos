package dadkvs.server.domain.paxos;

import java.util.List;

import dadkvs.server.domain.classes.*;
import dadkvs.server.domain.DadkvsServerState;

public class ConsensusLoop implements Runnable {
  private DadkvsServerState server_state;

  public ConsensusLoop(DadkvsServerState state) {
    server_state = state;
  }

  public int getConfig() {
    return server_state.getConfig();
  }

  @Override
  public void run() {
    while (true) {
      try {
        waitToBeLeader();
        waitForPendingTransactions();

        startConsensus();
      } catch (Exception e) {
        // Leader is not the leader anymore
        System.out.println("    [LEADER] INTERRUPTING PAXOS ROUND\n");
      }
    }
  }

  private void startConsensus() {
    ConsensusMonitor consensusMonitor = server_state.getConsensusMonitor();
    LiderPaxos paxos = consensusMonitor.startPaxosRound(server_state.getMyId());

    System.out.println("    [LEADER] START PAXOS ROUND: " + paxos.getIndex());
    while (true) {
      int proposed_value = prepare(paxos);
      if (proposed_value == -1) continue;

      boolean consensus_finished = accept(paxos, proposed_value);
      if (!consensus_finished) continue;

      consensusMonitor.finishPaxosRound(paxos.getIndex(), proposed_value);
      System.out.println("    [LEADER] CLOSE PAXOS ROUND: " + paxos.getIndex() + " WITH VALUE: " + proposed_value + "\n");
      break;
    }
  }

  // PAXOS ALGORITHM

  private int prepare(LiderPaxos paxos) {
    // Send prepare request to all proposers, incrementing the timestamp of the leader
    System.out.println("    [LEADER] Do prepare request: " + paxos.getIndex() + " (timestamp=" + (paxos.getTimestamp() + 3) + ")");
    PhaseOneRequest phaseOneRequest = new PhaseOneRequest(getConfig(), paxos.getIndex(), paxos.nextTimestamp());
    List<PhaseOneReply> phaseOneReplies = server_state.getPaxosClient().prepareBroadcast(phaseOneRequest, getConfig());

    // If there is negative response from the proposers, it means that there is other leader with higher timestamp
    // So, we need to increment the timestamp of the leader and try again
    if (!checkPhase1Replies(phaseOneReplies)) return -1;

    // Choose the value to propose
    int proposed_value = choose_value_to_propose(phaseOneReplies);
    System.out.println("    [LEADER] Proposing value: " + proposed_value);

    return proposed_value;
  }

  private boolean accept(LiderPaxos paxos, int proposed_value) {
    System.out.println("    [LEADER] Do accept request: " + paxos.getIndex() + " (val=" + proposed_value + ", time=" + paxos.getTimestamp() + ")");

    // Send accept request to all acceptors
    PhaseTwoRequest phase2Request = new PhaseTwoRequest(getConfig(), paxos.getIndex(), proposed_value, paxos.getTimestamp());
    List<PhaseTwoReply> phaseTwoReplies = server_state.getPaxosClient().acceptBroadcast(phase2Request, getConfig());

    // If there is negative response from the acceptors, it means that there is other leader with higher timestamp
    // So, we need to increment the timestamp of the leader and try again
    return checkPhase2Replies(phaseTwoReplies);
  }

  // AUXILIARY METHODS

  private synchronized void waitToBeLeader() {
    server_state.getLeaderMode().waitUntilIsLider();
    System.out.println("    [LEADER] Checking if there is pending transaction");
  }

  private synchronized void waitForPendingTransactions() {
    server_state.getConsensusMonitor().waitForPendingValues();
    System.out.println("    [LEADER] Found pending transaction, starting...\n");
  }

  private int choose_value_to_propose(List<PhaseOneReply> replies) {
    PhaseOneReply reply_with_higher_timestamp = replies.stream()
        .max(PhaseOneReply::compareTo)
        .orElseThrow(() -> new RuntimeException("    [LEADER] No reply with higher timestamp"));

    // If a previous leader has already chosen a value, we can use it
    if (reply_with_higher_timestamp.getPhase1timestamp() != -1) {
      return reply_with_higher_timestamp.getPhase1value();
    }

    // If there is no work done by the previous leader, we can choose the value
    return server_state.getConsensusMonitor().selectTransaction();
  }

  private boolean checkPhase1Replies(List<PhaseOneReply> replies) {
    System.out.println("    [LEADER] Checking phase 1 replies: " + replies.stream().filter(PhaseOneReply::getPhase1accepted).count() + "/" + replies.size());
    return replies.stream().allMatch(PhaseOneReply::getPhase1accepted);
  }

  private boolean checkPhase2Replies(List<PhaseTwoReply> replies) {
    System.out.println("    [LEADER] Checking phase 2 replies: " + replies.stream().filter(PhaseTwoReply::getPhase2accepted).count() + "/" + replies.size());
    return replies.stream().allMatch(PhaseTwoReply::getPhase2accepted);
  }

}
