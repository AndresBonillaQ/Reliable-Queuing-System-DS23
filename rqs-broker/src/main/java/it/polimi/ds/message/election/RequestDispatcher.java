package it.polimi.ds.message.election;

import com.google.gson.Gson;
import it.polimi.ds.broker2.state.impl.FollowerBrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.RequestVote;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.utils.GsonInstance;

public class RequestDispatcher {

    public static String processRequest(String request, Integer termNumber, FollowerBrokerState brokerState) {

        Gson gson1= new Gson();
        Gson gson2 = new Gson();
        if (gson1.fromJson(request, RequestMessage.class).getId().equals(RequestIdEnum.VOTE_REQUEST.getValue())) {
            RequestVote requestVote = gson1.fromJson(gson1.fromJson(request, RequestMessage.class).getContent(), RequestVote.class);
            ResponseMessage responseMessage = new ResponseMessage();
            VoteResponse voteResponse = new VoteResponse();
            responseMessage.setId(ResponseIdEnum.VOTE_OUTCOME);

            String response = gson2.toJson(responseMessage.getContent(), ResponseMessage.class);

            if (termNumber <= requestVote.getTerm()) {
                voteResponse.setOutcome("OK");
                responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(voteResponse));
                brokerState.setVoted(true);
            } else {
                voteResponse.setOutcome("KO");
                responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(voteResponse));
                brokerState.setVoted(true);
            }

            return gson2.toJson(responseMessage.getContent(), ResponseMessage.class);
        }if (gson1.fromJson(request, RequestMessage.class).getId().equals(RequestIdEnum.HEARTBEAT_REQUEST.getValue())) {
            brokerState.notifyHeartbeatLock();
        }
        return "";
    }
}
