package paul05.de.QuestMaker.Exceptions;

import java.io.File;
import java.io.PrintWriter;

import org.json.simple.JSONArray;

import paul05.de.QuestMaker.main;

public class AttributeMissingException extends Exception {
	
	private File file;
	private String geter;
	private String from;

	public AttributeMissingException(File f, String geter, String from) {
		this.file = f;
		this.geter = geter;
		this.from = from;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getGeter() {
		return geter;
	}
	
	public String getFrom() {
		return from;
	}
	
	@Override
	public void printStackTrace() {
		JSONArray text = (JSONArray) main.Errorconfig.get("attribute");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%file%", file.getName());
			st = st.replace("%geter%", geter);
			st = st.replace("%from%", from);
			main.logg(st);
		}
	}
	
	@Override
	public void printStackTrace(PrintWriter s) {
		JSONArray text = (JSONArray) main.Errorconfig.get("attribute");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%file%", file.getName());
			st = st.replace("%geter%", geter);
			s.write(st+"\n");
			s.flush();
		}
		s.close();
	}
}
