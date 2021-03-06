package com.elex.chatservice.model.kurento.room;

import com.elex.chatservice.model.kurento.jsonrpc.JsonRpcResponseError;

/**
 * Room error class
 */
public class RoomError {

    /* Copied from https://github.com/Kurento/kurento-room/blob/master/kurento-room-sdk/src/main/java/org/kurento/room/exception/RoomException.java
       to avoid dependency to server for error checking
    */
    public static enum Code {
        GENERIC_ERROR_CODE(999),

        TRANSPORT_ERROR_CODE(803), TRANSPORT_RESPONSE_ERROR_CODE(802), TRANSPORT_REQUEST_ERROR_CODE(801),

        MEDIA_MUTE_ERROR_CODE(307), MEDIA_NOT_A_WEB_ENDPOINT_ERROR_CODE(306), MEDIA_RTP_ENDPOINT_ERROR_CODE(
                305), MEDIA_WEBRTC_ENDPOINT_ERROR_CODE(304), MEDIA_ENDPOINT_ERROR_CODE(303), MEDIA_SDP_ERROR_CODE(
                302), MEDIA_GENERIC_ERROR_CODE(301),

        ROOM_CANNOT_BE_CREATED_ERROR_CODE(204), ROOM_CLOSED_ERROR_CODE(203), ROOM_NOT_FOUND_ERROR_CODE(
                202), ROOM_GENERIC_ERROR_CODE(201),

        USER_NOT_STREAMING_ERROR_CODE(105), EXISTING_USER_IN_ROOM_ERROR_CODE(104), USER_CLOSED_ERROR_CODE(
                103), USER_NOT_FOUND_ERROR_CODE(102), USER_GENERIC_ERROR_CODE(101);
        private int value;

        Code(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private int code = 0;
    private String data = null;

    public RoomError(JsonRpcResponseError error){
        super();
        if(error!=null) {
            this.code = error.getCode();
            if(error.getData()!=null) {
                this.data = error.getData().toString();
            }
        }
    }

    public int getCode() {
        return code;
    }

    public String getData() {
        return data;
    }

    public String toString(){
        return "RoomError: "+code+" - "+data;
    }
}
