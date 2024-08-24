package it.polimi.ds.message.election.requestHandler;

import com.google.gson.Gson;
import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.network.handler.FollowerRequestHandler;
import it.polimi.ds.utils.GsonInstance;

public class FollowerVoteRequestHandler implements FollowerRequestHandler {

    @Override
    public ResponseMessage exec(BrokerContext brokerContext, RequestMessage request) {
        VoteRequest voteRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), VoteRequest.class);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId(ResponseIdEnum.VOTE_RESPONSE);
        VoteResponse voteResponse = new VoteResponse();

        if (brokerContext.getBrokerRaftIntegration().getCurrentTerm() <= voteRequest.getTerm())
            voteResponse.setStatus(StatusEnum.OK);
        else
            voteResponse.setStatus(StatusEnum.KO);
        responseMessage.setContent( new Gson().toJson(voteResponse) );
        return responseMessage;

        //ci sarebbe da aggiungere il caso in cui il follower ha già votato
    }
}
