package paul05.de.QuestMaker.Exceptions;

import java.io.File;
import java.io.PrintWriter;

import org.json.simple.JSONArray;

import paul05.de.QuestMaker.main;

public class JSONSyntaxException extends Exception {
	
	private File file;
	private Object unex;
	private int pos;

	public JSONSyntaxException(File f, Object unex, int pos) {
		this.file = f;
		this.unex = unex;
		this.pos = pos;
	}
	
	private String createArrow(int r, int pos) {
		int i = (r+"").length()+pos+1;
		String st = "^";
		for (int j = 0; j < i; j++) {
			st = " " + st;
		}
		return st;
	}

	public File getFile() {
		return file;
	}
	
	public int getPos() {
		return pos;
	}
	
	public Object getUnex() {
		return unex;
	}
	
	@Override
	public void printStackTrace() {
		JSONArray text = (JSONArray) main.Errorconfig.get("json");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%file%", file.getName());
			st = st.replace("%pos%", ""+pos);
			st = st.replace("%unex%", ""+unex);
			main.logg(st);
		}
	}
	
	@Override
	public void printStackTrace(PrintWriter s) {
		JSONArray text = (JSONArray) main.Errorconfig.get("json");
		for (Object o : text) {
			String st = (String) o;
			st = st.replace("%file%", file.getName());
			st = st.replace("%pos%", ""+pos);
			st = st.replace("%unex%", ""+unex);
			s.write(st+"\n");
			s.flush();
		}
		s.close();
	}
}
