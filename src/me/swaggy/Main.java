package me.swaggy;

import me.swaggy.LoanManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin {

    private LoanManager loanManager;
    private BukkitTask incomeMonitorTask;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize your LoanDataPersistence instance
        LoanDataPersistence loanDataPersistence = new LoanDataPersistence(this);

        // Assuming LoanManager now takes an Economy instance as a parameter
        this.loanManager = new LoanManager(loanDataPersistence, economy);


        // Other initializations like command registration
        this.getCommand("loan").setExecutor(new LoanCommandExecutor(loanManager));

        if (incomeMonitorTask == null || incomeMonitorTask.isCancelled()) {
            IncomeMonitor monitor = new IncomeMonitor(this, loanManager);
            incomeMonitorTask = monitor.runTaskTimer(this, 20L, 20L * 30); // Adjust timing as needed
        }
        getLogger().info("LoanPlugin has been enabled");
    }

    @Override
    public void onDisable() {
        if (incomeMonitorTask != null && !incomeMonitorTask.isCancelled()) {
            incomeMonitorTask.cancel();
        }

        getLogger().info("LoanPlugin has been disabled");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
