package tv.lycam.internal;

import org.kurento.client.*;
import org.kurento.client.internal.server.KurentoServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.lycam.api.MutedMediaType;
import tv.lycam.api.pojo.UserParticipant;
import tv.lycam.endpoint.PublisherEndpoint;
import tv.lycam.endpoint.SdpType;
import tv.lycam.endpoint.SubscriberEndpoint;
import tv.lycam.exception.RoomException;
import tv.lycam.exception.RoomException.Code;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by chengbin on 2017/6/7.
 */
public class Participant {

    private static final Logger log = LoggerFactory.getLogger(Participant.class);

    private UserParticipant user;
    private boolean web = false;
    private boolean dataChannels = false;

    private final RoomConnection room;

    private final MediaPipeline pipeline;

    private PublisherEndpoint publisher;
    private CountDownLatch endPointLatch = new CountDownLatch(1);

    private final ConcurrentMap<String, Filter> filters = new ConcurrentHashMap<>();

    // Subscriber
    // TODO
    private final ConcurrentMap<String, SubscriberEndpoint> subscribers =
            new ConcurrentHashMap<String, SubscriberEndpoint>();

    private volatile boolean streaming = false;
    private volatile boolean closed;

    public Participant(UserParticipant user, RoomConnection room, MediaPipeline pipeline,
                       boolean dataChannels, boolean web) {
        this.user = user;
        this.web = web;
        this.dataChannels = dataChannels;
        this.pipeline = pipeline;
        this.room = room;
        this.publisher = new PublisherEndpoint(web, dataChannels, this, user.getUserName(), pipeline);

        for (Participant other : room.getParticipants()) {
            if (!other.getName().equals(this.user.getUserName())) {
                getNewOrExistingSubscriber(other.getName());
            }
        }
    }

    public void createPublishingEndpoint() {
        publisher.createEndpoint(endPointLatch);
        if (getPublisher().getEndpoint() == null) {
            throw new RoomException(Code.MEDIA_ENDPOINT_ERROR_CODE,
                    "Unable to create publisher endpoint");
        }
    }

    public String getId() {
        return this.user.getParticipantId();
    }

    public String getName() {
        return this.user.getUserName();
    }

    public void shapePublisherMedia(MediaElement element, MediaType type) {
        if (type == null) {
            this.publisher.apply(element);
        } else {
            this.publisher.apply(element, type);
        }
    }

    public synchronized Filter getFilterElement(String id) {
        return filters.get(id);
    }

    public synchronized void addFilterElement(String id, Filter filter) {
        filters.put(id, filter);
        shapePublisherMedia(filter, null);
    }

    public synchronized void disableFilterelement(String filterID, boolean releaseElement) {
        Filter filter = getFilterElement(filterID);

        if (filter != null) {
            try {
                publisher.revert(filter, releaseElement);
            } catch (RoomException e) {
                //Ignore error
            }
        }
    }

    public synchronized void enableFilterelement(String filterID) {
        Filter filter = getFilterElement(filterID);

        if (filter != null) {
            try {
                publisher.apply(filter);
            } catch (RoomException e) {
                // Ignore exception if element is already used
            }
        }
    }

    public synchronized void removeFilterElement(String id) {
        Filter filter = getFilterElement(id);

        filters.remove(id);
        if (filter != null) {
            publisher.revert(filter);
        }
    }

    public synchronized void releaseAllFilters() {

        // Check this, mutable array?

        filters.forEach((s, filter) -> removeFilterElement(s));
    }

    public PublisherEndpoint getPublisher() {
        try {
            if (!endPointLatch.await(RoomConnection.ASYNC_LATCH_TIMEOUT, TimeUnit.SECONDS)) {
                throw new RoomException(
                        Code.MEDIA_ENDPOINT_ERROR_CODE,
                        "Timeout reached while waiting for publisher endpoint to be ready");
            }
        } catch (InterruptedException e) {
            throw new RoomException(
                    Code.MEDIA_ENDPOINT_ERROR_CODE,
                    "Interrupted while waiting for publisher endpoint to be ready: " + e.getMessage());
        }
        return this.publisher;
    }

    public RoomConnection getRoom() {
        return this.room;
    }

