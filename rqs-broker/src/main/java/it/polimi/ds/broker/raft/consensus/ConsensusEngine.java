package it.polimi.ds.broker.raft.consensus;

import it.polimi.ds.broker.raft.IBrokerRaftIntegration;
import it.polimi.ds.exception.network.ConsensusRetryException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.response.RaftLogEntryResponse;
import it.polimi.ds.network.utils.LeaderWaitingForFollowersCallable;
import it.polimi.ds.network.utils.LeaderWaitingForFollowersResponse;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;

import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsensusEngine implements IConsensusEngine {

    private final Logger log = Logger.getLogger(ConsensusEngine.class.getName());
    private final Set<String> brokerIdSetVotedOk = new ConcurrentSkipListSet<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CompletionService<LeaderWaitingForFollowersResponse> completionService = new ExecutorCompletionService<>(executorService);

    @Override
    public Set<String> calculateConsensus(String leaderId, IBrokerRaftIntegration raftIntegration) {

        brokerIdSetVotedOk.clear();

        final int numFollowersAlive = ThreadsCommunication.getInstance().getNumThreadsOfAliveBrokers();

        if(numFollowersAlive > 0){
            submitThreadsToReceiveRequestFromFollowers();
            consumeFollowerResponses(leaderId, raftIntegration, numFollowersAlive);
        }

        return brokerIdSetVotedOk;
    }

    private void submitThreadsToReceiveRequestFromFollowers(){
        ThreadsCommunication.getInstance()
                .getResponseConcurrentHashMap()
                .forEach((queueId, responseQueue) -> {
                    completionService.submit(new LeaderWaitingForFollowersCallable(queueId, responseQueue));
                });
    }

    private void consumeFollowerResponses(String leaderId, IBrokerRaftIntegration raftIntegration, int numFollowersAlive){

        for (int i = 0; i < numFollowersAlive; i++) {

            Future<LeaderWaitingForFollowersResponse> response;
            try {
                response = completionService.take();
            } catch (InterruptedException e) {
                log.log(Level.INFO, "InterruptedException of completionService.take while waiting for a response");
                continue;
            }

            LeaderWaitingForFollowersResponse leaderWaitingForFollowersResponse = null;
            try {
                leaderWaitingForFollowersResponse = response.get();
            } catch (InterruptedException e) {
                log.log(Level.INFO, "InterruptedException of response.get while waiting for a response");
                continue;
            } catch (ExecutionException e) {
                log.log(Level.INFO, "ExecutionException of response.get while waiting for a response");
                continue;
            }

            log.log(Level.INFO, "Response from follower taken from queue: {0}", leaderWaitingForFollowersResponse);
            handleFollowerResponse(leaderId, raftIntegration, leaderWaitingForFollowersResponse);
        }
    }

    private void handleFollowerResponse(String leaderId, IBrokerRaftIntegration raftIntegration, LeaderWaitingForFollowersResponse leaderWaitingForFollowersResponse){

        final String response = leaderWaitingForFollowersResponse.getResponse();
        final String brokerIdOfResponse = leaderWaitingForFollowersResponse.getBrokerId();

        ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(response, ResponseMessage.class);

        if(ResponseIdEnum.APPEND_ENTRY_LOG_RESPONSE.equals(responseMessage.getId())) {
            RaftLogEntryResponse raftLogEntryResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), RaftLogEntryResponse.class);

            if (StatusEnum.OK.equals(raftLogEntryResponse.getStatus())) {
                log.log(Level.INFO, "Consensus +1 from brokerId {0}", brokerIdOfResponse);
                brokerIdSetVotedOk.add(brokerIdOfResponse);
            } else
                handleRetryRaftLogRequest(leaderId, raftIntegration, raftLogEntryResponse.getLastMatchIndex(), brokerIdOfResponse);
        } else
            log.log(Level.SEVERE, "Waiting for APPEND_ENTRY_LOG_RESPONSE but received another kind of message!");
    }

    private void handleRetryRaftLogRequest(String leaderId, IBrokerRaftIntegration raftIntegration, int lastMatchIndexResp, String brokerIdOfResponse){
        RetryRequestOutcome retryRequestOutcome = new RetryRequestOutcome(false, lastMatchIndexResp);
        do {
            sendRetryRequest(buildRetryRequest(leaderId, raftIntegration, retryRequestOutcome.lastIndexMatch), brokerIdOfResponse);
            try {
                retryRequestOutcome = consumeRetryRequest(brokerIdOfResponse);
            } catch (ExecutionException | InterruptedException | ConsensusRetryException e) {
                log.log(Level.SEVERE, "Error by executing retry logic for consensus with broker {0}", brokerIdOfResponse);
                return;
            }
            log.log(Level.INFO, "RetryOutcome: {0}", retryRequestOutcome);
        } while (!retryRequestOutcome.hasBeenAccepted && retryRequestOutcome.lastIndexMatch >= -1);
    }

    private String buildRetryRequest(String leaderId, IBrokerRaftIntegration raftIntegration, int lastMatchIndex){
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildAppendEntryLogRequest(
                raftIntegration.getCurrentTerm(),
                leaderId,
                raftIntegration.getPrevLogIndexOf(lastMatchIndex),
                raftIntegration.getPrevLogTermOfIndex(lastMatchIndex),
                raftIntegration.getRaftLogEntriesFromIndex(lastMatchIndex)
        );
        log.log(Level.INFO, "AppendLogEntryRequest new to solve is {0}", requestMessage);

        return GsonInstance.getInstance().getGson().toJson(requestMessage);
    }

    private void sendRetryRequest(String requestLine, String brokerIdOfResponse){
        ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(brokerIdOfResponse).add(requestLine);
    }

    private RetryRequestOutcome consumeRetryRequest(String brokerIdOfResponse) throws ExecutionException, InterruptedException, ConsensusRetryException {

        Future<LeaderWaitingForFollowersResponse> responseCallableRetry = executorService.submit(
                new LeaderWaitingForFollowersCallable(
                        brokerIdOfResponse,
                        ThreadsCommunication.getInstance().getResponseConcurrentHashMapOfBrokerId(brokerIdOfResponse)
                )
        );

        ResponseMessage responseMessageCallableRetry;
        try {
            responseMessageCallableRetry = GsonInstance.getInstance().getGson().fromJson(responseCallableRetry.get().getResponse(), ResponseMessage.class);
        } catch (InterruptedException e) {
            log.log(Level.INFO, "InterruptedException while waiting for responseQueue od brokerId {0}", brokerIdOfResponse);
            throw e;
        } catch (ExecutionException e) {
            log.log(Level.INFO, "ExecutionException while waiting for responseQueue od brokerId {0}", brokerIdOfResponse);
            throw e;
        }

        log.log(Level.INFO, "AppendLogEntryResponse of the new to solve is {0}", responseMessageCallableRetry);

        if(ResponseIdEnum.APPEND_ENTRY_LOG_RESPONSE.equals(responseMessageCallableRetry.getId())){
            RaftLogEntryResponse raftLogEntryResponse = GsonInstance.getInstance().getGson().fromJson(responseMessageCallableRetry.getContent(), RaftLogEntryResponse.class);

            if (StatusEnum.OK.equals(raftLogEntryResponse.getStatus())) {
                log.log(Level.INFO, "Consensus +1 from brokerId {0}", brokerIdOfResponse);
                brokerIdSetVotedOk.add(brokerIdOfResponse);
                return new RetryRequestOutcome(true, -1);
            }

            return new RetryRequestOutcome(false, raftLogEntryResponse.getLastMatchIndex());
        }

        throw new ConsensusRetryException("Error in ConsensusEngine, waiting for an APPEND_ENTRY_LOG_RESPONSE but received " + responseMessageCallableRetry.toString());
    }

    class RetryRequestOutcome{
        private final boolean hasBeenAccepted;
        private final int lastIndexMatch;

        public RetryRequestOutcome(boolean hasBeenAccepted, int lastIndexMatch) {
            this.hasBeenAccepted = hasBeenAccepted;
            this.lastIndexMatch = lastIndexMatch;
        }

        public boolean isHasBeenAccepted() {
            return hasBeenAccepted;
        }

        public int getLastIndexMatch() {
            return lastIndexMatch;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("RetryRequestOutcome{");
            sb.append("hasBeenAccepted=").append(hasBeenAccepted);
            sb.append(", lastIndexMatch=").append(lastIndexMatch);
            sb.append('}');
            return sb.toString();
        }
    }
}