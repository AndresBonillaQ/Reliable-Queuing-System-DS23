package it.polimi.ds.utils.builder;

import it.polimi.ds.broker.raft.utils.RaftLog;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.*;
import it.polimi.ds.message.raft.response.HeartbeatResponse;
import it.polimi.ds.message.raft.response.RaftLogEntryResponse;
import it.polimi.ds.message.raft.response.ServiceUnavailableResponse;
import it.polimi.ds.message.raft.response.SetUpResponse;
import it.polimi.ds.utils.GsonInstance;

import java.util.List;

public class NetworkMessageBuilder {

    private NetworkMessageBuilder(){}

    public static class Request{
        private Request(){}

        public static RequestMessage buildHeartBeatRequest(String leaderId, int leaderTerm){
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest(leaderId, leaderTerm);

            return new RequestMessage(
                    RequestIdEnum.HEARTBEAT_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(heartbeatRequest)
            );
        }

        public static RequestMessage buildSetUpRequest(String brokerId){
            SetUpRequest setUpRequest = new SetUpRequest(brokerId);

            return new RequestMessage(
                    RequestIdEnum.SET_UP_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(setUpRequest)
            );
        }

        public static RequestMessage buildAppendEntryLogRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm, List<RaftLog> raftLogs){
            RaftLogEntryRequest raftLogEntryRequest = new RaftLogEntryRequest(
                    term,
                    leaderId,
                    prevLogIndex,
                    prevLogTerm,
                    raftLogs
            );

            return new RequestMessage(
                    RequestIdEnum.APPEND_ENTRY_LOG_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(raftLogEntryRequest)
            );
        }

        public static RequestMessage buildCommitRequest(int lastCommitIndex){
            CommitLogRequest commitLogRequest = new CommitLogRequest(lastCommitIndex);

            return new RequestMessage(
                    RequestIdEnum.COMMIT_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(commitLogRequest)
            );
        }

        public static RequestMessage buildVoteRequest(int currentTerm){
            VoteRequest voteRequest = new VoteRequest(currentTerm);

            return new RequestMessage(
                    RequestIdEnum.VOTE_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(voteRequest)
            );
        }

        public static RequestMessage buildNewLeaderToGatewayRequest(String clusterId, String brokerId, String hostName, int port){
            NewLeaderToGatewayRequest newLeaderToGatewayRequest = new NewLeaderToGatewayRequest(clusterId, brokerId, hostName, port);

            return new RequestMessage(
                    RequestIdEnum.NEW_LEADER_TO_GATEWAY_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(newLeaderToGatewayRequest)
            );
        }
    }

    public static class Response{
        private Response(){}

        public static ResponseMessage buildHeartBeatResponse(StatusEnum statusEnum, String desStatus){
            HeartbeatResponse heartbeatResponse = new HeartbeatResponse(statusEnum, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.HEARTBEAT_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(heartbeatResponse)
            );
        }

        public static ResponseMessage buildSetUpResponse(StatusEnum statusEnum, String desStatus){
            SetUpResponse setUpResponse = new SetUpResponse(statusEnum, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.SET_UP_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(setUpResponse)
            );
        }

        public static ResponseMessage buildAppendEntryLogResponse(StatusEnum statusEnum, String desStatus, int lastMatchIndex){
            RaftLogEntryResponse raftLogEntryResponse = new RaftLogEntryResponse(lastMatchIndex, statusEnum, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.APPEND_ENTRY_LOG_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(raftLogEntryResponse)
            );
        }

        public static ResponseMessage buildVoteResponse(StatusEnum statusEnum, String desStatus){
            VoteResponse voteResponse = new VoteResponse(statusEnum, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.VOTE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(voteResponse)
            );
        }

        public static ResponseMessage buildServiceUnavailableResponse(StatusEnum statusEnum, String desStatus, String clientId){
            ServiceUnavailableResponse serviceUnavailableResponse = new ServiceUnavailableResponse(statusEnum, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.SERVICE_UNAVAILABLE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(serviceUnavailableResponse),
                    clientId
            );
        }

    }
}
