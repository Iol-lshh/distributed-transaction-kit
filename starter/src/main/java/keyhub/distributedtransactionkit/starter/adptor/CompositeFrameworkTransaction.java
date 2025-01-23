package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.composite.CompositeTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

public class CompositeFrameworkTransaction extends FrameworkTransaction implements CompositeTransaction {

    protected CompositeFrameworkTransaction(CompositeTransaction innerTransaction) {
        super(innerTransaction);
    }

    public static CompositeFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        CompositeTransaction transaction = CompositeTransaction.from(context);
        return new CompositeFrameworkTransaction(transaction);
    }

    @Override
    public CompositeTransaction add(KhTransaction transaction) {
        ((CompositeTransaction)this.innerTransaction).add(transaction);
        return this;
    }
}
