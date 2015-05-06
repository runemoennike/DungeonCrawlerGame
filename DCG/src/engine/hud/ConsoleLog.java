/**
 * This class represents the console log, used by ConsoleWindow.
 */
package engine.hud;

import java.util.ArrayList;

public class ConsoleLog {
	private static boolean changed = true;
	private static ArrayList<String> lines;

	public static void init() {
		ConsoleLog.lines = new ArrayList<String>();
	}

	public static void addLine(String line) {
		lines.add(line);
		changed = true;
	}

	public static ArrayList<String> getLines() {
		return ConsoleLog.lines;
	}

	public static boolean hasChanged() {
		boolean old = changed;
		changed = false;
		return old;
	}

	public static String getNToLastLine(int i) {
		int idx = lines.size() - 1 - i;

		if (idx >= 0 && idx < lines.size()) {
			return lines.get(idx);
		} else {
			return "";
		}
	}

}