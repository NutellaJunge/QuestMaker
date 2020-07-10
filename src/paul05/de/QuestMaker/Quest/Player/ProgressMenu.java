package paul05.de.QuestMaker.Quest.Player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Quest.Task;

public class ProgressMenu implements Listener {
	
	private Inventory inv = Bukkit.createInventory(null, 9 * 3);
	private ItemStack[] items;
	
	public ProgressMenu(RunningQuest q) {
		items = new ItemStack[q.getQuest().getTasks().size()];
		for (int s = 0; s < inv.getSize(); s++) {
			inv.setItem(s, getItem(Material.BLUE_STAINED_GLASS_PANE, "§4"));
		}
		
		int c = q.getComplete()*14/q.getQuest().getTasks().size();
		String st = "";
		for (int i = 0; i < c; i++) {
			st+="§a█";
		}
		for (int i = 0; i < 14-c; i++) {
			st+="§8█";
		}
		
		inv.setItem(9+4, getItem(Material.PAPER, st));
		
		q.getPlayer().openInventory(inv);
		Bukkit.getPluginManager().registerEvents(this, main.getInstance());
	}
	
	@EventHandler
	private void onClick(InventoryClickEvent e) {
		if (e.getClickedInventory().equals(inv)) {
			e.setCancelled(true);
		}
	}
	
	private ItemStack getItem(Material m, String name) {
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
}
