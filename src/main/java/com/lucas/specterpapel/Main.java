package com.lucas.specterpapel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
	@Override
	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage("");
		Bukkit.getConsoleSender().sendMessage("§7==========================");
		Bukkit.getConsoleSender().sendMessage("§7| §bSpecterPapel          §7|");
		Bukkit.getConsoleSender().sendMessage("§7| §bVersão 1.0            §7|");
		Bukkit.getConsoleSender().sendMessage("§7| §fStatus: §aLigado         §7|");
		Bukkit.getConsoleSender().sendMessage("§7==========================");
		Bukkit.getConsoleSender().sendMessage("");
		File f = new File(getDataFolder(), "config.yml");
		if (!f.exists())
			saveDefaultConfig();
		loadAll();
		c("Plugin habilitado");
	}

	public void c(String msg) {
		Bukkit.getConsoleSender().sendMessage("§a[SpecterPapel] " + msg);
	}

	public void loadAll() {
		buildNewItens();
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("darpapel").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
		if (s instanceof Player) {
			if (!s.hasPermission("papelvip.usar")) {
				s.sendMessage("§cVocê não tem permissão para fazer isto.");
				return true;
			}
		}
		if (args.length < 2 || args.length < 1) {
			s.sendMessage("§cUtilize /darpapel <jogador> <nomedopapel>");
			return true;
		}
		Player t = Bukkit.getPlayer(args[0]);
		if (t == null) {
			s.sendMessage("§c" + args[0] + " jogador está offline.");
			return true;
		}
		String nameItem = args[1].toUpperCase();
		if (!NAME_ITENS.containsKey(nameItem)) {
			s.sendMessage("§cO item requisitado não existe. Os itens validos são: " + NAME_ITENS.keySet().toString());
			return true;
		}
		ItemStack item = NAME_ITENS.get(nameItem).clone();
		int quantia;
		if (args.length < 3) {
			quantia = 1;
		} else {
			try {
				quantia = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				s.sendMessage("§cO" + args[2] + "não é um número valido.");
				return true;
			}
		}
		item.setAmount(quantia);
		t.getInventory().addItem(item);
		s.sendMessage("§aVocê enviou o §2" + nameItem + "§apara o §2" + t.getName() + " §acom sucesso.");
		return true;
	}

	private String getMessage(String string) {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString(string));
	}

	public static HashMap<ItemStack, List<String>> NEW_ITENS = new HashMap<>();
	public static HashMap<String, ItemStack> NAME_ITENS = new HashMap<>();

	@SuppressWarnings("deprecation")
	private void buildNewItens() {
		Set<String> itens = getConfig().getConfigurationSection("Itens").getKeys(false);
		for (String item : itens) {
			int idItem = getConfig().getInt("Itens." + item + ".ID");
			int dataItem = getConfig().getInt("Itens." + item + ".Data");
			String nomeItem = getConfig().getString("Itens." + item + ".Nome");
			List<String> loreItem = getConfig().getStringList("Itens." + item + ".Lore");
			List<String> executa = getConfig().getStringList("Itens." + item + ".Comandos");
			nomeItem = nomeItem.replace("&", "§");
			ItemStack ItemEspecial = new ItemStack(idItem);
			ItemMeta ItemEspecialMeta = ItemEspecial.getItemMeta();
			ItemEspecialMeta.setDisplayName(nomeItem);
			ItemEspecialMeta.setLore(coloredLore(loreItem));
			ItemEspecial.setDurability((short) dataItem);
			ItemEspecial.setItemMeta(ItemEspecialMeta);
			NEW_ITENS.put(ItemEspecial, executa);
			NAME_ITENS.put(item.toUpperCase(), ItemEspecial);
		}
	}

	private static List<String> coloredLore(List<String> oldLore) {
		List<String> coloredLore = new ArrayList<>();
		for (String s : oldLore) {
			coloredLore.add(s.replace('&', '§'));
		}
		return coloredLore;
	}

	@EventHandler
	public void usarItem(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getItem() == null)
				return;
			ItemStack item = e.getItem();
			for (ItemStack i : NEW_ITENS.keySet()) {
				if (item.isSimilar(i)) {
					dispatchCommands(e.getPlayer(), NEW_ITENS.get(i));
					removeItem(e.getPlayer());
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	private void removeItem(Player p) {
		if (p.getItemInHand().getAmount() < 2) {
			p.setItemInHand(new ItemStack(Material.AIR));
		} else {
			ItemStack item = p.getItemInHand();
			item.setAmount(item.getAmount() - 1);
		}
	}

	private void dispatchCommands(Player p, List<String> commands) {
		for (String command : commands) {
			String line = command.replace('&', '§').replace("@jogador", p.getName());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
			continue;

		}
	}

}
