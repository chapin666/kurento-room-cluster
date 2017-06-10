package tv.lycam.rpc;

import org.kurento.jsonrpc.Session;
import org.kurento.jsonrpc.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chengbin on 2017/6/6.
 */
public class SessionWrapper {

    // Logger
    private  static final Logger log = LoggerFactory.getLogger(SessionWrapper.class);

    private ConcurrentHashMap<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    private Session session;


    public SessionWrapper(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public Transaction getTransaction(Integer requestId) {
        return transactions.get(requestId);
    }

    public void addTransaction(Integer requestId, Transaction t) {
        Transaction oldT = transactions.putIfAbsent(requestId, t);
        if (oldT != null) {
            log.error("Found an existing transaction for the key {}", requestId);
        }
    }

    public void removeTransaction(Integer requestId) {
        transactions.remove(requestId);
    }


    public Collection<Transaction> getTransactions() {
        return transactions.values();
    }

}
