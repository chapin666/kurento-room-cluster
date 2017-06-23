package tv.lycam.sdk.internal;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.kurento.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.lycam.sdk.HazelcastConfiguration;
import tv.lycam.sdk.api.RoomHandler;
import tv.lycam.sdk.api.pojo.Room;
import tv.lycam.sdk.api.pojo.UserParticipant;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.exception.RoomException.Code;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by chengbin on 2017/6/7.
 */
public class RoomConnection {

    public static final int ASYNC_LATCH_TIMEOUT = 30;

    private final static Logger log = LoggerFactory.getLogger(RoomConnection.class);


    private HazelcastInstance hazelcastInstance;
    private ConcurrentMap<String, UserParticipant> participantDB;

    // participants set
    // TODO
    private final ConcurrentMap<String, Participant> participants =
            new ConcurrentHashMap<String, Participant>();


    private Room room;

    private MediaPipeline pipeline;
    private CountDownLatch pipelineLatch = new CountDownLatch(1);

    private KurentoClient kurentoClient;

    private RoomHandler roomHandler;

    private volatile boolean closed = false;

    private AtomicInteger activePublishers = new AtomicInteger(0);

    private Object pipelineCreateLock = new Object();
    private Object pipelineReleaseLock = new Object();
    private volatile boolean pipelineReleased = false;
    private boolean destroyKurentoClient;

    private final ConcurrentHashMap<String, String> filterStates = new ConcurrentHashMap<>();


    public RoomConnection() {}


    public RoomConnection(Room room, KurentoClient kurentoClient, RoomHandler roomHandler,
                          boolean destroyKurentoClient) {
        this.room = room;
        this.kurentoClient = kurentoClient;
        this.destroyKurentoClient = destroyKurentoClient;
        this.roomHandler = roomHandler;
        log.debug("New ROOM instance, named '{}'", room.getRoomName());

        this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(HazelcastConfiguration.config());
        this.participantDB = hazelcastInstance.getMap("participants");
    }

    public String getName() {
        return this.room.getRoomName();
    }

