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
            CreateQueueResponse createQueueResponse = new CreateQueueResponse(StatusEnum.OK, Const.ResponseDes.OK.CREATE_QUEUE, queueId);

            return new ResponseMessage(
                    ResponseIdEnum.CREATE_QUEUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(createQueueResponse),
                    clientId
            );
        }

        public static ResponseMessage buildAppendValueResponseMessage(String clientId) {
            AppendValueResponse appendValueResponse = new AppendValueResponse(StatusEnum.OK, Const.ResponseDes.OK.APPEND_VALUE);

            return new ResponseMessage(
                    ResponseIdEnum.APPEND_VALUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(appendValueResponse),
                    clientId
            );
        }

        public static ResponseMessage buildReadValueResponseMessage(int value,String clientId) {
            ReadValueResponse readValueResponse = new ReadValueResponse(StatusEnum.OK, Const.ResponseDes.OK.READ_VALUE, value);

            return new ResponseMessage(
                    ResponseIdEnum.READ_VALUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(readValueResponse),
                    clientId
            );
        }
    }

    public static class KO {
        public static ResponseMessage buildCreateQueueResponseMessage(String clientId, String desStatus, String queueId) {
            CreateQueueResponse createQueueResponse = new CreateQueueResponse(StatusEnum.KO, desStatus, queueId);

            return new ResponseMessage(
                    ResponseIdEnum.CREATE_QUEUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(createQueueResponse),
                    clientId
            );
        }

        public static ResponseMessage buildAppendValueResponseMessage(String clientId, String desStatus) {
            AppendValueResponse appendValueResponse = new AppendValueResponse(StatusEnum.KO, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.APPEND_VALUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(appendValueResponse),
                    clientId
            );
        }

        public static ResponseMessage buildReadValueResponseMessage(String clientId, String desStatus) {
            ReadValueResponse readValueResponse = new ReadValueResponse(StatusEnum.KO, desStatus);

            return new ResponseMessage(
                    ResponseIdEnum.READ_VALUE_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(readValueResponse),
                    clientId
            );
        }
    }
}
