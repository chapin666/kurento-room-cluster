package tv.lycam.server.kms;

/**
 * Created by chengbin on 2017/6/6.
 */
public interface LoadManager {

    double calculateLoad(Kms kms);

    boolean allowMoreElements(Kms kms);

}
