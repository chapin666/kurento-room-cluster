package tv.lycam.kms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chengbin on 2017/6/6.
 */
public class MaxWebRtcLoadManager implements LoadManager {

    private static final Logger log = LoggerFactory.getLogger(MaxWebRtcLoadManager.class);

    private int maxWebRtcPerKms;

    public MaxWebRtcLoadManager(int maxWebRtcPerKms) {
        this.maxWebRtcPerKms = maxWebRtcPerKms;
    }

    @Override
    public double calculateLoad(Kms kms) {
        int numWebRtcs = countWebRtcEndpoints(kms);
        if (numWebRtcs > maxWebRtcPerKms) {
            return 1;
        } else {
            return numWebRtcs / (double)maxWebRtcPerKms;
        }
    }

    @Override
    public boolean allowMoreElements(Kms kms) {
        return countWebRtcEndpoints(kms) < maxWebRtcPerKms;
    }


    /**
     *
     * @param kms
     * @return
     */
    private synchronized int countWebRtcEndpoints(Kms kms) {
        try {
            return kms.getKurentoClient().getServerManager().getPipelines().size();
        } catch (Throwable e) {
            log.warn("Error counting KurentoClient pipelines", e);
            return 0;
        }
    }
}
