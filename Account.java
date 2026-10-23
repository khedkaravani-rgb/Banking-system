import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A single bank account: holder, PIN, balance and transaction history.
 */
public class Account
{
    private final String accountNumber;
    private final String holderName;
    private final String pin;
    private double balance;
    private final List<String> transactions;

    public Account(String accountNumber, String holderName, String pin, double openingBalance)
    {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = openingBalance;
        this.transactions = new ArrayList<String>();
        if (openingBalance > 0) {
            addTransaction("Opening deposit", openingBalance);
        }
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName()    { return holderName; }
    public double getBalance()       { return balance; }

    public boolean checkPin(String attempt)
    {
        return pin.equals(attempt);
    }

    public List<String> getTransactions()
    {
        return transactions;
    }

    /** Returns true if the deposit was accepted. */
    public boolean deposit(double amount, String description)
    {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        addTransaction(description, amount);
        return true;
    }

    /** Returns true if the withdrawal succeeded (positive amount and enough funds). */
    public boolean withdraw(double amount, String description)
    {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        addTransaction(description, -amount);
        return true;
    }

    private void addTransaction(String description, double amount)
    {
        String time = new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date());
        String sign = amount >= 0 ? "+" : "-";
        transactions.add(String.format("%s | %-22s | %s%.2f | Bal: %.2f",
                time, description, sign, Math.abs(amount), balance));
    }
}