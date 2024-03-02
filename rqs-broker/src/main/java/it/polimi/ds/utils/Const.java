package it.polimi.ds.utils;

public class Const {
    private Const(){}

    public static class ResponseDes {
        private ResponseDes(){}

        public static class OK {
            private OK(){}

            public static final String CREATE_QUEUE = "The queue has been successfully created!";
            public static final String APPEND_VALUE = "The value has been successfully appended!";
            public static final String READ_VALUE = "The value has been successfully read:";
        }

        public static class KO {
            private KO(){}

            public static final String CREATE_QUEUE_QUEUE_ID_ALREADY_PRESENT_KO = "The queue has been impossible to create because the queueId it's already used";
            public static final String APPEND_VALUE_QUEUE_ID_NOT_EXISTS_KO = "The value has been impossible to append because the queueId doesn't exists";
            public static final String READ_VALUE_QUEUE_ID_NOT_EXISTS_KO = "The values has been impossible to read because the queueId doesn't exists";
            public static final String READ_VALUE_QUEUE_ID_INDEX_OUT_OF_BOUND_KO = "The values has been impossible to read because of index out of bound";
        }
    }
}
