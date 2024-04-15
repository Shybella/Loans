package me.swaggy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoanCommandExecutor implements CommandExecutor {

    private LoanManager loanManager;

    public LoanCommandExecutor(LoanManager loanManager) {
        this.loanManager = loanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is being sent by a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length >= 2 && "take".equalsIgnoreCase(args[0])) {
            try {
                // Attempt to parse the amount
                double amount = Double.parseDouble(args[1]);
                loanManager.requestLoan(player, amount);
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount. Please specify a valid number.");
                return true;
            }
        }

        switch (args[0].toLowerCase()) {
            case "accept":
                return handleAccept(player);
            case "decline":
                return handleDecline(player);
            case "info":
                return handleInfo(player);
            case "pay":
                if (args.length < 2) {
                    player.sendMessage("Please specify an amount to pay.");
                    return true;
                }
                return handlePay(player, args[1]);
            default:
                return false; // Show usage
        }
    }

    private boolean handleAccept(Player player) {
        // Logic to accept a loan offer
        loanManager.acceptLoan(player);
        return true;
    }

    private boolean handleDecline(Player player) {
        // Logic to decline a loan offer
        loanManager.declineLoan(player);
        return true;
    }

    private boolean handleInfo(Player player) {
        // Logic to display loan information
        loanManager.displayLoanInfo(player);
        return true;
    }

    private boolean handlePay(Player player, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            loanManager.repayLoan(player, amount);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid amount. Please enter a valid number.");
            return true;
        }
        return true;
    }
}
