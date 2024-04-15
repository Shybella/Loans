package me.swaggy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoanManager {

    private Map<UUID, Loan> activeLoans = new HashMap<>();
    //private Map<UUID, Boolean> recentLoanTransactions = new HashMap<>();
    private LoanDataPersistence loanDataPersistence;
    private Economy economy; // Assuming this is the Vault economy instance
    private final double loanFeePercentage = 5.0;
    private JavaPlugin plugin;

    public LoanManager(LoanDataPersistence loanDataPersistence, Economy economy) {
        this.loanDataPersistence = loanDataPersistence;
        this.economy = economy;
        this.plugin = plugin;
        // Optionally load active loans from data persistence
        this.activeLoans = loanDataPersistence.loadLoans();
    }

    public void acceptLoan(Player player) {
        double balance = economy.getBalance(player);
        double loanAmount = balance * 2.5; // Loan amount is 2.5 times the player's current balance
        double fee = loanAmount * 0.05; // A 5% fee of the total loan amount
        loanAmount += fee; // Add the fee to the loan amount
        economy.depositPlayer(player, loanAmount); // Deposit the loan amount into player's account

        Loan loan = new Loan(loanAmount, fee);
        activeLoans.put(player.getUniqueId(), loan);
        loanDataPersistence.saveLoan(player.getUniqueId(), loan); // Persist loan data

        player.sendMessage(String.format("Loan accepted. Amount: %.2f, Fee: %.2f", loanAmount, fee));
    }

    public void declineLoan(Player player) {
        player.sendMessage("Loan opportunity declined.");
    }

    public void displayLoanInfo(Player player) {
        Loan loan = activeLoans.get(player.getUniqueId());
        if (loan == null) {
            player.sendMessage("You do not have an active loan.");
        } else {
            player.sendMessage(String.format("Loan amount owed: %.2f, Amount paid: %.2f, Loan date: %s", loan.getAmountOwed(), loan.getAmountPaid(), loan.getDateTaken().toString()));
        }
    }

    public void repayLoan(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        Loan loan = activeLoans.get(playerId);

        if (loan == null) {
            player.sendMessage("You do not have an active loan.");
            return;
        }

        loan.setAmountOwed(loan.getAmountOwed() - amount); // Deduct the amount from owed
        loan.setAmountPaid(loan.getAmountPaid() + amount); // Update the total amount paid

        if (loan.getAmountOwed() <= 0) {
            // If the loan is fully repaid or overpaid
            activeLoans.remove(playerId);
            economy.withdrawPlayer(player, amount);
            loanDataPersistence.removeLoan(playerId);
            player.sendMessage("Congratulations! Your loan has been fully repaid.");
        } else {
            loanDataPersistence.saveLoan(playerId, loan); // Save the updated loan information
            economy.withdrawPlayer(player, amount);
            player.sendMessage(String.format("Thank you. You've repaid %.2f. Your remaining loan balance is %.2f.", amount, loan.getAmountOwed()));
        }
    }
    public void applyIncomeDeduction(Player player, double deduction) {
        Loan loan = activeLoans.get(player.getUniqueId());

//        if (recentLoanTransactions.containsKey(player.getUniqueId())) {
//            return; // Skip deduction this time
//        }

        if (loan != null) {
            loan.setAmountOwed(loan.getAmountOwed() - deduction); // Apply deduction
            loan.setAmountPaid(loan.getAmountPaid() + deduction); // Update amount paid

            if (loan.getAmountOwed() <= 0) {
                // If overpaid or exactly fully repaid
                activeLoans.remove(player.getUniqueId());
                loanDataPersistence.removeLoan(player.getUniqueId());
                player.sendMessage("Congratulations! Your loan has been fully repaid.");
            } else {
                loanDataPersistence.saveLoan(player.getUniqueId(), loan); // Persist updates
                player.sendMessage(String.format("A total of %.2f has been deducted from your balance towards loan repayment. Remaining balance: %.2f", deduction, loan.getAmountOwed()));
            }
        }
    }

    public void requestLoan(Player player, double requestedAmount) {
        UUID playerId = player.getUniqueId();

        if (hasActiveLoan(player)) {
            player.sendMessage("You already have an active loan.");
            return;
        }

        // Check the player's current balance to determine maximum loan amount
        double currentBalance = economy.getBalance(player);
        double maxLoanAmount = currentBalance * 2.5; // The maximum loan amount is 2.5 times the current balance

        // Check if requested amount exceeds the maximum loan amount
        if (requestedAmount > maxLoanAmount) {
            player.sendMessage(String.format("Loan request denied. The maximum loan amount based on your current balance (%.2f) is %.2f.", currentBalance, maxLoanAmount));
            return;
        }

        // Proceed with processing the loan request
        double finalLoanAmount = calculateLoanAmount(requestedAmount);
        boolean transactionSuccess = economy.depositPlayer(player, requestedAmount).transactionSuccess();

        if (transactionSuccess) {
            // Create and store a new loan instance
            //recentLoanTransactions.put(player.getUniqueId(), true);
            // Schedule the removal of this flag after a short delay
            //Bukkit.getScheduler().runTaskLater(plugin, () -> recentLoanTransactions.remove(player.getUniqueId()), 20L * 5); // 60 seconds delay
            Loan loan = new Loan(finalLoanAmount, 0); // Adjust parameters as needed for your Loan class constructor
            activeLoans.put(playerId, loan);
            loanDataPersistence.saveLoan(playerId, loan); // Persist the new loan
            player.sendMessage(String.format("Loan for %.2f has been processed. Fee Applied: %.2f", requestedAmount, finalLoanAmount - requestedAmount));
        } else {
            player.sendMessage("Failed to process the loan. Please try again.");
        }
    }

    public double calculateLoanAmount(double requestedAmount) {
        double fee = requestedAmount * (loanFeePercentage / 100.0);
        return requestedAmount + fee; // Amount after adding the fee
    }

    public boolean hasActiveLoan(Player player) {
        return activeLoans.containsKey(player.getUniqueId());
    }
}
