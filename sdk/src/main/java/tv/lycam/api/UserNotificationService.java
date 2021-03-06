package tv.lycam.api;

import tv.lycam.api.pojo.ParticipantRequest;
import tv.lycam.exception.RoomException;

/**
 * Created by chengbin on 2017/6/9.
 */
public interface UserNotificationService {

    /**
     * Responds back to the remote peer with the result of the invoked method.
     *
     * @param participantRequest
     *          instance of {@link ParticipantRequest} POJO
     * @param result
     *          Object containing information that depends on the invoked method. It'd normally be a
     *          JSON element-type object.
     */
    void sendResponse(ParticipantRequest participantRequest, Object result);

    /**
     * Responds back to the remote peer with the details of why the invoked method failed to be
     * processed correctly.
     *
     * @param participantRequest
     *          instance of {@link ParticipantRequest} POJO
     * @param data
     *          optional (nullable) Object containing additional information on the error. Can be a
     *          String or a JSON element-type object.
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message
     */
    void sendErrorResponse(ParticipantRequest participantRequest, Object data, RoomException error);

    /**
     * Sends a notification to a remote peer. This falls outside the normal exchange of messages
     * (client requests - server answers) so there's no need for a request identifier.
     *
     * @param participantId
     *          identifier of the targeted participant
     * @param method
     *          String with the name of the method or event to be invoked on the client
     * @param params
     *          Object containing information that depends on the invoked method. It'd normally be a
     *          JSON element-type object.
     */
    void sendNotification(String participantId, String method, Object params);

    /**
     * Notifies that any information associated with the provided request should be cleaned up (the
     * participant has left).
     *
     * @param participantRequest
     *          instance of {@link ParticipantRequest} POJO
     */
    void closeSession(ParticipantRequest participantRequest);

}
