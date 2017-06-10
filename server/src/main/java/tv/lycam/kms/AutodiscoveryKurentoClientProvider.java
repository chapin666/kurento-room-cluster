package tv.lycam.kms;

import org.kurento.client.KurentoClient;
import org.kurento.client.Properties;
import tv.lycam.api.KurentoClientProvider;
import tv.lycam.api.KurentoClientSessionInfo;
import tv.lycam.exception.RoomException;


/**
 * Created by chengbin on 2017/6/5.
 */
public class AutodiscoveryKurentoClientProvider implements KurentoClientProvider {

    private static final int ROOM_PIPELINE_LOAD_POINTS = 50;


    @Override
    public KurentoClient getKurentoClient(KurentoClientSessionInfo kurentoClientSessionInfo) throws RoomException {
        return KurentoClient.create(Properties.of("loadPoints", ROOM_PIPELINE_LOAD_POINTS));
    }

    @Override
    public boolean destroyWhenUnused() {
        return true;
    }
}
