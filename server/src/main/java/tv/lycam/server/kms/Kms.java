package tv.lycam.server.kms;

import org.kurento.client.KurentoClient;

/**
 * Created by chengbin on 2017/6/6.
 */
public class Kms {

    private LoadManager loadManager = new MaxWebRtcLoadManager(10000);

    private KurentoClient client;
    private String kmsUri;


    public Kms(KurentoClient client, String kmsUri) {
        this.client = client;
        this.kmsUri = kmsUri;
    }

    public void setLoadManager(LoadManager loadManager) {
        this.loadManager = loadManager;
    }

    public double getLoad() {
        return loadManager.calculateLoad(this);
    }

    public boolean allowMoreElements() {
        return loadManager.allowMoreElements(this);
    }

    public String getUri() {
        return kmsUri;
    }

    public KurentoClient getKurentoClient() {
        return this.client;
    }
}
