/**
 * This class reads game setting from the setting.cfg file and sets.
 */

package engine;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;

public class Settings {
	public enum Fields {
		TEXTURE_QUALITY("texqual", Entry.Type.STRING), MODEL_TEXTURE_QUALITY("modeltexqual", Entry.Type.STRING), SCR_W(
				"scrw", Entry.Type.INTEGER), SCR_H("scrh", Entry.Type.INTEGER), SCR_FULL("fullscreen",
				Entry.Type.BOOLEAN), SCR_FREQ("frequency", Entry.Type.INTEGER), SCR_BITS("bitdepth", Entry.Type.INTEGER);

		public String ident;
		public Entry.Type type;

		private Fields(String ident, Entry.Type type) {
			this.ident = ident;
			this.type = type;
		}
	}

	private static LinkedHashMap<Fields, Entry> data = new LinkedHashMap<Fields, Entry>();

	public final static String FILENAME = "settings.cfg";
	public static boolean NOGFX = false;

	public static Entry get(Fields field) {
		return Settings.data.get(field);
	}

	public static void init() {
		Settings.data.put(Fields.TEXTURE_QUALITY, new Entry("high"));
		Settings.data.put(Fields.MODEL_TEXTURE_QUALITY, new Entry("high"));
		Settings.data.put(Fields.SCR_W, new Entry(800));
		Settings.data.put(Fields.SCR_H, new Entry(600));
		Settings.data.put(Fields.SCR_FULL, new Entry(false));
		Settings.data.put(Fields.SCR_FREQ, new Entry(60));
		Settings.data.put(Fields.SCR_BITS, new Entry(32));
	}

	public static void dump() {
		Iterator<Fields> iter = Settings.data.keySet().iterator();
		while (iter.hasNext()) {
			Fields f = iter.next();
			System.out.println(f + " (\"" + f.ident + "\", " + f.type + "): " + Settings.data.get(f).toString());
		}
	}

	public static void save() {
		try {
			PrintStream fout = new PrintStream(new FileOutputStream(FILENAME));

			Iterator<Fields> iter = Settings.data.keySet().iterator();
			while (iter.hasNext()) {
				Fields f = iter.next();
				fout.println(f.ident + " " + Settings.data.get(f).toString());
			}

			fout.flush();
			fout.close();

		} catch (IOException e) {
			Game.logger.log(Level.WARNING, "Unable to save settings.");
		}
	}

	public static void load() {
		HashMap<String, Fields> map = new HashMap<String, Fields>();

		for (Fields f : Fields.values()) {
			map.put(f.ident, f);
		}

		try {
			BufferedReader fin = new BufferedReader(new FileReader(FILENAME));
			String line;

			while ((line = fin.readLine()) != null) {
				String[] parts = line.split(" ", 2);
				if (map.containsKey(parts[0])) {
					Settings.data.put(map.get(parts[0]), new Entry(parts[1], map.get(parts[0]).type));
				}
			}

		} catch (IOException e) {
			Game.logger.log(Level.WARNING, "Unable to save settings.");
		}
	}

	public static class Entry {
		public static enum Type {
			STRING, FLOAT, INTEGER, BOOLEAN
		};

		public Type type;

		public String str;
		public float flt;
		public int i;
		public boolean b;

		public Entry(String str) {
			this.type = Type.STRING;
			this.str = str;
		}

		public Entry(float flt) {
			this.type = Type.FLOAT;
			this.flt = flt;
		}

		public Entry(int i) {
			this.type = Type.INTEGER;
			this.i = i;
		}

		public Entry(boolean b) {
			this.type = Type.BOOLEAN;
			this.b = b;
		}

		public Entry(String string, Type type) {
			this.type = type;
			switch (this.type) {
				case STRING :
					this.str = string;
					break;
				case BOOLEAN :
					this.b = Boolean.parseBoolean(string);
					break;
				case INTEGER :
					this.i = Integer.parseInt(string);
					break;
				case FLOAT :
					this.flt = Float.parseFloat(string);
					break;
			}
		}

		@Override
		public String toString() {
			switch (this.type) {
				case STRING :
					return this.str;
				case FLOAT :
					return Float.toString(this.flt);
				case INTEGER :
					return Integer.toString(this.i);
				case BOOLEAN :
					return Boolean.toString(this.b);
			}
			return "";
		}
	}
}
