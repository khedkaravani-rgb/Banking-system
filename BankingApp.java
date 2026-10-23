import javax.swing.*;
import java.awt.*;

/**
 * Swing GUI for the banking application.
 * Run it in BlueJ: right-click BankingApp > void main(String[] args).
 */
public class BankingApp extends JFrame
{
    private static final String LOGIN = "login";
    private static final String CREATE = "create";
    private static final String DASHBOARD = "dashboard";

    private final Bank bank = new Bank();
    private Account current;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // Login widgets
    private final JTextField loginNumber = new JTextField(15);
    private final JPasswordField loginPin = new JPasswordField(15);

    // Create-account widgets
    private final JTextField newName = new JTextField(15);
    private final JPasswordField newPin = new JPasswordField(15);
    private final JTextField newDeposit = new JTextField(15);

    // Dashboard widgets
    private final JLabel welcomeLabel = new JLabel();
    private final JLabel accountLabel = new JLabel();
    private final JLabel balanceLabel = new JLabel();
    private final JTextArea history = new JTextArea();

    public BankingApp()
    {
        super("BlueJ Bank");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        root.add(buildLoginPanel(), LOGIN);
        root.add(buildCreatePanel(), CREATE);
        root.add(buildDashboardPanel(), DASHBOARD);
        add(root);

        setSize(640, 480);
        setLocationRelativeTo(null);
        cards.show(root, LOGIN);
    }

    // ---------- Panels ----------

    private JPanel buildLoginPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = baseConstraints();

        JLabel title = new JLabel("Welcome to BlueJ Bank", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;
        addRow(panel, c, 1, "Account number:", loginNumber);
        addRow(panel, c, 2, "PIN:", loginPin);

        JButton loginBtn = new JButton("Login");
        JButton createBtn = new JButton("Create new account");
        loginBtn.addActionListener(e -> doLogin());
        createBtn.addActionListener(e -> cards.show(root, CREATE));
        loginPin.addActionListener(e -> doLogin());

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(loginBtn, c);
        c.gridy = 4;
        panel.add(createBtn, c);

        JLabel hint = new JLabel("Demo account: 1001 / PIN 1234", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        c.gridy = 5;
        panel.add(hint, c);
        return panel;
    }

    private JPanel buildCreatePanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = baseConstraints();

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;
        addRow(panel, c, 1, "Full name:", newName);
        addRow(panel, c, 2, "4-digit PIN:", newPin);
        addRow(panel, c, 3, "Opening deposit:", newDeposit);

        JButton createBtn = new JButton("Create account");
        JButton backBtn = new JButton("Back");
        createBtn.addActionListener(e -> doCreate());
        backBtn.addActionListener(e -> cards.show(root, LOGIN));

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        panel.add(createBtn, c);
        c.gridy = 5;
        panel.add(backBtn, c);
        return panel;
    }

    private JPanel buildDashboardPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: welcome + balance
        JPanel top = new JPanel(new GridLayout(3, 1));
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        accountLabel.setForeground(Color.GRAY);
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        top.add(welcomeLabel);
        top.add(accountLabel);
        top.add(balanceLabel);
        panel.add(top, BorderLayout.NORTH);

        // Centre: history
        history.setEditable(false);
        history.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(history);
        scroll.setBorder(BorderFactory.createTitledBorder("Transaction history"));
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom: actions
        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 0));
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton logoutBtn = new JButton("Logout");
        depositBtn.addActionListener(e -> doDeposit());
        withdrawBtn.addActionListener(e -> doWithdraw());
        transferBtn.addActionListener(e -> doTransfer());
        logoutBtn.addActionListener(e -> doLogout());
        buttons.add(depositBtn);
        buttons.add(withdrawBtn);
        buttons.add(transferBtn);
        buttons.add(logoutBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Actions ----------

    private void doLogin()
    {
        String number = loginNumber.getText().trim();
        String pin = new String(loginPin.getPassword());
        Account account = bank.login(number, pin);
        if (account == null) {
            error("Invalid account number or PIN.");
            return;
        }
        current = account;
        loginNumber.setText("");
        loginPin.setText("");
        refreshDashboard();
        cards.show(root, DASHBOARD);
    }

    private void doCreate()
    {
        String name = newName.getText().trim();
        String pin = new String(newPin.getPassword());
        if (name.isEmpty()) {
            error("Please enter your name.");
            return;
        }
        if (!pin.matches("\\d{4}")) {
            error("PIN must be exactly 4 digits.");
            return;
        }
        Double deposit = parseAmount(newDeposit.getText(), true);
        if (deposit == null) {
            return;
        }
        Account account = bank.createAccount(name, pin, deposit);
        info("Account created!\nYour account number is " + account.getAccountNumber()
                + "\nKeep it safe - you need it to log in.");
        newName.setText("");
        newPin.setText("");
        newDeposit.setText("");
        loginNumber.setText(account.getAccountNumber());
        cards.show(root, LOGIN);
    }

    private void doDeposit()
    {
        Double amount = askAmount("Amount to deposit:");
        if (amount == null) return;
        if (current.deposit(amount, "Deposit")) {
            refreshDashboard();
        } else {
            error("Amount must be greater than zero.");
        }
    }

    private void doWithdraw()
    {
        Double amount = askAmount("Amount to withdraw:");
        if (amount == null) return;
        if (current.withdraw(amount, "Withdrawal")) {
            refreshDashboard();
        } else {
            error("Insufficient funds or invalid amount.");
        }
    }

    private void doTransfer()
    {
        String target = JOptionPane.showInputDialog(this, "Destination account number:");
        if (target == null) return;
        Double amount = askAmount("Amount to transfer:");
        if (amount == null) return;
        String problem = bank.transfer(current, target.trim(), amount);
        if (problem != null) {
            error(problem);
        } else {
            refreshDashboard();
            info("Transfer successful.");
        }
    }

    private void doLogout()
    {
        current = null;
        cards.show(root, LOGIN);
    }

    // ---------- Helpers ----------

    private void refreshDashboard()
    {
        welcomeLabel.setText("Hello, " + current.getHolderName());
        accountLabel.setText("Account " + current.getAccountNumber());
        balanceLabel.setText(String.format("Balance: %.2f", current.getBalance()));
        StringBuilder sb = new StringBuilder();
        for (String line : current.getTransactions()) {
            sb.append(line).append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    private Double askAmount(String prompt)
    {
        String text = JOptionPane.showInputDialog(this, prompt);
        if (text == null) return null;
        return parseAmount(text, false);
    }

    /** Parses an amount; shows an error and returns null if invalid. */
    private Double parseAmount(String text, boolean allowZero)
    {
        try {
            double value = Double.parseDouble(text.trim());
            if (value < 0 || (!allowZero && value == 0)) {
                error("Amount must be positive.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            error("Please enter a valid number.");
            return null;
        }
    }

    private GridBagConstraints baseConstraints()
    {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field)
    {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private void error(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void info(String message)
    {
        JOptionPane.showMessageDialog(this, message, "BlueJ Bank", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new BankingApp().setVisible(true));
    }
}