/*
 * BankSimulation.java
 * A simple bank account simulation demonstrating OOP: classes, inheritance,
 * method overriding, and transaction history.
 *
 * How to compile & run (terminal / VS Code):
 *   javac BankSimulation.java
 *   java BankSimulation
 *
 * Tools: Java 8+ (JDK), VS Code (Java Extension Pack recommended), Terminal
 */

import java.util.*;
import java.time.*;

public class BankSimulation {
    private static Scanner scanner = new Scanner(System.in);
    private static Map<String, Account> accounts = new HashMap<>();

    public static void main(String[] args) {
        // Pre-create two sample accounts for quick testing
        Account acc1 = new SavingsAccount("SA1001", "Alice", 5000.0, 2.5);
        Account acc2 = new CurrentAccount("CA2001", "Bob", 2000.0, 500.0, 50.0);
        accounts.put(acc1.getAccountNumber(), acc1);
        accounts.put(acc2.getAccountNumber(), acc2);

        System.out.println("=== Welcome to the Bank Account Simulation ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": createAccount(); break;
                case "2": deposit(); break;
                case "3": withdraw(); break;
                case "4": printStatement(); break;
                case "5": listAccounts(); break;
                case "6": running = false; break;
                default:
                    System.out.println("Invalid choice. Please enter a number from the menu.");
            }
        }
        System.out.println("Thank you for using the simulation. Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Create account (Savings / Current)");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Print account statement");
        System.out.println("5. List accounts");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private static void createAccount() {
        System.out.print("Enter account type (S for Savings / C for Current): ");
        String type = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter account number (e.g. SA1002): ");
        String accNum = scanner.nextLine().trim();
        if (accounts.containsKey(accNum)) {
            System.out.println("Account number already exists. Try again with a unique number.");
            return;
        }
        System.out.print("Enter account holder name: ");
        String holder = scanner.nextLine().trim();
        double initial = readDouble("Enter initial deposit amount: ");
        try {
            if (type.equals("S")) {
                double interest = readDouble("Enter annual interest rate (percent, e.g. 2.5): ");
                Account acc = new SavingsAccount(accNum, holder, initial, interest);
                accounts.put(accNum, acc);
                System.out.println("Savings account created: " + acc.getAccountInfo());
            } else if (type.equals("C")) {
                double overdraft = readDouble("Enter overdraft limit (e.g. 500): ");
                double fee = readDouble("Enter overdraft fee (e.g. 50): ");
                Account acc = new CurrentAccount(accNum, holder, initial, overdraft, fee);
                accounts.put(accNum, acc);
                System.out.println("Current account created: " + acc.getAccountInfo());
            } else {
                System.out.println("Unknown account type. Use S or C.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    private static void deposit() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        Account acc = accounts.get(accNum);
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }
        double amount = readDouble("Enter deposit amount: ");
        try {
            acc.deposit(amount);
            System.out.println("Deposit successful. New balance: " + String.format("%.2f", acc.getBalance()));
        } catch (IllegalArgumentException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private static void withdraw() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        Account acc = accounts.get(accNum);
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }
        double amount = readDouble("Enter withdrawal amount: ");
        try {
            acc.withdraw(amount);
            System.out.println("Withdrawal successful. New balance: " + String.format("%.2f", acc.getBalance()));
        } catch (IllegalArgumentException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private static void printStatement() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        Account acc = accounts.get(accNum);
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }
        acc.printStatement();
    }

    private static void listAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts available.");
            return;
        }
        System.out.println("\nAccounts:");
        for (Account acc : accounts.values()) {
            System.out.println(acc.getAccountInfo());
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

// Account and subclasses
abstract class Account {
    protected String accountNumber;
    protected String accountHolder;
    protected double balance;
    protected List<Transaction> transactions = new ArrayList<>();

    public Account(String accountNumber, String accountHolder, double initialBalance) {
        if (accountNumber == null || accountNumber.isEmpty()) throw new IllegalArgumentException("Account number required");
        if (accountHolder == null || accountHolder.isEmpty()) throw new IllegalArgumentException("Account holder name required");
        if (initialBalance < 0) throw new IllegalArgumentException("Initial balance cannot be negative");
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = initialBalance;
        addTransaction("OPEN", initialBalance, "Account opened");
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        balance += amount;
        addTransaction("DEPOSIT", amount, "Deposit");
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        if (balance < amount) throw new InsufficientFundsException("Insufficient funds.");
        balance -= amount;
        addTransaction("WITHDRAW", amount, "Withdrawal");
    }

    protected void addTransaction(String type, double amount, String desc) {
        transactions.add(new Transaction(LocalDateTime.now(), type, amount, balance, desc));
    }

    public void printStatement() {
        System.out.println("\n--- Statement for " + accountNumber + " (" + accountHolder + ") ---");
        System.out.println("Current balance: " + String.format("%.2f", balance));
        System.out.println("Date & Time           | Type     | Amount     | BalanceAfter | Description");
        System.out.println("-----------------------+----------+------------+--------------+----------------");
        for (Transaction t : transactions) {
            System.out.println(t.toTableString());
        }
        System.out.println("---------------------------------------- End of statement ----------------------------------------");
    }

    public String getAccountInfo() {
        return accountNumber + " | " + accountHolder + " | Balance: " + String.format("%.2f", balance);
    }
}

class SavingsAccount extends Account {
    private double annualInterestRate;
    private static final double MIN_BALANCE = 1000.0;

    public SavingsAccount(String accountNumber, String accountHolder, double initialBalance, double annualInterestRate) {
        super(accountNumber, accountHolder, initialBalance);
        this.annualInterestRate = annualInterestRate;
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        if (balance - amount < MIN_BALANCE) {
            throw new InsufficientFundsException("Cannot withdraw. Savings accounts must maintain a minimum balance of " + MIN_BALANCE);
        }
        balance -= amount;
        addTransaction("WITHDRAW", amount, "Savings withdrawal");
    }

    // Apply monthly interest (simple monthly interest for demonstration)
    public void applyMonthlyInterest() {
        double monthlyRate = (annualInterestRate / 100.0) / 12.0;
        double interest = balance * monthlyRate;
        if (interest > 0) {
            balance += interest;
            addTransaction("INTEREST", interest, "Monthly interest applied");
        }
    }
}

class CurrentAccount extends Account {
    private double overdraftLimit;
    private double overdraftFee;

    public CurrentAccount(String accountNumber, String accountHolder, double initialBalance, double overdraftLimit, double overdraftFee) {
        super(accountNumber, accountHolder, initialBalance);
        if (overdraftLimit < 0) throw new IllegalArgumentException("Overdraft limit cannot be negative.");
        if (overdraftFee < 0) throw new IllegalArgumentException("Overdraft fee cannot be negative.");
        this.overdraftLimit = overdraftLimit;
        this.overdraftFee = overdraftFee;
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        double projected = balance - amount;
        if (projected < -overdraftLimit) {
            throw new InsufficientFundsException("Cannot withdraw: would exceed overdraft limit of " + overdraftLimit);
        }
        balance -= amount;
        addTransaction("WITHDRAW", amount, "Current withdrawal");
        // If account goes negative, apply overdraft fee
        if (balance < 0) {
            balance -= overdraftFee;
            addTransaction("FEE", overdraftFee, "Overdraft fee applied");
        }
    }
}

class Transaction {
    private LocalDateTime timestamp;
    private String type;
    private double amount;
    private double balanceAfter;
    private String description;

    public Transaction(LocalDateTime timestamp, String type, double amount, double balanceAfter, String description) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public String toTableString() {
        return String.format("%-22s | %-8s | %10.2f | %12.2f | %s",
                timestamp.toString(), type, amount, balanceAfter, description);
    }

    @Override
    public String toString() {
        return timestamp + " | " + type + " | " + amount + " | balance: " + balanceAfter + " | " + description;
    }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String msg) {
        super(msg);
    }
}
