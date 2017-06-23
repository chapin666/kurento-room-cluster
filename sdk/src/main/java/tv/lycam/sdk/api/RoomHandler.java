package tv.lycam.sdk.api;

import org.kurento.client.IceCandidate;
import tv.lycam.sdk.internal.Participant;

import java.util.Set;

/**
 * Created by chengbin on 2017/6/7.
 */
public interface RoomHandler {

    /**
     * Called when a new {@link IceCandidate} is gathered for the local WebRTC endpoint. The user
     * should receive a notification with all the provided information so that the candidate is added
     * to the remote WebRTC peer.
     *
     * @param roomName      name of the room
     * @param participantId identifier of the contact
     * @param endpoint      String the identifier of the local WebRTC endpoint (created in the sdk)
     * @param candidate     the gathered {@link IceCandidate}
     */
    void onIceCandidate(String roomName, String participantId, String endpoint,
                        IceCandidate candidate);

    /**
     * Called as a result of an error intercepted on a media element of a contact. The contact
     * should be notified.
     *
     * @param roomName         name of the room
     * @param participantId    identifier of the contact
     * @param errorDescription description of the error
     */
    void onMediaElementError(String roomName, String participantId, String errorDescription);

    /**
     * Called as a result of an error intercepted on the media pipeline. The affected participants
     * should be notified.
     *
     * @param roomName         the room where the error occurred
     * @param participantIds   the participants identifiers
     * @param errorDescription description of the error
     */
    void onPipelineError(String roomName, Set<String> participantIds, String errorDescription);

    /**
     * Called when a new contact joins the conference and there are filters configured
     *
     * @param roomName
     * @param participant
     * @param filterId
     * @param state
     */
    void updateFilter(String roomName, Participant participant, String filterId, String state);

    /**
     * Called to get the next state of a filter when requested by a call to updateFilter
     *
     * @param filterId The filter ID
     * @param state    The current state of the filter
     * @return Then new state of the filter
     */
    String getNextFilterState(String filterId, String state);
}
