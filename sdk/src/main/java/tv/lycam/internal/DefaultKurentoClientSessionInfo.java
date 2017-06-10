package tv.lycam.internal;

import tv.lycam.api.KurentoClientSessionInfo;

/**
 * Created by chengbin on 2017/6/9.
 */
public class DefaultKurentoClientSessionInfo implements KurentoClientSessionInfo {

    private String participantId;
    private String roomName;

    public DefaultKurentoClientSessionInfo(String participantId, String roomName) {
        super();
        this.participantId = participantId;
        this.roomName = roomName;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    @Override
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

}
