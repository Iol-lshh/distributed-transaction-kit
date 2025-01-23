package keyhub.distributedtransactionkit.core.exception;

import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public class KhCompensationException extends Exception {
    private final transient TransactionId transactionId;

    public TransactionId getTransactionId() {
        return transactionId;
    }
    public KhCompensationException(TransactionId transactionId) {
        this.transactionId = transactionId;
    }
    public KhCompensationException(TransactionId transactionId, String message) {
        super(message);
        this.transactionId = transactionId;
    }
    public KhCompensationException(TransactionId transactionId, String message, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
    }
    public KhCompensationException(TransactionId transactionId, Throwable cause) {
        super(cause);
        this.transactionId = transactionId;
    }
}
