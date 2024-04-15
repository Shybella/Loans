package me.swaggy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoanDataPersistence {

    private JavaPlugin plugin;
    private File loansFile;
    private FileConfiguration loansConfig;

    public LoanDataPersistence(JavaPlugin plugin) {
        this.plugin = plugin;
        createLoansFile();
    }

    private void createLoansFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs(); // Ensure directory exists
        }
        loansFile = new File(plugin.getDataFolder(), "loans.yml");
        if (!loansFile.exists()) {
            try {
                loansFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loansConfig = YamlConfiguration.loadConfiguration(loansFile);
    }

    public void saveLoan(UUID playerId, Loan loan) {
        // Store individual loan data
        loansConfig.set(playerId.toString() + ".amountOwed", loan.getAmountOwed());
        loansConfig.set(playerId.toString() + ".amountPaid", loan.getAmountPaid());
        loansConfig.set(playerId.toString() + ".dateTaken", loan.getDateTaken().toString());
        try {
            loansConfig.save(loansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Loan> loadLoans() {
        Map<UUID, Loan> loadedLoans = new HashMap<>();
        for (String key : loansConfig.getKeys(false)) {
            if (!key.equals("loans")) continue; // Skip if it's not under the "loans" section
            for (String playerKey : loansConfig.getConfigurationSection(key).getKeys(false)) {
                UUID playerId = UUID.fromString(playerKey);
                double amountOwed = loansConfig.getDouble(key + "." + playerKey + ".amountOwed");
                double amountPaid = loansConfig.getDouble(key + "." + playerKey + ".amountPaid");
                String dateTaken = loansConfig.getString(key + "." + playerKey + ".dateTaken");

                Loan loan = new Loan(amountOwed, amountPaid, dateTaken);
                loadedLoans.put(playerId, loan);
            }
        }
        return loadedLoans;
    }

    public void removeLoan(UUID playerId) {
        // Remove the loan data associated with the player's UUID
        loansConfig.set(playerId.toString(), null); // This effectively removes the key and its values
        try {
            loansConfig.save(loansFile); // Save the changed configuration back to "loans.yml"
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}