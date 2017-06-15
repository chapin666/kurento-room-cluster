package tv.lycam.api.rest;

import static org.kurento.commons.PropertiesManager.getProperty;

import org.kurento.commons.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tv.lycam.api.exception.ResourceNotFoundException;
import tv.lycam.sdk.NotificationRoomManager;
import tv.lycam.sdk.exception.RoomException;

import java.util.Set;

/**
 * Created by chengbin on 2017/6/7.
 */
@RestController
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);


    // Speaker config
    private static final int UPDATE_SPEAKER_INTERVAL_DEFAULT = 1800;
    private static final int THRESHOLD_SPEAKER_DEFAULT = -50;

    // loopback config
    private final static boolean ROOM_LOOPBACK_REMOTE = PropertiesManager
                    .getProperty("room.loopback.remote", false);
    private final static boolean ROOM_LOOPBACK_AND_LOCAL = PropertiesManager
                    .getProperty("room.loopback.andLocal", false);

    // client config
    private static ClientConfig config;
    static {
        config = new ClientConfig();
        config.setLoopbackRemote(ROOM_LOOPBACK_REMOTE);
        config.setLookbackAndLocal(ROOM_LOOPBACK_AND_LOCAL);
        log.info("Set client config: {}", config);
    }

    @Autowired
    private NotificationRoomManager roomManager;


    /**
     *
     * @return
     */
    @RequestMapping("/getAllRooms")
    public Set<String> getAllRooms() {
        return roomManager.getRooms();
    }


    /**
     *
     * @return
     */
    @RequestMapping("/getUpdateSpeakerInterval")
    public Integer getUpdateSpeakerInterval() {
        return Integer.valueOf(getProperty("updateSpeakerInterval", UPDATE_SPEAKER_INTERVAL_DEFAULT));
    }


    /**
     *
     * @return
     */
    @RequestMapping("/getThresholdSpeaker")
    public Integer getThresholdSpeaker() {
        return Integer.valueOf(getProperty("thresholdSpeaker", THRESHOLD_SPEAKER_DEFAULT));
    }

    /**
     *
     * @param room
     */
    @RequestMapping("/close")
    public void closeRoom(@RequestParam("room") String room) {
        log.warn("Trying to close the room {}", room);

        if (!roomManager.getRooms().contains(room)) {
            log.warn("Unable to close room '{}', not found.", room);
            throw new ResourceNotFoundException("RoomConnection '" + room + "' not found");
        }

        try {
            roomManager.closeRoom(room);
        } catch (RoomException e) {
            log.warn("Error close room {}", room, e);
            throw new ResourceNotFoundException(e.getMessage());
        }
    }


    @RequestMapping("/getClientConfig")
    public ClientConfig clientConfig() {
        log.debug("Sending client config {}", config);
        return config;
    }

}
