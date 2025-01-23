package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.composite.SequencedTransaction;

public class SequencedFrameworkTransaction extends FrameworkTransaction implements SequencedTransaction {
    protected SequencedFrameworkTransaction(SequencedTransaction innerTransaction) {
        super(innerTransaction);
    }

    @Override
    public SequencedFrameworkTransaction add(KhTransaction transaction) {
        ((SequencedTransaction)this.innerTransaction).add(transaction);
        return this;
    }
}
