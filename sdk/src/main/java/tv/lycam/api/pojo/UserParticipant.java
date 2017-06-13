package tv.lycam.api.pojo;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by chengbin on 2017/6/7.
 */
public class UserParticipant implements DataSerializable {

    private String participantId;
    private String userName;
    private boolean streaming = false;
    private String roomName;

    public UserParticipant() {}

    public UserParticipant(String participantId, String userName) {
        super();
        this.participantId = participantId;
        this.userName = userName;
    }

    public UserParticipant(String participantId, String userName, boolean streaming) {
        super();
        this.participantId = participantId;
        this.userName = userName;
        this.streaming = streaming;
    }

    public UserParticipant(String roomName, String participantId, String userName) {
        super();
        this.roomName = roomName;
        this.participantId = participantId;
        this.userName = userName;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (participantId != null) {
            builder.append("participantId=").append(participantId).append(", ");
        }
        if (userName != null) {
            builder.append("userName=").append(userName).append(", ");
        }
        builder.append("streaming=").append(streaming).append("]");
        return builder.toString();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(participantId);
        out.writeUTF(userName);
        out.writeUTF(roomName);
        out.writeBoolean(isStreaming());
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.participantId = in.readUTF();
        this.userName = in.readUTF();
        this.roomName = in.readUTF();
        this.streaming = in.readBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserParticipant that = (UserParticipant) o;

        if (streaming != that.streaming) return false;
        if (participantId != null ? !participantId.equals(that.participantId) : that.participantId != null)
            return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        return roomName != null ? roomName.equals(that.roomName) : that.roomName == null;
    }

    @Override
    public int hashCode() {
        int result = participantId != null ? participantId.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (streaming ? 1 : 0);
        result = 31 * result + (roomName != null ? roomName.hashCode() : 0);
        return result;
    }
}
