package it.polimi.ds.utils.builder;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.AppendValueResponse;
import it.polimi.ds.message.model.response.CreateQueueResponse;
import it.polimi.ds.message.model.response.ReadValueResponse;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;

public class ModelResponseMessageBuilder {

    private ModelResponseMessageBuilder(){}

    public static class OK {
        public static ResponseMessage buildCreateQueueResponseMessage(String queueId, String clientId) {
            ResponseMessage responseMessage = new ResponseMessage();

            CreateQueueResponse createQueueResponse = new CreateQueueResponse(StatusEnum.OK, Const.ResponseDes.OK.CREATE_QUEUE, queueId);

            responseMessage.setId(ResponseIdEnum.CREATE_QUEUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(createQueueResponse));
            responseMessage.setClientId(clientId);

            return responseMessage;
        }

        public static ResponseMessage buildAppendValueResponseMessage(String clientId) {
            ResponseMessage responseMessage = new ResponseMessage();

            AppendValueResponse appendValueResponse = new AppendValueResponse();
            appendValueResponse.setStatus(StatusEnum.OK);
            appendValueResponse.setDesStatus(Const.ResponseDes.OK.APPEND_VALUE);

            responseMessage.setId(ResponseIdEnum.APPEND_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(appendValueResponse));
            responseMessage.setClientId(clientId);


            return responseMessage;
        }

        public static ResponseMessage buildReadValueResponseMessage(int value,String clientId) {
            ResponseMessage responseMessage = new ResponseMessage();

            ReadValueResponse readValueResponse = new ReadValueResponse();
            readValueResponse.setValue(value);
            readValueResponse.setStatus(StatusEnum.OK);
            readValueResponse.setDesStatus(Const.ResponseDes.OK.READ_VALUE);

            responseMessage.setId(ResponseIdEnum.READ_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(readValueResponse));
            responseMessage.setClientId(clientId);


            return responseMessage;
        }
    }

    public static class KO {
        public static ResponseMessage buildCreateQueueResponseMessage(String clientId, String desStatus, String queueId) {
            ResponseMessage responseMessage = new ResponseMessage();

            CreateQueueResponse createQueueResponse = new CreateQueueResponse(StatusEnum.KO, desStatus, queueId);

            responseMessage.setId(ResponseIdEnum.CREATE_QUEUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(createQueueResponse));
            responseMessage.setClientId(clientId);

            return responseMessage;
        }

        public static ResponseMessage buildAppendValueResponseMessage(String clientId, String desStatus) {
            ResponseMessage responseMessage = new ResponseMessage();

            AppendValueResponse appendValueResponse = new AppendValueResponse();
            appendValueResponse.setStatus(StatusEnum.KO);
            appendValueResponse.setDesStatus(desStatus);

            responseMessage.setId(ResponseIdEnum.APPEND_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(appendValueResponse));
            responseMessage.setClientId(clientId);

            return responseMessage;
        }

        public static ResponseMessage buildReadValueResponseMessage(String clientId, String desStatus) {
            ResponseMessage responseMessage = new ResponseMessage();

            ReadValueResponse readValueResponse = new ReadValueResponse();
            readValueResponse.setStatus(StatusEnum.KO);
            readValueResponse.setDesStatus(desStatus);
            responseMessage.setClientId(clientId);

            responseMessage.setId(ResponseIdEnum.READ_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(readValueResponse));

            return responseMessage;
        }
    }
}
