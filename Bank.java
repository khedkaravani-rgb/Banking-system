import java.util.HashMap;
import java.util.Map;

/**
 * Holds all accounts and handles account creation, login and transfers.
 */
public class Bank
{
    private final Map<String, Account> accounts;
    private int nextNumber;

    public Bank()
    {
        accounts = new HashMap<String, Account>();
        nextNumber = 1001;
        // Demo account so you can log in straight away: 1001 / PIN 1234
        createAccount("Demo User", "1234", 5000.00);
    }

    public Account createAccount(String name, String pin, double openingBalance)
    {
        String number = String.valueOf(nextNumber++);
        Account account = new Account(number, name, pin, openingBalance);
        accounts.put(number, account);
        return account;
    }

    /** Returns the account if number and PIN match, otherwise null. */
    public Account login(String accountNumber, String pin)
    {
        Account account = accounts.get(accountNumber);
        if (account != null && account.checkPin(pin)) {
            return account;
        }
        return null;
    }

    /** Returns null on success, or an error message. */
    public String transfer(Account from, String toNumber, double amount)
    {
        Account to = accounts.get(toNumber);
        if (to == null) {
            return "Destination account not found.";
        }
        if (to == from) {
            return "You cannot transfer to your own account.";
        }
        if (amount <= 0) {
            return "Amount must be greater than zero.";
        }
        if (amount > from.getBalance()) {
            return "Insufficient funds.";
        }
        from.withdraw(amount, "Transfer to " + toNumber);
        to.deposit(amount, "Transfer from " + from.getAccountNumber());
        return null;
    }
}