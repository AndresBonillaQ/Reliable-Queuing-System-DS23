package it.polimi.ds.utils;

import it.polimi.ds.broker.raft.impl.RaftLog;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.CommitLogRequest;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;
import it.polimi.ds.message.raft.request.SetUpRequest;
import it.polimi.ds.message.raft.response.HeartbeatResponse;
import it.polimi.ds.message.raft.response.RaftLogEntryResponse;
import it.polimi.ds.message.raft.response.SetUpResponse;

import java.util.List;

public class NetworkMessageBuilder {

    private NetworkMessageBuilder(){}

    public static class Request{
        private Request(){}

        public static RequestMessage buildHeartBeatRequest(String leaderId){
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest(leaderId);

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
    }

    public static class Response{
        private Response(){}

        public static ResponseMessage buildHeartBeatResponse(){
            HeartbeatResponse heartbeatResponse = new HeartbeatResponse();

            return new ResponseMessage(
                    ResponseIdEnum.HEARTBEAT_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(heartbeatResponse)
            );
        }

        public static ResponseMessage buildSetUpResponse(StatusEnum statusEnum, String desStatus){
            SetUpResponse setUpResponse = new SetUpResponse();

            setUpResponse.setStatus(statusEnum);
            setUpResponse.setDesStatus(desStatus);

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
    }
}
