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
        public static ResponseMessage buildCreateQueueResponseMessage(String clientId) {
            ResponseMessage responseMessage = new ResponseMessage();

            CreateQueueResponse createQueueResponse = new CreateQueueResponse();
            createQueueResponse.setStatus(StatusEnum.OK);
            createQueueResponse.setDesStatus(Const.ResponseDes.OK.CREATE_QUEUE);

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

            responseMessage.setId(ResponseIdEnum.CREATE_QUEUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(readValueResponse));
            responseMessage.setClientId(clientId);


            return responseMessage;
        }
    }

    public static class KO {
        public static ResponseMessage buildCreateQueueResponseMessage(String desStatus) {
            ResponseMessage responseMessage = new ResponseMessage();

            CreateQueueResponse createQueueResponse = new CreateQueueResponse();
            createQueueResponse.setStatus(StatusEnum.KO);
            createQueueResponse.setDesStatus(desStatus);

            responseMessage.setId(ResponseIdEnum.CREATE_QUEUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(createQueueResponse));

            return responseMessage;
        }

        public static ResponseMessage buildAppendValueResponseMessage(String desStatus) {
            ResponseMessage responseMessage = new ResponseMessage();

            AppendValueResponse appendValueResponse = new AppendValueResponse();
            appendValueResponse.setStatus(StatusEnum.KO);
            appendValueResponse.setDesStatus(desStatus);

            responseMessage.setId(ResponseIdEnum.APPEND_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(appendValueResponse));

            return responseMessage;
        }

        public static ResponseMessage buildReadValueResponseMessage(String desStatus) {
            ResponseMessage responseMessage = new ResponseMessage();

            ReadValueResponse readValueResponse = new ReadValueResponse();
            readValueResponse.setStatus(StatusEnum.KO);
            readValueResponse.setDesStatus(desStatus);

            responseMessage.setId(ResponseIdEnum.READ_VALUE_RESPONSE);
            responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(readValueResponse));

            return responseMessage;
        }
    }
}
