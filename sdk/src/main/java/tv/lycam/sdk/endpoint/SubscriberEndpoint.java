package tv.lycam.sdk.endpoint;

import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.lycam.sdk.api.MutedMediaType;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.exception.RoomException.Code;
import tv.lycam.sdk.internal.Participant;

/**
 * Created by chengbin on 2017/6/7.
 */
public class SubscriberEndpoint extends MediaEndpoint {

    private final static Logger log = LoggerFactory.getLogger(SubscriberEndpoint.class);

    private boolean connectedToPublisher = false;

    private PublisherEndpoint publisher = null;

    public SubscriberEndpoint(boolean web, Participant owner, String endpointName,
                              MediaPipeline pipeline) {
        super(web, false, owner, endpointName, pipeline, log);
    }

    public synchronized String subscribe(String sdpOffer, PublisherEndpoint publisher) {
        registerOnIceCandidateEventListener();
        String sdpAnswer = processOffer(sdpOffer);
        gatherCandidates();
        publisher.connect(this.getEndpoint());
        setConnectedToPublisher(true);
        setPublisher(publisher);
        return sdpAnswer;
    }

    public boolean isConnectedToPublisher() {
        return connectedToPublisher;
    }

    public void setConnectedToPublisher(boolean connectedToPublisher) {
        this.connectedToPublisher = connectedToPublisher;
    }

    public PublisherEndpoint getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherEndpoint publisher) {
        this.publisher = publisher;
    }

    @Override
    public synchronized void mute(MutedMediaType muteType) {
        if (this.publisher == null) {
            throw new RoomException(Code.MEDIA_MUTE_ERROR_CODE, "Publisher endpoint not found");
        }
        switch (muteType) {
            case ALL :
                this.publisher.disconnectFrom(this.getEndpoint());
                break;
            case AUDIO :
                this.publisher.disconnectFrom(this.getEndpoint(), MediaType.AUDIO);
                break;
            case VIDEO :
                this.publisher.disconnectFrom(this.getEndpoint(), MediaType.VIDEO);
                break;
        }
        resolveCurrentMuteType(muteType);
    }

    @Override
    public synchronized void unmute() {
        this.publisher.connect(this.getEndpoint());
        setMuteType(null);
    }


    @Override
    public String toString() {
        return "SubscriberEndpoint{" +
                "connectedToPublisher=" + connectedToPublisher +
                ", publisher=" + publisher +
                "} " + super.toString();
    }
}
