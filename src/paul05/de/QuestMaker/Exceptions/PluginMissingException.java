package paul05.de.QuestMaker.Exceptions;

import java.io.File;
import java.io.PrintWriter;

import org.json.simple.JSONArray;

import paul05.de.QuestMaker.main;

public class PluginMissingException extends Exception {

	private String name;

	public PluginMissingException(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public void printStackTrace() {
		JSONArray text = (JSONArray) main.Errorconfig.get("plugin");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%name%", name);
			main.logg(st);
		}
	}
	
	@Override
	public void printStackTrace(PrintWriter s) {
		JSONArray text = (JSONArray) main.Errorconfig.get("plugin");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%name%", name);
			s.write(st+"\n");
			s.flush();
		}
		s.close();
	}
}
