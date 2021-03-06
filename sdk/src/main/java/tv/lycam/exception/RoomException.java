package tv.lycam.exception;

/**
 * Created by chengbin on 2017/6/7.
 */
public class RoomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static enum Code {

        GENERIC_ERROR_CODE(999),
        TRANSPORT_ERROR_CODE(803),
        TRANSPORT_RESPONSE_ERROR_CODE(802),
        TRANSPORT_REQUEST_ERROR_CODE(801),
        MEDIA_MUTE_ERROR_CODE(307),
        MEDIA_NOT_A_WEB_ENDPOINT_ERROR_CODE(306),
        MEDIA_RTP_ENDPOINT_ERROR_CODE(305),
        MEDIA_WEBRTC_ENDPOINT_ERROR_CODE(304),
        MEDIA_ENDPOINT_ERROR_CODE(303),
        MEDIA_SDP_ERROR_CODE(302),
        MEDIA_GENERIC_ERROR_CODE(301),
        ROOM_CANNOT_BE_CREATED_ERROR_CODE(204),
        ROOM_CLOSED_ERROR_CODE(203),
        ROOM_NOT_FOUND_ERROR_CODE(202),
        ROOM_GENERIC_ERROR_CODE(201),
        USER_NOT_STREAMING_ERROR_CODE(105),
        EXISTING_USER_IN_ROOM_ERROR_CODE(104),
        USER_CLOSED_ERROR_CODE(103),
        USER_NOT_FOUND_ERROR_CODE(102),
        USER_GENERIC_ERROR_CODE(101);

        private int value;

        private Code(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }


    private Code code = Code.GENERIC_ERROR_CODE;

    public RoomException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public int getCodeValue() {
        return code.getValue();
    }

    @Override
    public String toString() {
        return "Code: " + getCodeValue() + " " + super.toString();
    }
}
