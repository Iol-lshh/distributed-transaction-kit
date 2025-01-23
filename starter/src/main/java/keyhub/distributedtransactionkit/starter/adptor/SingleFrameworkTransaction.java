package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

import java.util.function.Supplier;

public class SingleFrameworkTransaction extends FrameworkTransaction implements SingleTransaction {
    protected SingleFrameworkTransaction(SingleTransaction innerTransaction) {
        super(innerTransaction);
    }

    public static <T> SingleFrameworkTransaction of(Supplier<T> transactionProcess) {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SingleTransaction transaction = SingleTransaction.of(transactionProcess, context);
        return new SingleFrameworkTransaction(transaction);
    }
}
