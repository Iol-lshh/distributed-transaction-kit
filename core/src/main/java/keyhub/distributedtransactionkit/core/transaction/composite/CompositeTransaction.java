package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;

public interface CompositeTransaction extends KhTransaction {

    CompositeTransaction add(KhTransaction transaction);

    interface Result extends KhTransaction.Result<Map<TransactionId, KhTransaction.Result<?>>>{
        KhTransaction.Result<?> get(TransactionId transactionId);
    }
}
