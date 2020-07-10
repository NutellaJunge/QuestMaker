package assets;

import java.io.InputStream;

public class Assets {
	
	public InputStream getFile(String f) {
		return this.getClass().getResourceAsStream(f);
	}
}
