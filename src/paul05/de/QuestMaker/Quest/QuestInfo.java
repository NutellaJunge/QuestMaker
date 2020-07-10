package paul05.de.QuestMaker.Quest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import paul05.de.QuestMaker.EntityUtils.EntityUtils;

public class QuestInfo {
	
	private File f;
	private ArrayList<ArrayList<String>> pages = new ArrayList<>();
	private Quest q;
	
	public QuestInfo(Quest q, File f) {
		this.f = f;
		this.q = q;
		registerInfo();
	}
	
	public void registerInfo() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));
			ArrayList<String> page = new ArrayList<>();
			while (r.ready()) {
				String st = r.readLine();
				if (st.equals("/NEWPAGE/")) {
					pages.add(page);
					page = new ArrayList<>();
				} else {
					page.add(st);
				}
			}
			pages.add(page);
			page = new ArrayList<>();
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openView(Player p) {
		EntityUtils.openBook(p, q.getName(), pages);
	}
}
