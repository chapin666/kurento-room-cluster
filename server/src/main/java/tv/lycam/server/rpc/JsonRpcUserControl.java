package tv.lycam.server.rpc;


import com.google.gson.JsonObject;
import org.kurento.jsonrpc.Session;
import org.kurento.jsonrpc.Transaction;
import org.kurento.jsonrpc.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tv.lycam.sdk.NotificationRoomManager;
import tv.lycam.sdk.api.pojo.ParticipantRequest;
import tv.lycam.sdk.api.pojo.UserParticipant;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.internal.ProtocolElements;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


/**
 * Created by chengbin on 2017/6/5.
 */
public class JsonRpcUserControl {

    private static final Logger log = LoggerFactory.getLogger(JsonRpcUserControl.class);

    protected NotificationRoomManager roomManager;

    /**
     *
     * @param roomManager
     */
    @Autowired
    public JsonRpcUserControl(NotificationRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    /**
     *
     * join room
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void joinRoom(Transaction transaction, Request<JsonObject> request,
                         ParticipantRequest participantRequest)
            throws IOException, InterruptedException, ExecutionException {
        String roomName = getStringParam(request, ProtocolElements.JOINROOM_ROOM_PARAM);
        String userName = getStringParam(request, ProtocolElements.JOINROOM_USER_PARAM);

        boolean dataChannels = false;

        if (request.getParams().has(ProtocolElements.JOINROOM_DATACHANNELS_PARAM)) {
            dataChannels = getBooleanParam(request, ProtocolElements.JOINROOM_DATACHANNELS_PARAM);
        }

        ParticipantSession participantSession = getParticipantSession(transaction);
        participantSession.setParticipantName(userName);
        participantSession.setRoomName(roomName);
        participantSession.setDataChannels(dataChannels);

        roomManager.joinRoom(userName, roomName, dataChannels, true, participantRequest);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void publishVideo(Transaction transaction, Request<JsonObject> request,
                             ParticipantRequest participantRequest) {
        String sdpOffer = getStringParam(request, ProtocolElements.PUBLISHVIDEO_SDPOFFER_PARAM);
        boolean doLoopback = getBooleanParam(request, ProtocolElements.PUBLISHVIDEO_DOLOOPBACK_PARAM);

        roomManager.publishMedia(participantRequest, sdpOffer, doLoopback);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void unpublishVideo(Transaction transaction, Request<JsonObject> request,
                               ParticipantRequest participantRequest) {
        roomManager.unpublishMedia(participantRequest);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void receiveFrom(final Transaction transaction, Request<JsonObject> request,
                            ParticipantRequest participantRequest) {
        String senderName = getStringParam(request, ProtocolElements.RECEIVEVIDEO_SENDER_PARAM);

        // TODO
        senderName = senderName.substring(0, senderName.indexOf("_"));


        String sdpOffer = getStringParam(request, ProtocolElements.RECEIVEVIDEO_SDPOFFER_PARAM);

        roomManager.subscribe(senderName, sdpOffer, participantRequest);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void unsubscribeFromVideo(Transaction transaction, Request<JsonObject> request,
                                     ParticipantRequest participantRequest) {
        String senderName = getStringParam(request, ProtocolElements.UNSUBSCRIBEFROMVIDEO_SENDER_PARAM);
        // TODO
        senderName = senderName.substring(0, senderName.indexOf("_"));

        roomManager.unsubscribe(senderName, participantRequest);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void leaveRoom(Transaction transaction, Request<JsonObject> request,
                          ParticipantRequest participantRequest) {

        boolean isExists = false;
        String pid = participantRequest.getParticipantId();

        // trying with room info from session
        String roomName = null;
        if (transaction != null) {
            roomName = getParticipantSession(transaction).getRoomName();
        }

        if (roomName == null) {
            log.warn("No room information found for contact with session Id {}. "
                + "Using the admin method to evict the user. ", pid);
            leaveRoomAfterConnClosed(pid);
        } else {
            for (UserParticipant participant : roomManager.getParticipants(roomName)) {
                if (participant.getParticipantId().equals(participantRequest.getParticipantId())) {
                    isExists = true;
                    break;
                }
            }

            if (isExists) {
                log.debug("Participant with sessionId {} is leaving room {}", pid, roomName);
                roomManager.leaveRoom(participantRequest);
                log.info("Participant with sessionId {} has left room {}", pid, roomName);
            } else {
                log.warn("Participant with session id {} not found oin room {}."
                    + "Using the admin method to evict the user.", pid, roomName);
                leaveRoomAfterConnClosed(pid);
            }
        }

    }


    /**
     *
     * @param sessionId
     */
    public void leaveRoomAfterConnClosed(String sessionId) {

        try {
            roomManager.evictParticipant(sessionId);
            log.info("Evict contact with sessionId {}", sessionId);
        } catch (RoomException e) {
            log.warn("Unable to evict : {}", e.getMessage());
            log.trace("unable to evict user", e);
        }
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void onIceCandidate(Transaction transaction, Request<JsonObject> request,
                               ParticipantRequest participantRequest) {
        String endpointName = getStringParam(request, ProtocolElements.ONICECANDIDATE_EPNAME_PARAM);
        String candidate = getStringParam(request, ProtocolElements.ONICECANDIDATE_CANDIDATE_PARAM);
        String sdpMid = getStringParam(request, ProtocolElements.ONICECANDIDATE_SDPMIDPARAM);
        int sdpMLineIndex = getIntParam(request, ProtocolElements.ONICECANDIDATE_SDPMLINEINDEX_PARAM);

        roomManager.onIceCandidate(endpointName, candidate, sdpMLineIndex, sdpMid, participantRequest);
    }

    /**
     *
     * @param transaction
     * @param request
     * @param participant
     */
    public void sendMessage(Transaction transaction, Request<JsonObject> request,
                            ParticipantRequest participant) {
        String username = getStringParam(request, ProtocolElements.SENDMESSAGE_USER_PARAM);
        String roomName = getStringParam(request, ProtocolElements.SENDMESSAGE_ROOM_PARAM);
        String message = getStringParam(request, ProtocolElements.SENDMESSAGE_MESSAGE_PARAM);

        log.debug("Message from {} in room {} : {}", username, roomName, message);

        roomManager.sendMessage(message, username, roomName, participant);
    }


    /**
     *
     * @param transaction
     * @param request
     * @param participantRequest
     */
    public void customRequest(Transaction transaction, Request<JsonObject> request,
                              ParticipantRequest participantRequest) {
        throw new RuntimeException("Unsupported method");
    }




    /**
     *
     * @param transaction
     * @return
     */
    public ParticipantSession getParticipantSession(Transaction transaction) {
        Session session = transaction.getSession();
        ParticipantSession participantSession = (ParticipantSession) session.getAttributes().get(ParticipantSession.SESSION_KEY);
        if (participantSession == null) {
            participantSession = new ParticipantSession();
            session.getAttributes().put(participantSession.SESSION_KEY, participantSession);
        }
        return participantSession;
    }




    /******************** utils ***************/


    public static String getStringParam(Request<JsonObject> request, String key) {
        if (request.getParams() == null || request.getParams().get(key) == null) {
            throw new RuntimeException("Request element '" + key + "' is missing");
        }
        return request.getParams().get(key).getAsString();
    }

    public static int getIntParam(Request<JsonObject> request, String key) {
        if (request.getParams() == null || request.getParams().get(key) == null) {
            throw new RuntimeException("Request element '" + key + "' is missing");
        }
        return request.getParams().get(key).getAsInt();
    }

    public static boolean getBooleanParam(Request<JsonObject> request, String key) {
        if (request.getParams() == null || request.getParams().get(key) == null) {
            throw new RuntimeException("Request element '" + key + "' is missing");
        }
        return request.getParams().get(key).getAsBoolean();
    }

}
