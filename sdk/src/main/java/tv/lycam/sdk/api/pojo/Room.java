package tv.lycam.sdk.api.pojo;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import tv.lycam.sdk.HazelcastConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lycamandroid on 2017/6/13.
 */
public class Room implements DataSerializable {

    private String roomName;

    public Room() {}

    public Room(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }



    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(roomName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.roomName = in.readUTF();
    }


    public Collection<UserParticipant> getParticipants() {
        ConcurrentMap<String, UserParticipant> participants =
                Hazelcast.getOrCreateHazelcastInstance(HazelcastConfiguration.config()).getMap("participants");
        Collection<UserParticipant> tmps = new ArrayList<>();
        for (UserParticipant entry: participants.values()) {
            if (entry.getRoomName().equals(this.roomName)) {
                tmps.add(entry);
            }
        }
        return tmps;
    }
}
