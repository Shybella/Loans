package me.swaggy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IncomeMonitor extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final Economy economy;
    private final LoanManager loanManager;
    private final Map<UUID, Double> lastBalances = new HashMap<>();

    public IncomeMonitor(JavaPlugin plugin, LoanManager loanManager) {
        this.plugin = plugin;
        this.loanManager = loanManager;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        this.economy = rsp.getProvider();

        for (Player player : Bukkit.getOnlinePlayers()) {
            lastBalances.put(player.getUniqueId(), economy.getBalance(player));
        }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double lastBalance = lastBalances.getOrDefault(player.getUniqueId(), 0.0);
            double currentBalance = economy.getBalance(player);

            if (currentBalance > lastBalance && loanManager.hasActiveLoan(player)) {
                double income = currentBalance - lastBalance;
                double deduction = income * 0.1; // Deduct 10% for loan repayment

                economy.withdrawPlayer(player, deduction);
                loanManager.applyIncomeDeduction(player, deduction); // Adjust loan repayment

                player.sendMessage(String.format("10%% of your income (%.2f) has been deducted towards your loan repayment.", deduction));
            }

            lastBalances.put(player.getUniqueId(), currentBalance);
        }
    }
}