    public MediaPipeline getPipeline() {
        try {
            pipelineLatch.await(RoomConnection.ASYNC_LATCH_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this.pipeline;
    }


    public synchronized void join(UserParticipant userParticipant, boolean dataChannels,
                                  boolean webParticipant) throws RoomException {

        checkClosed();

        if (userParticipant.getUserName() == null || userParticipant.getUserName().isEmpty()) {
            throw new RoomException(Code.GENERIC_ERROR_CODE, "Empty user name is not allowed");
        }

        for (UserParticipant p : participantDB.values()) {
            if (p.getUserName().equals(userParticipant.getUserName())) {
                throw new RoomException(Code.EXISTING_USER_IN_ROOM_ERROR_CODE,
                        "User '" + userParticipant.getUserName() + "' already exists in room '" + this.room.getRoomName() + "'");
            }
        }

        createPipeline();

        // TODO
        this.participantDB.put(userParticipant.getUserName(), userParticipant);
        //


        Participant participant =
                new Participant(userParticipant, this, getPipeline(), dataChannels, webParticipant);
        participants.put(userParticipant.getParticipantId(), participant);

        filterStates.forEach((filterId, state) -> {
            log.info("Adding filter {}", filterId);
            roomHandler.updateFilter(this.room.getRoomName(), participant, filterId, state);
        });

        log.info("ROOM {}: Added contact {}", this.room.getRoomName(), userParticipant.getUserName());
    }

    public void newPublisher(Participant participant) {
        registerPublisher();

        // pre-load endpoints to recv video from the new publisher
        for (UserParticipant participant1 : participantDB.values()) {

            Participant participant2 = getParticipantByName(participant1.getUserName());
            if (participant2 == null) {
                log.warn(participant1.getUserName() + " User not exists");
                continue;
            }

            if (participant.equals(participant2)) {
                continue;
            }

            //
            participant2.getNewOrExistingSubscriber(participant.getName());
            //
        }

        log.debug("ROOM {}: Virtually subscribed other participants {} to new publisher {}", this.room.getRoomName(),
                participants.values(), participant.getName());
    }

    public void cancelPublisher(Participant participant) {
        deregisterPublisher();

        // cancel recv video from this publisher
        for (UserParticipant s : participantDB.values()) {
            if (participant.equals(s)) {
                continue;
            }
            Participant subscriber = getParticipant(s.getParticipantId());
            if (subscriber != null) {
                subscriber.cancelReceivingMedia(participant.getName());
            } else {
                throw new RoomException(Code.USER_NOT_FOUND_ERROR_CODE,
                        "User #" + subscriber.getId() + " not found in room '" + this.room.getRoomName() + "'");
            }
        }

        log.debug("ROOM {}: Unsubscribed other participants {} from the publisher {}", this.room.getRoomName(),
                participants.values(), participant.getName());

    }

    public void leave(String participantId) throws RoomException {

        checkClosed();

        Participant participant = participants.get(participantId);
        if (participant == null) {
            throw new RoomException(Code.USER_NOT_FOUND_ERROR_CODE,
                    "User #" + participantId + " not found in room '" + this.room.getRoomName() + "'");
        }
        participant.releaseAllFilters();

        log.info("PARTICIPANT {}: Leaving room {}", participant.getName(), this.room.getRoomName());
        if (participant.isStreaming()) {
            this.deregisterPublisher();
        }

        participant.close();

        this.removeParticipant(participant);


    }

    public Collection<Participant> getParticipants() {

        checkClosed();

        return participants.values();
    }

    public Set<String> getParticipantIds() {

        checkClosed();

        return participants.keySet();
    }

    public Participant getParticipant(String participantId) {

        checkClosed();

        return participants.get(participantId);
    }

    public Participant getParticipantByName(String userName) {

        checkClosed();

        for (Participant p : participants.values()) {
            if (p.getName().equals(userName)) {
                return p;
            }
        }

        return null;
    }

    public void close() {
        if (!closed) {

            for (Participant user : participants.values()) {
                user.close();
            }

            participants.clear();

            closePipeline();

            log.debug("RoomConnection {} closed", this.room.getRoomName());

            if (destroyKurentoClient) {
                kurentoClient.destroy();
            }

            this.closed = true;
        } else {
            log.warn("Closing an already closed room '{}'", this.room.getRoomName());
        }
    }



    public void sendIceCandidate(String participantId, String endpointName, IceCandidate candidate) {
        this.roomHandler.onIceCandidate(this.room.getRoomName(), participantId, endpointName, candidate);
    }



    public void sendMediaError(String participantId, String description) {
        this.roomHandler.onMediaElementError(this.room.getRoomName(), participantId, description);
    }

    public boolean isClosed() {
        return closed;
    }

    private void checkClosed() {
        if (closed) {
            throw new RoomException(Code.ROOM_CLOSED_ERROR_CODE, "The room '" + this.room.getRoomName() + "' is closed");
        }
    }

    private void removeParticipant(Participant participant) {

        checkClosed();

        participants.remove(participant.getId());

        // TODO
        participantDB.remove(participant.getName());
        //

        log.debug("ROOM {}: Cancel receiving media from user '{}' for other users", this.room.getRoomName(),
                participant.getName());
        for (Participant other : participants.values()) {
            other.cancelReceivingMedia(participant.getName());
        }
    }

    public int getActivePublishers() {
        return activePublishers.get();
    }

    public void registerPublisher() {
        this.activePublishers.incrementAndGet();
    }

    public void deregisterPublisher() {
        this.activePublishers.decrementAndGet();
    }

    private void createPipeline() {
        synchronized (pipelineCreateLock) {
            if (pipeline != null) {
                return;
            }
            log.info("ROOM {}: Creating MediaPipeline", this.room.getRoomName());
            try {
                kurentoClient.createMediaPipeline(new Continuation<MediaPipeline>() {
                    @Override
                    public void onSuccess(MediaPipeline result) throws Exception {
                        pipeline = result;
                        pipelineLatch.countDown();
                        log.debug("ROOM {}: Created MediaPipeline", room.getRoomName());
                    }

                    @Override
                    public void onError(Throwable cause) throws Exception {
                        pipelineLatch.countDown();
                        log.error("ROOM {}: Failed to create MediaPipeline", room.getRoomName(), cause);
                    }
                });
            } catch (Exception e) {
                log.error("Unable to create media pipeline for room '{}'", this.room.getRoomName(), e);
                pipelineLatch.countDown();
            }
            if (getPipeline() == null) {
                throw new RoomException(Code.ROOM_CANNOT_BE_CREATED_ERROR_CODE,
                        "Unable to create media pipeline for room '" + this.room.getRoomName() + "'");
            }

            pipeline.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent event) {
                    String desc =
                            event.getType() + ": " + event.getDescription() + "(errCode=" + event.getErrorCode()
                                    + ")";
                    log.warn("ROOM {}: Pipeline error encountered: {}", room.getRoomName(), desc);
                    roomHandler.onPipelineError(room.getRoomName(), getParticipantIds(), desc);
                }
            });
        }
    }

    private void closePipeline() {
        synchronized (pipelineReleaseLock) {
            if (pipeline == null || pipelineReleased) {
                return;
            }
            getPipeline().release(new Continuation<Void>() {

                @Override
                public void onSuccess(Void result) throws Exception {
                    log.debug("ROOM {}: Released Pipeline", room.getRoomName());
                    pipelineReleased = true;
                }

                @Override
                public void onError(Throwable cause) throws Exception {
                    log.warn("ROOM {}: Could not successfully release Pipeline", room.getRoomName(), cause);
                    pipelineReleased = true;
                }
            });
        }
    }

    public synchronized void updateFilter(String filterId) {
        String state = filterStates.get(filterId);
        String newState = roomHandler.getNextFilterState(filterId, state);

        filterStates.put(filterId, newState);

        for (Participant participant : participants.values()) {
            roomHandler.updateFilter(getName(), participant, filterId, newState);
        }
    }


    @Override
    public String toString() {
        return "RoomConnection{" +
                "name='" + this.room.getRoomName() + '\'' +
                ", pipeline=" + pipeline +
                ", pipelineLatch=" + pipelineLatch +
                ", roomHandler=" + roomHandler +
                '}';
    }
}
