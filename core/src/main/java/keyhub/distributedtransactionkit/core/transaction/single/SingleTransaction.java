package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public interface SingleTransaction extends KhTransaction {

    static <T> SingleTransaction of(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        return new SimpleSingleTransaction<>(transactionProcess, transactionContext);
    }
}
