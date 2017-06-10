package tv.lycam.kms;

import org.kurento.client.KurentoClient;

/**
 * Created by chengbin on 2017/6/5.
 */
public class FixedOneKmsManager extends KmsManager {

    /**
     *
     * @param kmsWsUri
     */
    public FixedOneKmsManager(String kmsWsUri) {
        this(kmsWsUri, 1);
    }


    /**
     *
     * @param kmsWsUri
     * @param numKmss
     */
    public FixedOneKmsManager(String kmsWsUri, int numKmss) {
        for (int i = 0; i < numKmss; i++) {
            this.addKms(new Kms(KurentoClient.create(kmsWsUri), kmsWsUri));
        }
    }
}
