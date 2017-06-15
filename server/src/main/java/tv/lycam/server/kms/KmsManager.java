package tv.lycam.server.kms;

import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.lycam.sdk.api.KurentoClientProvider;
import tv.lycam.sdk.api.KurentoClientSessionInfo;
import tv.lycam.sdk.exception.RoomException;
import tv.lycam.sdk.internal.DefaultKurentoClientSessionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Created by chengbin on 2017/6/6.
 */
public class KmsManager implements KurentoClientProvider {



    /**
     *
     *  KMS load compare
     *
     */
    public static class KmsLoad implements Comparable<KmsLoad> {
        private Kms kms;
        private double load;

        public KmsLoad(Kms kms, double load) {
            this.kms = kms;
            this.load = load;
        }

        @Override
        public int compareTo(KmsLoad o) {
            return Double.compare(this.load, o.load);
        }
    }

    private final Logger log = LoggerFactory.getLogger(KmsManager.class);

    private List<Kms> kmss = new ArrayList<Kms>();
    private Iterator<Kms> usageIterator = null;


    @Override
    public KurentoClient getKurentoClient(KurentoClientSessionInfo sessionInfo) throws RoomException {
        if(!(sessionInfo instanceof DefaultKurentoClientSessionInfo)) {
            throw new RoomException(RoomException.Code.GENERIC_ERROR_CODE,
                    "Unkown session info bean type (expected "
                            + DefaultKurentoClientSessionInfo.class.getName() + ")");
        }
        return getKms((DefaultKurentoClientSessionInfo) sessionInfo).getKurentoClient();
    }

    /**
     *
     * @param kms
     */
    public synchronized void addKms(Kms kms) {
        this.kmss.add(kms);
    }

    /**
     *
     * @return
     */
    public synchronized Kms getLessLoadedKms() {
        return Collections.min(getKmsLoads()).kms;
    }

    /**
     *
     * @return
     */
    public List<KmsLoad> getKmsLoads() {
        ArrayList<KmsLoad> kmsLoads = new ArrayList<>();
        for (Kms kms : kmss) {
            double load = kms.getLoad();
            kmsLoads.add(new KmsLoad(kms, load));
            log.trace("Calc load {} for kms: {}", load, kms.getUri());
        }
        return kmsLoads;
    }

    /**
     *
     * @return
     */
    public synchronized Kms getNextLessLoadKms() {
        List<KmsLoad> sortedLoads = getKmssSortedByLoad();
        if (sortedLoads.size() > 1) {
            return sortedLoads.get(1).kms;
        } else {
            return sortedLoads.get(0).kms;
        }
    }

    /**
     *
     * @return
     */
    public List<KmsLoad> getKmssSortedByLoad() {
        List<KmsLoad> kmsLoads =  getKmsLoads();
        Collections.sort(kmsLoads);
        return kmsLoads;
    }

    /**
     *
     * @param sessionInfo
     * @return
     */
    public synchronized Kms getKms(DefaultKurentoClientSessionInfo sessionInfo) {
        if (usageIterator == null || !usageIterator.hasNext()) {
            usageIterator = kmss.iterator();
        }
        return usageIterator.next();
    }

    @Override
    public boolean destroyWhenUnused() {
        return false;
    }
}
