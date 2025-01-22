package keyhub.distributedtransactionkit.core.transaction.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.single.AbstractSingleTransaction;
import org.springframework.http.HttpMethod;

import java.util.Map;

public abstract class AbstractRemoteTransaction extends AbstractSingleTransaction<Object> implements RemoteTransaction {
    protected final ObjectMapper objectMapper;

    protected AbstractRemoteTransaction(KhTransactionContext transactionContext, ObjectMapper objectMapper) {
        super(transactionContext);
        this.objectMapper = objectMapper;
    }

    protected AbstractRemoteTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AbstractRemoteTransaction get(String url) {
        return request(HttpMethod.GET, url);
    }

    @Override
    public AbstractRemoteTransaction get(String url, Map<String, Object> params) {
        return request(HttpMethod.GET, url, params);
    }

    @Override
    public AbstractRemoteTransaction post(String url) {
        return request(HttpMethod.POST, url);
    }

    @Override
    public AbstractRemoteTransaction post(String url, Object param) {
        return request(HttpMethod.POST, url, param);
    }

    @Override
    public AbstractRemoteTransaction put(String url) {
        return request(HttpMethod.PUT, url);
    }

    @Override
    public AbstractRemoteTransaction put(String url, Object param) {
        return request(HttpMethod.PUT, url, param);
    }

    @Override
    public AbstractRemoteTransaction delete(String url) {
        return request(HttpMethod.DELETE, url);
    }

    @Override
    public AbstractRemoteTransaction delete(String url, Map<String, Object> params) {
        return request(HttpMethod.DELETE, url, params);
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url) {
        request(method, url, null, null);
        return this;
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url, Map<String, Object> parameters) {
        request(method, url, parameters, null);
        return this;
    }

    @Override
    public AbstractRemoteTransaction request(HttpMethod method, String url, Object body) {
        request(method, url, null, body);
        return this;
    }
}
