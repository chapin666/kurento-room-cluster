package tv.lycam.rpc;

/**
 * Created by chengbin on 2017/6/6.
 *
 */
public class ParticipantSession {

    public static final String SESSION_KEY = "participant";

    private String participantName;
    private String roomName;
    private boolean dataChannels = false;

    public ParticipantSession() {

    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean useDataChannels() {
        return dataChannels;
    }

    public void setDataChannels(boolean dataChannels) {
        this.dataChannels = dataChannels;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (participantName != null) {
            builder.append("participantName=").append(participantName).append(",");
        }
        if (roomName != null) {
            builder.append("roomName=").append(roomName).append(",");
        }
        builder.append("useDataChannels=").append(dataChannels);
        builder.append("]");

        return builder.toString();
    }
}
