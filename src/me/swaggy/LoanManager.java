package me.swaggy;

import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoanManager {

    private Map<UUID, Loan> activeLoans = new HashMap<>();
    private LoanDataPersistence loanDataPersistence;
    private Economy economy; // Assuming this is the Vault economy instance
    private final double loanFeePercentage = 5.0;

    public LoanManager(LoanDataPersistence loanDataPersistence, Economy economy) {
        this.loanDataPersistence = loanDataPersistence;
        this.economy = economy;
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
        Loan loan = activeLoans.get(player.getUniqueId());
        if (loan == null) {
            player.sendMessage("You do not have an active loan.");
            return;
        }

        double remaining = loan.repay(amount); // Repay amount and return remaining balance
        economy.withdrawPlayer(player, amount); // Deduct the paid amount from player's account
        if (remaining <= 0) {
            activeLoans.remove(player.getUniqueId()); // Loan fully repaid, remove from active loans
            player.sendMessage("Loan fully repaid!");
        } else {
            player.sendMessage(String.format("Paid %.2f towards loan. Amount remaining: %.2f", amount, remaining));
        }

        loanDataPersistence.saveLoan(player.getUniqueId(), loan); // Update loan data persistence
    }

    public void applyIncomeDeduction(Player player, double deduction) {
        Loan loan = activeLoans.get(player.getUniqueId());
        if (loan != null) {
            double newAmountOwed = loan.getAmountOwed() - deduction;
            if (newAmountOwed < 0) {
                // If overpaid, consider the loan fully repaid and remove it
                activeLoans.remove(player.getUniqueId());
            } else {
                loan.setAmountOwed(newAmountOwed);
                // Optionally, update the loan with the deduction
                loanDataPersistence.saveLoan(player.getUniqueId(), loan);
            }

            // Send a message to the player regarding the deduction
            player.sendMessage(String.format("A total of %.2f has been deducted from your balance towards loan repayment.", deduction));
        }
    }

    public void requestLoan(Player player, double requestedAmount) {
        UUID playerId = player.getUniqueId();

        if (hasActiveLoan(player)) {
            player.sendMessage("You already have an active loan.");
            return;
        }

        // Calculate final loan amount after applying the fee
        double finalLoanAmount = calculateLoanAmount(requestedAmount);
        // Assume the player needs to receive the requested amount, and fee is extra
        boolean transactionSuccess = economy.depositPlayer(player, requestedAmount).transactionSuccess();

        if (transactionSuccess) {
            // Create and store a new loan instance
            Loan loan = new Loan(finalLoanAmount, 0); // initial fee handled separately
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
    // Other methods like getLoanInfo, deductIncomePercentage for event handling, etc.
}