    public MediaPipeline getPipeline() {
        return pipeline;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public boolean isSubscribed() {
        for (SubscriberEndpoint se : subscribers.values()) {
            if (se.isConnectedToPublisher()) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getConnectedSubscribedEndpoints() {
        Set<String> subscribedToSet = new HashSet<String>();
        for (SubscriberEndpoint se : subscribers.values()) {
            if (se.isConnectedToPublisher()) {
                subscribedToSet.add(se.getEndpointName());
            }
        }
        return subscribedToSet;
    }

    public String preparePublishConnection() {
        log.info(
                "USER {}: Request to publish video in room {} by " + "initiating connection from server",
                this.user.getUserName(), this.room.getName());

        String sdpOffer = this.getPublisher().preparePublishConnection();

        log.trace("USER {}: Publishing SdpOffer is {}", this.user.getUserName(), sdpOffer);
        log.info("USER {}: Generated Sdp offer for publishing in room {}", this.user.getUserName(),
                this.room.getName());
        return sdpOffer;
    }

    public String publishToRoom(SdpType sdpType, String sdpString, boolean doLoopback,
                                MediaElement loopbackAlternativeSrc, MediaType loopbackConnectionType) {
        log.info("USER {}: Request to publish video in room {} (sdp type {})", this.user.getUserName(),
                this.room.getName(), sdpType);
        log.trace("USER {}: Publishing Sdp ({}) is {}", this.user.getUserName(), sdpType, sdpString);

        String sdpResponse = this.getPublisher()
                .publish(sdpType, sdpString, doLoopback, loopbackAlternativeSrc, loopbackConnectionType);
        this.streaming = true;

        log.trace("USER {}: Publishing Sdp ({}) is {}", this.user.getUserName(), sdpType, sdpResponse);
        log.info("USER {}: Is now publishing video in room {}", this.user.getUserName(), this.room.getName());

        return sdpResponse;
    }

    public void unpublishMedia() {
        log.debug("PARTICIPANT {}: unpublishing media stream from room {}", this.user.getUserName(),
                this.room.getName());
        releasePublisherEndpoint();
        this.publisher = new PublisherEndpoint(web, dataChannels, this, this.user.getUserName(), pipeline);
        log.debug("PARTICIPANT {}: released publisher endpoint and left it "
                + "initialized (ready for future streaming)", this.user.getUserName());
    }

    public String receiveMediaFrom(Participant sender, String sdpOffer) {
        final String senderName = sender.getName();

        log.info("USER {}: Request to receive media from {} in room {}", this.user.getUserName(), senderName,
                this.room.getName());
        log.trace("USER {}: SdpOffer for {} is {}", this.user.getUserName(), senderName, sdpOffer);

        if (senderName.equals(this.user.getUserName())) {
            log.warn("PARTICIPANT {}: trying to configure loopback by subscribing", this.user.getUserName());
            throw new RoomException(Code.USER_NOT_STREAMING_ERROR_CODE,
                    "Can loopback only when publishing media");
        }

        if (sender.getPublisher() == null) {
            log.warn("PARTICIPANT {}: Trying to connect to a user without " + "a publishing endpoint",
                    this.user.getUserName());
            return null;
        }

        log.debug("PARTICIPANT {}: Creating a subscriber endpoint to user {}", this.user.getUserName(), senderName);

        SubscriberEndpoint subscriber = getNewOrExistingSubscriber(senderName);

        try {
            CountDownLatch subscriberLatch = new CountDownLatch(1);
            SdpEndpoint oldMediaEndpoint = subscriber.createEndpoint(subscriberLatch);
            try {
                if (!subscriberLatch.await(RoomConnection.ASYNC_LATCH_TIMEOUT, TimeUnit.SECONDS)) {
                    throw new RoomException(Code.MEDIA_ENDPOINT_ERROR_CODE,
                            "Timeout reached when creating subscriber endpoint");
                }
            } catch (InterruptedException e) {
                throw new RoomException(Code.MEDIA_ENDPOINT_ERROR_CODE,
                        "Interrupted when creating subscriber endpoint: " + e.getMessage());
            }
            if (oldMediaEndpoint != null) {
                log.warn("PARTICIPANT {}: Two threads are trying to create at "
                        + "the same time a subscriber endpoint for user {}", this.user.getUserName(), senderName);
                return null;
            }
            if (subscriber.getEndpoint() == null) {
                throw new RoomException(Code.MEDIA_ENDPOINT_ERROR_CODE,
                        "Unable to create subscriber endpoint");
            }
        } catch (RoomException e) {
            this.subscribers.remove(senderName);
            throw e;
        }

        log.debug("PARTICIPANT {}: Created subscriber endpoint for user {}", this.user.getUserName(), senderName);
        try {
            String sdpAnswer = subscriber.subscribe(sdpOffer, sender.getPublisher());
            log.trace("USER {}: Subscribing SdpAnswer is {}", this.user.getUserName(), sdpAnswer);
            log.info("USER {}: Is now receiving video from {} in room {}", this.user.getUserName(), senderName,
                    this.room.getName());
            return sdpAnswer;
        } catch (KurentoServerException e) {
            // TODO Check object status when KurentoClient sets this info in the
            // object
            if (e.getCode() == 40101) {
                log.warn("Publisher endpoint was already released when trying "
                        + "to connect a subscriber endpoint to it", e);
            } else {
                log.error("Exception connecting subscriber endpoint " + "to publisher endpoint", e);
            }
            this.subscribers.remove(senderName);
            releaseSubscriberEndpoint(senderName, subscriber);
        }
        return null;
    }

    public void cancelReceivingMedia(String senderName) {
        log.debug("PARTICIPANT {}: cancel receiving media from {}", this.user.getUserName(), senderName);
        SubscriberEndpoint subscriberEndpoint = subscribers.remove(senderName);
        if (subscriberEndpoint == null || subscriberEndpoint.getEndpoint() == null) {
            log.warn("PARTICIPANT {}: Trying to cancel receiving video from user {}. "
                    + "But there is no such subscriber endpoint.", this.user.getUserName(), senderName);
        } else {
            log.debug("PARTICIPANT {}: Cancel subscriber endpoint linked to user {}", this.user.getUserName(),
                    senderName);

            releaseSubscriberEndpoint(senderName, subscriberEndpoint);
        }
    }

    public void mutePublishedMedia(MutedMediaType muteType) {
        if (muteType == null) {
            throw new RoomException(Code.MEDIA_MUTE_ERROR_CODE, "Mute type cannot be null");
        }
        this.getPublisher().mute(muteType);
    }

    public void unmutePublishedMedia() {
        if (this.getPublisher().getMuteType() == null) {
            log.warn("PARTICIPANT {}: Trying to unmute published media. " + "But media is not muted.",
                    this.user.getUserName());
        } else {
            this.getPublisher().unmute();
        }
    }

    public void muteSubscribedMedia(Participant sender, MutedMediaType muteType) {
        if (muteType == null) {
            throw new RoomException(Code.MEDIA_MUTE_ERROR_CODE, "Mute type cannot be null");
        }
        String senderName = sender.getName();
        SubscriberEndpoint subscriberEndpoint = subscribers.get(senderName);
        if (subscriberEndpoint == null || subscriberEndpoint.getEndpoint() == null) {
            log.warn("PARTICIPANT {}: Trying to mute incoming media from user {}. "
                    + "But there is no such subscriber endpoint.", this.user.getUserName(), senderName);
        } else {
            log.debug("PARTICIPANT {}: Mute subscriber endpoint linked to user {}", this.user.getUserName(),
                    senderName);
            subscriberEndpoint.mute(muteType);
        }
    }

    public void unmuteSubscribedMedia(Participant sender) {
        String senderName = sender.getName();
        SubscriberEndpoint subscriberEndpoint = subscribers.get(senderName);
        if (subscriberEndpoint == null || subscriberEndpoint.getEndpoint() == null) {
            log.warn("PARTICIPANT {}: Trying to unmute incoming media from user {}. "
                    + "But there is no such subscriber endpoint.", this.user.getUserName(), senderName);
        } else {
            if (subscriberEndpoint.getMuteType() == null) {
                log.warn("PARTICIPANT {}: Trying to unmute incoming media from user {}. "
                        + "But media is not muted.", this.user.getUserName(), senderName);
            } else {
                log.debug("PARTICIPANT {}: Unmute subscriber endpoint linked to user {}", this.user.getUserName(),
                        senderName);
                subscriberEndpoint.unmute();
            }
        }
    }

    public void close() {
        log.debug("PARTICIPANT {}: Closing user", this.user.getUserName());
        if (isClosed()) {
            log.warn("PARTICIPANT {}: Already closed", this.user.getUserName());
            return;
        }
        this.closed = true;
        for (String remoteParticipantName : subscribers.keySet()) {
            SubscriberEndpoint subscriber = this.subscribers.get(remoteParticipantName);
            if (subscriber != null && subscriber.getEndpoint() != null) {
                releaseSubscriberEndpoint(remoteParticipantName, subscriber);
                log.debug("PARTICIPANT {}: Released subscriber endpoint to {}", this.user.getUserName(),
                        remoteParticipantName);
            } else {
                log.warn("PARTICIPANT {}: Trying to close subscriber endpoint to {}. "
                        + "But the endpoint was never instantiated.", this.user.getUserName(), remoteParticipantName);
            }
        }
        releasePublisherEndpoint();
    }

    /**
     * Returns a {@link SubscriberEndpoint} for the given username. The endpoint is created if not
     * found.
     *
     * @param remoteName name of another user
     * @return the endpoint instance
     */
    public SubscriberEndpoint getNewOrExistingSubscriber(String remoteName) {
        SubscriberEndpoint sendingEndpoint = new SubscriberEndpoint(web, this, remoteName, pipeline);
        SubscriberEndpoint existingSendingEndpoint =
                this.subscribers.putIfAbsent(remoteName, sendingEndpoint);
        if (existingSendingEndpoint != null) {
            sendingEndpoint = existingSendingEndpoint;
            log.trace("PARTICIPANT {}: Already exists a subscriber endpoint to user {}", this.user.getUserName(),
                    remoteName);
        } else {
            log.debug("PARTICIPANT {}: New subscriber endpoint to user {}", this.user.getUserName(), remoteName);
        }
        return sendingEndpoint;
    }

    public void addIceCandidate(String endpointName, IceCandidate iceCandidate) {
        if (this.user.getUserName().equals(endpointName)) {
            this.publisher.addIceCandidate(iceCandidate);
        } else {
            this.getNewOrExistingSubscriber(endpointName).addIceCandidate(iceCandidate);
        }
    }

    public void sendIceCandidate(String endpointName, IceCandidate candidate) {
        room.sendIceCandidate(this.user.getParticipantId(), endpointName, candidate);
    }

    public void sendMediaError(ErrorEvent event) {
        String desc =
                event.getType() + ": " + event.getDescription() + "(errCode=" + event.getErrorCode() + ")";
        log.warn("PARTICIPANT {}: Media error encountered: {}", this.user.getUserName(), desc);
        room.sendMediaError(this.user.getParticipantId(), desc);
    }

    private void releasePublisherEndpoint() {
        if (publisher != null && publisher.getEndpoint() != null) {
            this.streaming = false;
            publisher.unregisterErrorListeners();
            for (MediaElement el : publisher.getMediaElements()) {
                releaseElement(this.user.getUserName(), el);
            }
            releaseElement(this.user.getUserName(), publisher.getEndpoint());
            publisher = null;
        } else {
            log.warn("PARTICIPANT {}: Trying to release publisher endpoint but is null", this.user.getUserName());
        }
    }

    private void releaseSubscriberEndpoint(String senderName, SubscriberEndpoint subscriber) {
        if (subscriber != null) {
            subscriber.unregisterErrorListeners();
            releaseElement(senderName, subscriber.getEndpoint());
        } else {
            log.warn("PARTICIPANT {}: Trying to release subscriber endpoint for '{}' but is null", this.user.getUserName(),
                    senderName);
        }
    }

    private void releaseElement(final String senderName, final MediaElement element) {
        final String eid = element.getId();
        try {
            element.release(new Continuation<Void>() {
                @Override
                public void onSuccess(Void result) throws Exception {
                    log.debug("PARTICIPANT {}: Released successfully media element #{} for {}",
                            user.getUserName(), eid, senderName);
                }

                @Override
                public void onError(Throwable cause) throws Exception {
                    log.warn("PARTICIPANT {}: Could not release media element #{} for {}",
                           user.getUserName(), eid, senderName, cause);
                }
            });
        } catch (Exception e) {
            log.error("PARTICIPANT {}: Error calling release on elem #{} for {}", this.user.getUserName(), eid, senderName,
                    e);
        }
    }

    @Override
    public String toString() {
        return "[User: " + this.user.getUserName() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.user.getParticipantId() == null ? 0 : this.user.getParticipantId().hashCode());
        result = prime * result + (this.user.getUserName() == null ? 0 : this.user.getUserName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Participant)) {
            return false;
        }
        Participant other = (Participant) obj;
        if (this.user.getParticipantId() == null) {
            if (other.user.getParticipantId() != null) {
                return false;
            }
        } else if (!this.user.getParticipantId().equals(other.user.getParticipantId())) {
            return false;
        }
        if (this.user.getUserName() == null) {
            if (other.user.getUserName() != null) {
                return false;
            }
        } else if (!this.user.getUserName().equals(other.user.getUserName())) {
            return false;
        }
        return true;
    }


}
