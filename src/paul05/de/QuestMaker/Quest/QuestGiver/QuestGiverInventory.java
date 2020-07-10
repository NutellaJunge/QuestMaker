package paul05.de.QuestMaker.Quest.QuestGiver;

import java.util.HashMap;

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
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.QuestInfo;
import paul05.de.QuestMaker.Quest.Player.ProgressMenu;
import paul05.de.QuestMaker.Quest.Types.QuestGiverInventoryType;

public class QuestGiverInventory implements Listener {
	
	private Inventory inv = Bukkit.createInventory(null, 9 * 3);
	private QuestGiver giver;
	private ItemStack[] items = new ItemStack[4];
	
	public QuestGiverInventory(QuestGiver giver, QuestGiverInventoryType t) {
		this.giver = giver;
		for (int s = 0; s < inv.getSize(); s++) {
			inv.setItem(s, getItem(Material.BLUE_STAINED_GLASS_PANE, "§4"));
		}
		switch (t) {
		case Quest:
			items[0]=getItem(Material.WRITTEN_BOOK, "§9Quest Infos");
			inv.setItem(4, items[0]);
			items[1]=getItem(Material.EMERALD_BLOCK, "§aGet Quest");
			inv.setItem(9+4, items[1]);
			break;
		case Progress:
			items[0]=getItem(Material.WRITTEN_BOOK, "§9Quest Infos");
			inv.setItem(4, items[0]);
			items[2]=getItem(Material.CLOCK, "§3Quest Progres");
			inv.setItem(9+4, items[2]);
			break;
		case Reward:
			items[3]=getItem(Material.GOLD_BLOCK, "§6Collect Reward");
			inv.setItem(9+4, items[3]);
			break;
		case Collected:
			inv.setItem(9+4, getItem(Material.PAPER, "§aYou have always collected this quest."));
			break;
		}
		Bukkit.getPluginManager().registerEvents(this, main.getInstance());
	}
	
	private ItemStack getItem(Material m, String name) {
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	private int t;
	
	public void open(Player p) {
		p.openInventory(inv);
		giver.setStop(true);
		t = Bukkit.getScheduler().runTaskTimer(main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if (!inv.getViewers().contains(p)) {
					giver.setStop(false);
					Bukkit.getScheduler().cancelTask(t);
				}
			}
		}, 0, 15).getTaskId();
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if (inv != null) {
			if (inv == e.getInventory()) {
				e.setCancelled(true);
				if (item != null) {
					if (item.equals(items[0])) {
						QuestInfo info = giver.getQest().getInfo();
						if (info != null) {
							info.openView(p);
						}
					}
					if (item.equals(items[1])) {
						Quest q = giver.getQest();
						if (!Quest.isActive(p, q)) {
							q.startQuest(p);
							p.closeInventory();
						}
					}
					if (item.equals(items[2])) {
						Quest q = giver.getQest();
						if (Quest.isActive(p, q)) {
							new ProgressMenu(Quest.getActive(p, q));
						}
					}
					if (item.equals(items[3])) {
						Quest q = giver.getQest();
						Quest.getActive(p, q).setCollected();
						Quest.reward(q, p);
						p.closeInventory();
					}
				}
			}
		}
	}
}
