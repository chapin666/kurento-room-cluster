package tv.lycam.sdk.api;

import org.kurento.client.KurentoClient;
import tv.lycam.sdk.exception.RoomException;

/**
 * Created by chengbin on 2017/6/7.
 */
public interface KurentoClientProvider {

    public KurentoClient getKurentoClient(KurentoClientSessionInfo sessionInfo) throws RoomException;

    boolean destroyWhenUnused();

}
