package tv.lycam.sdk.internal;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import tv.lycam.sdk.api.KurentoClientSessionInfo;

import java.io.IOException;

/**
 * Created by chengbin on 2017/6/9.
 */
public class DefaultKurentoClientSessionInfo implements KurentoClientSessionInfo, DataSerializable {

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



    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(this.participantId);
        out.writeUTF(roomName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.participantId = in.readUTF();
        this.roomName = in.readUTF();
    }
}
