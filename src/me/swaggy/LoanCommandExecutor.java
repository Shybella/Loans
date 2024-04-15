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
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // No command arguments provided, show general help
        if (args.length == 0) {
            player.sendMessage("Usage: /loan <accept|decline|info|pay|take> [args]");
            player.sendMessage("Commands:");
            player.sendMessage("/loan accept - Accept a loan offer.");
            player.sendMessage("/loan decline - Decline a loan offer.");
            player.sendMessage("/loan info - Show info about your current loan.");
            player.sendMessage("/loan pay <amount> - Repay a specific amount of your loan.");
            player.sendMessage("/loan take <amount> - Request a loan of a specific amount.");
            return true; // Return true to indicate the command was handled
        }

        // Handle the "take" command separately due to its unique structure
        if (args.length >= 2 && "take".equalsIgnoreCase(args[0])) {
            try {
                double amount = Double.parseDouble(args[1]);
                loanManager.requestLoan(player, amount);
                return true; // Command was processed successfully
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount. Please specify a valid number.");
                return true; // Return true, but inform of incorrect usage
            }
        }

        // Process other commands with a switch-case block
        switch (args[0].toLowerCase()) {
            case "accept":
                // Existing logic for "accept"
                return handleAccept(player);
            case "decline":
                // Existing logic for "decline"
                return handleDecline(player);
            case "info":
                // Existing logic for "info"
                return handleInfo(player);
            case "pay":
                // Additional check to ensure there is an amount provided with "pay" command
                if (args.length < 2) {
                    player.sendMessage("Please specify an amount to pay.");
                    return true; // Return true, but command wasn't fully processed due to missing amount
                }
                // Existing logic to handle "pay" command
                return handlePay(player, args[1]);
            default:
                // Command not recognized or properly formed, show general help
                player.sendMessage("Unknown command. Use /loan for help.");
                return true; // Return true to indicate command was processed (albeit as unrecognized)
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
