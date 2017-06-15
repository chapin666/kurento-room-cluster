package tv.lycam.server.kms;

import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.internal.DefaultKurentoClientSessionInfo;
import tv.lycam.server.rpc.JsonRpcNotificationService;
import tv.lycam.server.rpc.ParticipantSession;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chengbin on 2017/6/6.
 */
public class FixedNKmsManager extends KmsManager {

    private static final Logger log = LoggerFactory.getLogger(FixedNKmsManager.class);

    private String authRegex;
    private static Pattern authPattern = null;


    @Autowired
    private JsonRpcNotificationService notificationService;

    /**
     *
     * @param kmsWsUri
     */
    public FixedNKmsManager(List<String> kmsWsUri) {
        for (String uri : kmsWsUri) {
            this.addKms(new Kms(KurentoClient.create(uri), uri));
        }
    }

    /**
     *
     * @param kmsWsUri
     * @param kmsLoadLimit
     */
    public FixedNKmsManager(List<String> kmsWsUri, int kmsLoadLimit) {
        for (String uri : kmsWsUri) {
            Kms kms = new Kms(KurentoClient.create(uri), uri);
            kms.setLoadManager(new MaxWebRtcLoadManager(kmsLoadLimit));
            this.addKms(kms);
        }
    }


    /**
     * REGEXP
     *
     * @param regex
     */
    public synchronized void setAuthRegex(String regex) {
        this.authRegex = regex != null ? regex.trim() : null;
        if (authRegex != null && !authRegex.isEmpty()) {
            authPattern = Pattern.compile(authRegex, authPattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        }
    }


    @Override
    public synchronized Kms getKms(DefaultKurentoClientSessionInfo sessionInfo) {
        String userName = null;
        String participantId = sessionInfo.getParticipantId();
        Session session = notificationService.getSession(participantId);
        if (session != null) {
            Object sessionValue = session.getAttributes().get(ParticipantSession.SESSION_KEY);
            if (sessionValue != null) {
                ParticipantSession participantSession = (ParticipantSession) sessionValue;
                userName = participantSession.getParticipantName();
            }
        }

        if (userName == null) {
            log.warn("Unable to find user name in session {}", participantId);
            throw new RoomException(RoomException.Code.ROOM_CANNOT_BE_CREATED_ERROR_CODE,
                    "Not enough information");
        }
        if (!canCreateRoom(userName)) {
            throw new RoomException(RoomException.Code.ROOM_CANNOT_BE_CREATED_ERROR_CODE,
                    "User cannot create a new room");
        }
        Kms kms = null;
        String type = "";

        boolean hq = isUserHQ(userName);
        if (hq) {
            kms = getLessLoadedKms();
        } else {
            kms = getNextLessLoadKms();
            if (!kms.allowMoreElements()) {
                kms = getLessLoadedKms();
            } else {
                type = "next";
            }
        }

        if (!kms.allowMoreElements()) {
            log.debug("Was trying kms which has no resource left : highQ={}, {} less loaded KMS, uri={}",
                    hq, type, kms.getUri());
            throw new RoomException(RoomException.Code.ROOM_CANNOT_BE_CREATED_ERROR_CODE,
                    "No resource left to create new room");
        }

        log.debug("Offering Kms: high={}, {}less loaded KMS, uri={}", hq, type, kms.getUri());

        return kms;
    }

    /**
     *
     * @param userName
     * @return
     */
    private boolean isUserHQ(String userName) {
        return userName.toLowerCase().startsWith("special");
    }


    /**
     *
     * @param userName
     * @return
     */
    private boolean canCreateRoom(String userName) {
        if (authPattern == null) {
            return true;
        }

        Matcher m = authPattern.matcher(userName);
        return m.matches();
    }

}
