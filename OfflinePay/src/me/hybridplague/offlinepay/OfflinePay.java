package me.hybridplague.offlinepay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.ess3.api.IEssentials;
import net.milkbowl.vault.economy.Economy;
public class OfflinePay extends JavaPlugin {

	private Economy eco;
	IEssentials ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
	
	@Override
	public void onEnable() {
		if (!setupEconomy()) {
			System.out.println(ChatColor.RED + "You must have Vault" + " and an economy plugin installed.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		super.onDisable();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("offlinepay")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You much be a player to run this command.");
				return true;
			}
			Player p = (Player) sender;
			
			switch(args.length) {
			case 0, 1:
				p.sendMessage(ChatColor.RED + "/offlinepay <player> <amount>");
				break;
			case 2:
				
				// check if amount is a number
				if (!isNum(args[1])) {
					p.sendMessage(ChatColor.RED + "Invalid amount.");
					break;
				}
				
				// check if amount is positive
				if (Float.parseFloat(args[1]) <= 0) {
					p.sendMessage(ChatColor.RED + "Amount must be positive.");
					break;
				}
				
				// check if player has the amount in balance 
				if (eco.getBalance(p) < Float.parseFloat(args[1])) {
					p.sendMessage(ChatColor.RED + "You do not have sufficient funds.");
					break;
				}
				
				
				// check if arg0 player exists on the server
				OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
				if (!op.hasPlayedBefore() && !op.isOnline()) {
					p.sendMessage(ChatColor.RED + "Player not found.");
					break;
				}
				
				// check if arg0 player is accepting payments
				if (!ess.getOfflineUser(op.getName()).isAcceptingPay()) {
					p.sendMessage(ChatColor.RED + "Player is not accepting payments.");
					break;
					
				}
				
				// check if arg0 player is currently online (send mail or not)
				// if online, send chat message
				
				eco.withdrawPlayer(p, Float.parseFloat(args[1]));
				eco.depositPlayer(op, Float.parseFloat(args[1]));
				
				p.sendMessage(ChatColor.GRAY + "You have sent " + op.getName() + ChatColor.GREEN + " " + Float.parseFloat(args[1]) + " Krunas" + ChatColor.GRAY + ".");
				
				if (!op.isOnline()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', "mail send " + op.getName() + " &7You have received &a" + Float.parseFloat(args[1]) + " Krunas &7from &a" + p.getName() + " &7whilst you were offline."));
					break;
				}
				((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You have received &a" + Float.parseFloat(args[1]) + " Krunas &7from &a" + p.getName() + "&7."));
				break;
			default:
				p.sendMessage(ChatColor.RED + "/offlinepay <player> <amount>");
				break;
			}
			
			return true;
		}
		return false;
	}
	
	public boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
}
	
	public boolean isNum(String num) {
		try {
			Float.parseFloat(num);
		} catch (Exception e) {
			return false;
		}
		return true;
		
	}
	
	// Economy
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economy != null)
			eco = economy.getProvider();
		return (eco != null);
	}
}
