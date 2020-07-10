package paul05.de.QuestMaker.EntityUtils;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.ItemBook;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenBook;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import paul05.de.QuestMaker.main;

public class EntityUtils {
	
	private static HashMap<LivingEntity, EntityWalker> walker = new HashMap<>();
	
	public static void walk(LivingEntity ent, Location loc) {
		if (!walker.containsKey(ent)) {
			walker.put(ent, new EntityWalker(ent));
		}
		walker.get(ent).walk(loc);
	}
	
	public static EntityWalker getWalker(LivingEntity ent) {
		return walker.get(ent);
	}
	
	public static void openBook(Player p, String title, ArrayList<ArrayList<String>> sts) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta m = (BookMeta) book.getItemMeta();
		m.setTitle("title");
		m.setAuthor("autor");
		m.setGeneration(Generation.TATTERED);
		ArrayList<String> pages = new ArrayList<>();
		for (ArrayList<String> list : sts) {
			String st = "";
			for (String string : list) {
				st+=ChatColor.translateAlternateColorCodes('&', string)+" ";
			}
			pages.add(st);
		}
		m.setPages(pages);
		book.setItemMeta(m);
		
		final int slot = p.getInventory().getHeldItemSlot();
        final ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);
        PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;
        pc.sendPacket(new PacketPlayOutOpenBook(EnumHand.MAIN_HAND));
        p.getInventory().setItem(slot, old);
	}
}
