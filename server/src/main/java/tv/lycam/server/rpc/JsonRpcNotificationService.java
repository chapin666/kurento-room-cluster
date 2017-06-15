package tv.lycam.server.rpc;

import com.google.gson.JsonObject;
import org.kurento.jsonrpc.Session;
import org.kurento.jsonrpc.Transaction;
import org.kurento.jsonrpc.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.lycam.sdk.api.UserNotificationService;
import tv.lycam.sdk.api.pojo.ParticipantRequest;
import tv.lycam.sdk.exception.RoomException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chengbin on 2017/6/5.
 */
public class JsonRpcNotificationService implements UserNotificationService {

    // Logger
    private  static final Logger log = LoggerFactory.getLogger(JsonRpcNotificationService.class);

    // sessions
    // TODO
    private static ConcurrentHashMap<String, SessionWrapper> sessions = new ConcurrentHashMap<>();

    @Override
    public void sendResponse(ParticipantRequest participantRequest, Object result) {
        Transaction t = getAndRemoveTransaction(participantRequest);
        if (t == null) {
            log.error("No transaction found for {], unable to send result {}", participantRequest, result);
            return;
        }
        try {
            t.sendResponse(result);
        } catch (Exception e) {
            log.error("Exception responding to user ({})", participantRequest, e);
        }
    }

    @Override
    public void sendErrorResponse(ParticipantRequest participantRequest, Object data, RoomException e) {
        Transaction t = getAndRemoveTransaction(participantRequest);
        if (t == null) {
            log.error("No transaction found for {}, unable to send result {}", participantRequest, data);
            return;
        }

        try {
            String dataVal = data != null ? data.toString() : null;
            t.sendError(e.getCodeValue(), e.getMessage(), dataVal);
        } catch (IOException e1) {
            log.error("Exception sending error response to user ({})", participantRequest, e);
        }
    }

    @Override
    public void sendNotification(String participantId, String method, Object params) {
        SessionWrapper sw = sessions.get(participantId);
        if (sw == null || sw.getSession() == null) {
            log.error("No session found for id {}, unable to send notification {}: {}",
                    participantId, method, params);
            return;
        }
        Session s = sw.getSession();

        try {
            s.sendNotification(method, params);
        } catch (IOException e) {
            log.error("Exception sending notification '{}' : {} to user id {}", method, params, participantId, e);
        }
    }

    @Override
    public void closeSession(ParticipantRequest participantRequest) {
        if (participantRequest == null) {
            log.error("No session found for null ParticipantRequest object, " + " unable to cleanup ");
            return;
        }

        String sessionId = participantRequest.getParticipantId();
        SessionWrapper sessionWrapper = sessions.get(sessionId);
        if (sessionWrapper == null || sessionWrapper.getSession() == null) {
            log.error("No session found for id {}, unable to cleanup", sessionId);
            return;
        }
        Session session = sessionWrapper.getSession();

        try {
            ParticipantSession ps = null;
            if (session.getAttributes().containsKey(ParticipantSession.SESSION_KEY)) {
                ps = (ParticipantSession) session.getAttributes().get(ParticipantSession.SESSION_KEY);
            }
            session.close();
            log.info("Closed session for req {} (userInfo:{})", participantRequest, ps);
        } catch (IOException e) {
            log.error("Error closing session for req {}", participantRequest, e);
        }
        sessions.remove(sessionId);
    }

    /**
     *
     * @param transaction
     * @param request
     * @return
     */
    public SessionWrapper addTransaction(Transaction transaction, Request<JsonObject> request) {
        String sessionId = transaction.getSession().getSessionId();
        SessionWrapper sessionWrapper =  sessions.get(sessionId);
        if (sessionWrapper == null) {
            sessionWrapper = new SessionWrapper(transaction.getSession());
            SessionWrapper oldSw = sessions.putIfAbsent(sessionId, sessionWrapper);
            if (oldSw != null) {
                log.warn("Concurrent initialization of sessoion wrapper #{}", sessionId);
                sessionWrapper = oldSw;
            }
        }
        sessionWrapper.addTransaction(request.getId(), transaction);
        return sessionWrapper;
    }


    /**
     *
     * @param sessionId
     * @return
     */
    public Session getSession(String sessionId) {
        SessionWrapper sw = sessions.get(sessionId);
        if (sw == null) {
            return null;
        }
        return sw.getSession();
    }

    /**
     *
     * @param participantRequest
     * @return
     */
    private Transaction getAndRemoveTransaction(ParticipantRequest participantRequest) {
        Integer tid = null;
        if (participantRequest == null) {
            log.warn("Unable to obtain a transaction for a null ParticipantRequest object");
            return null;
        }
        String tidVal = participantRequest.getRequestId();

        try {
            tid = Integer.parseInt(tidVal);
        } catch (NumberFormatException e) {
            log.error("Invalid transaction id, a number was expected but recv: {}", tidVal, e);
            return null;
        }

        String sessionId = participantRequest.getParticipantId();
        SessionWrapper sessionWrapper = sessions.get(sessionId);
        if (sessionWrapper == null) {
            log.warn("Invalid session id {}", sessionId);
            return null;
        }
        log.trace("#{} - {} transactions", sessionId, sessionWrapper.getTransactions().size());
        Transaction t = sessionWrapper.getTransaction(tid);
        sessionWrapper.removeTransaction(tid);
        return t;
    }

}
