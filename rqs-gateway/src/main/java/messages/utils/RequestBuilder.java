package messages.utils;

import com.google.gson.Gson;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import messages.responses.AppendValueResponse;
import messages.responses.CreateQueueResponse;

public class RequestBuilder {

    public String createAppendValueRequest(AppendValueRequest request) {
        return new Gson().toJson(request, AppendValueResponse.class);
    }

    public String createNewQueueRequest(CreateQueueRequest request, String queueId) {
        CreateQueueResponse createQueueResponse = new CreateQueueResponse();
        createQueueResponse.setQueueId(queueId);

        return new Gson().toJson(createQueueResponse, CreateQueueResponse.class);

    }

    public String createReadValueResponse(ReadValueRequest request) {
        return new Gson().toJson(request, ReadValueRequest.class);
    }
}
