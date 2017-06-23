package tv.lycam.sdk.api;

import tv.lycam.sdk.api.pojo.ParticipantRequest;
import tv.lycam.sdk.api.pojo.UserParticipant;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.NotificationRoomManager;

import java.util.Set;

/**
 * Created by chengbin on 2017/6/9.
 */
public interface NotificationRoomHandler extends RoomHandler {

    /**
     * Called as a result of
     * {@link NotificationRoomManager#joinRoom(String, String, ParticipantRequest)} . The new
     * contact should be responded with all the available information: the existing peers and, for
     * any publishers, their stream names. The current peers should receive a notification of the join
     * event.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param roomName
     *          the room's name
     * @param newUserName
     *          the new user
     * @param existingParticipants
     *          instances of {@link UserParticipant} POJO representing the already existing peers
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the join was unsuccessful and the user should be responded accordingly.
     */
    void onParticipantJoined(ParticipantRequest request, String roomName, String newUserName,
                             Set<UserParticipant> existingParticipants, RoomException error);

    /**
     * Called as a result of
     * {@link NotificationRoomManager#leaveRoom(String, String, ParticipantRequest)} . The user should
     * receive an acknowledgement if the operation completed successfully, and the remaining peers
     * should be notified of this event.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param userName
     *          the departing user's name
     * @param remainingParticipants
     *          instances of {@link UserParticipant} representing the remaining participants in the
     *          room
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onParticipantLeft(ParticipantRequest request, String userName,
                           Set<UserParticipant> remainingParticipants, RoomException error);

    /**
     * Called as a result of {@link NotificationRoomManager#evictParticipant(String)}
     * (application-originated action). The remaining peers should be notified of this event.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param userName
     *          the departing user's name
     * @param remainingParticipants
     *          instances of {@link UserParticipant} representing the remaining participants in the
     *          room
     */
    void onParticipantLeft(String userName, Set<UserParticipant> remainingParticipants);

    /**
     * Called as a result of
     * {@link NotificationRoomManager#publishMedia(String, ParticipantRequest, MediaElement...)} . The
     * user should receive the generated SPD answer from the local WebRTC endpoint, and the other
     * peers should be notified of this event.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param publisherName
     *          the user name
     * @param sdpAnswer
     *          String with generated SPD answer from the local WebRTC endpoint
     * @param participants
     *          instances of {@link UserParticipant} for ALL the participants in the room (includes
     *          the publisher)
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onPublishMedia(ParticipantRequest request, String publisherName, String sdpAnswer,
                        Set<UserParticipant> participants, RoomException error);

    /**
     * Called as a result of {@link NotificationRoomManager#unpublishMedia(ParticipantRequest)}. The
     * user should receive an acknowledgement if the operation completed successfully, and all other
     * peers in the room should be notified of this event.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param publisherName
     *          the user name
     * @param participants
     *          instances of {@link UserParticipant} for ALL the participants in the room (includes
     *          the publisher)
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onUnpublishMedia(ParticipantRequest request, String publisherName,
                          Set<UserParticipant> participants, RoomException error);

    /**
     * Called as a result of
     * {@link NotificationRoomManager#subscribe(String, String, ParticipantRequest)} . The user should
     * be responded with generated SPD answer from the local WebRTC endpoint.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param sdpAnswer
     *          String with generated SPD answer from the local WebRTC endpoint
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onSubscribe(ParticipantRequest request, String sdpAnswer, RoomException error);

    /**
     * Called as a result of {@link NotificationRoomManager#unsubscribe(String, ParticipantRequest)}.
     * The user should receive an acknowledgement if the operation completed successfully (no error).
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onUnsubscribe(ParticipantRequest request, RoomException error);

    /**
     * Called as a result of
     * {@link NotificationRoomManager#sendMessage(String, String, String, ParticipantRequest)} . The
     * user should receive an acknowledgement if the operation completed successfully, and all the
     * peers in the room should be notified with the message contents and its origin.
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param message
     *          String with the message body
     * @param userName
     *          name of the peer that sent it
     * @param roomName
     *          the current room name
     * @param participants
     *          instances of {@link UserParticipant} for ALL the participants in the room (includes
     *          the sender)
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onSendMessage(ParticipantRequest request, String message, String userName, String roomName,
                       Set<UserParticipant> participants, RoomException error);

    /**
     * Called as a result of
     * {@link NotificationRoomManager#onIceCandidate(String, String, int, String, ParticipantRequest)}
     * . The user should receive an acknowledgement if the operation completed successfully (no
     * error).
     *
     * @param request
     *          instance of {@link ParticipantRequest} POJO to identify the user and the request
     * @param error
     *          instance of {@link RoomException} POJO, includes a code and error message. If not
     *          null, then the operation was unsuccessful and the user should be responded
     *          accordingly.
     */
    void onRecvIceCandidate(ParticipantRequest request, RoomException error);

    /**
     * Called as a result of {@link NotificationRoomManager#closeRoom(String)} -
     * application-originated method, not as a consequence of a client request. All resources on the
     * sdk, associated with the room, have been released. The existing participants in the room
     * should be notified of this event so that the client-side application acts accordingly.
     *
     * @param roomName
     *          the room that's just been closed
     * @param participants
     *          instances of {@link UserParticipant} POJO representing the peers of the closed room
     */
    void onRoomClosed(String roomName, Set<UserParticipant> participants);

    /**
     * Called as a result of {@link NotificationRoomManager#evictParticipant(String)} -
     * application-originated method, not as a consequence of a client request. The contact should
     * be notified so that the client-side application would terminate gracefully.
     *
     * @param participant
     *          instance of {@link UserParticipant} POJO representing the evicted peer
     */
    void onParticipantEvicted(UserParticipant participant);

}
