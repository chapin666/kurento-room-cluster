package tv.lycam.rest;

/**
 * Created by chengbin on 2017/6/7.
 */
public class ClientConfig {

    private boolean loopbackRemote;
    private boolean lookbackAndLocal;
    private String filteRequestParam;

    public boolean isLoopbackRemote() {
        return loopbackRemote;
    }

    public void setLoopbackRemote(boolean loopbackRemote) {
        this.loopbackRemote = loopbackRemote;
    }

    public boolean isLookbackAndLocal() {
        return lookbackAndLocal;
    }

    public void setLookbackAndLocal(boolean lookbackAndLocal) {
        this.lookbackAndLocal = lookbackAndLocal;
    }

    public String getFilteRequestParam() {
        return filteRequestParam;
    }

    public void setFilteRequestParam(String filteRequestParam) {
        this.filteRequestParam = filteRequestParam;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "loopbackRemote=" + loopbackRemote +
                ", lookbackAndLocal=" + lookbackAndLocal +
                ", filteRequestParam='" + filteRequestParam + '\'' +
                '}';
    }
}
