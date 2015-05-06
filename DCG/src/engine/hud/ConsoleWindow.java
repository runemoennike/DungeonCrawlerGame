/**
 * This class represents the console window.
 * 
 * It extends Pane and implements GuiMouseListener and KeyInputListener.
 */

package engine.hud;

import engine.Game;
import engine.Main;
import engine.Profiler;
import engine.Settings;
import engine.World;
import engine.Settings.Fields;
import entities.actors.Player;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Pane;
import gui.implementables.GuiMouseListener;
import threadMessaging.MTMessage;
import threadMessaging.MTMessage.MessageType;

import com.jme.input.KeyInput;
import com.jme.input.KeyInputListener;
import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont;
import com.jmex.angelfont.BitmapFontLoader;
import com.jmex.angelfont.BitmapText;
import com.jmex.angelfont.Rectangle;
import com.jmex.angelfont.BitmapFont.Align;

public class ConsoleWindow extends Pane implements GuiMouseListener, KeyInputListener {

	private Game game;

	private BitmapText txtLog[];
	private BitmapText txtInput;
	private Align align;
	private int size;
	private ColorRGBA color;
	private BitmapFont fnt;
	private int numLines;
	private String input = "";

	private int lineOffset;

	private boolean forceUpdate;

	public ConsoleWindow(Game game) {
		super("wndConsole", 1, 48, 98, 50);
		this.setTexs("border2.png", "pane2.png");
		this.game = game;
		this.setMouseListener(this);
		this.align = Align.Left;
		this.size = 12;
		this.color = ColorRGBA.white;
		this.fnt = BitmapFontLoader.loadDefaultFont();
		this.setAlpha(true);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();
		super.rebuild();

		int x = this.getScrLeft();
		int y = (int) (this.getScrTop() + this.getRelSize() * 2);

		this.numLines = (int) (this.getScrHeight() / this.getRelSize()) - 1;

		this.txtLog = new BitmapText[this.numLines];

		for (int i = 0; i < this.numLines; i++) {
			txtLog[i] = new BitmapText(this.fnt, false);

			txtLog[i].setSize(this.getRelSize());
			txtLog[i].setBox(new Rectangle(0, 0, this.getScrWidth(), this.getRelSize()));
			txtLog[i].setAlignment(this.align);
			txtLog[i].setDefaultColor(this.color);
			txtLog[i].setText("??? " + i);
			txtLog[i].setLocalTranslation(x, y + this.getRelSize() * i, 0);
			txtLog[i].update();
			this.getNode().attachChild(txtLog[i]);
		}

		this.txtInput = new BitmapText(this.fnt, false);
		txtInput.setSize(this.getRelSize());
		txtInput.setBox(new Rectangle(0, 0, this.getScrWidth(), this.getRelSize()));
		txtInput.setAlignment(this.align);
		txtInput.setDefaultColor(this.color);
		txtInput.setText("_");
		txtInput.setLocalTranslation(x, y - this.getRelSize(), 0);
		txtInput.update();
		this.getNode().attachChild(txtInput);
	}

	@Override
	public boolean handleMouse(MouseEvent e) {
		return super.handleMouse(e);
	}

	@Override
	public void update(float t) {
		super.update(t);

		if (ConsoleLog.hasChanged() || this.forceUpdate) {
			for (int i = 0; i < this.numLines; i++) {
				txtLog[i].setText(ConsoleLog.getNToLastLine(i - this.lineOffset));
				txtLog[i].update();
			}
			this.forceUpdate = false;
		}

	}

	private float getRelSize() {
		return this.size / (600f / Settings.get(Fields.SCR_H).i);
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

	@Override
	public void show() {
		super.show();
		KeyInput.get().addListener(this);
	}

	@Override
	public void hide() {
		super.hide();
		KeyInput.get().removeListener(this);
	}

	@Override
	public void onKey(char character, int keyCode, boolean pressed) {
		if (pressed) {
			if (keyCode == KeyInput.KEY_BACK) {
				if (this.input.length() > 0) {
					this.input = this.input.substring(0, this.input.length() - 1);
				}
			} else if (keyCode == KeyInput.KEY_RETURN) {
				parseInput(this.input);
				this.input = "";
			} else if (keyCode == KeyInput.KEY_PGUP) {
				scroll(-this.numLines);
			} else if (keyCode == KeyInput.KEY_PGDN) {
				scroll(this.numLines);
			} else if (keyCode == 43) {
				// ignore
			} else {
				this.input += character;
			}

			this.txtInput.setText(this.input + "_");
			this.txtInput.update();
		}
	}

	private void scroll(int i) {
		this.lineOffset += i;
		if (this.lineOffset > 0) {
			this.lineOffset = 0;
		}
		if (this.lineOffset < -ConsoleLog.getLines().size()) {
			this.lineOffset -= i;
		}
		this.forceUpdate = true;
	}

	private void parseInput(String input2) {
		String parts[] = input2.toLowerCase().split(" ");

		if (parts[0].equals("?") || parts[0].equals("help")) {
			printHelp();
		}
		if (parts[0].equals("caminfo")) {
			this.game.dumpCamInfo();
		}
		if (parts[0].equals("killmobs") || parts[0].equals("km")) {
			this.game.getWorld().getLocalPlayerMap().killAllMobs();
			ConsoleLog.addLine("All monsters removed.");
		}
		if (parts[0].equals("profile")) {
			Profiler.dumpTotals();
		}
		if (parts[0].equals("exit") || parts[0].equals("quit") || parts[0].equals("q")) {
			Main.exit();
		}
		if (parts[0].equals("clear") || parts[0].equals("cls") || parts[0].equals("c")) {
			ConsoleLog.getLines().clear();
			ConsoleLog.addLine("Cleared.");
		}
		if (parts[0].equals("gotomap") || parts[0].equals("gm")) {
			if (parts.length == 2) {
				if (parts[1].equals("all")) {
					for (int i = 0; i < World.LEVELS; i++) {
						this.game.movePlayerToMap(i);
						ConsoleLog.addLine("Player moved to map level " + i + ".");
					}
				} else {
					try {
						if (parts.length == 2 && this.game.movePlayerToMap(Integer.parseInt(parts[1]))) {
							ConsoleLog.addLine("Player moved to map level " + parts[1] + ".");
						} else {
							ConsoleLog.addLine("Invalid level.");
						}
					} catch (NumberFormatException e) {
						ConsoleLog.addLine("Invalid level " + parts[1] + ".");
					}
				}
			}
		}
		if (parts[0].equals("shake") || parts[0].equals("s")) {
			Player pl = Game.getInstance().getWorld().getLocalPlayer();
			float x = Game.getInstance().getWorld().getRndFloat(-9, 9);
			float y = Game.getInstance().getWorld().getRndFloat(-9, 9);
			pl.setPos(pl.getPosition().x + x, pl.getPosition().y + y);
		}
		if (parts[0].equals("iampjaske")) {
			Player pl = Game.getInstance().getWorld().getLocalPlayer();
			int bignum = (int) Math.pow(2, 20);
			pl.getCurStats().setVit(bignum);
			pl.getTotalStats().setVit(bignum);
			pl.getCurStats().setMag(bignum);
			pl.getTotalStats().setMag(bignum);
			pl.getCurStats().setStr(bignum);
			pl.getTotalStats().setStr(bignum);
			pl.getCurStats().setAgi(bignum);
			pl.getTotalStats().setAgi(bignum);
		}
		if (parts[0].equals("gotoloc") || parts[0].equals("gl")) {
			if (parts.length == 3) {
				try {
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					Game.getInstance().getWorld().getLocalPlayer().setPos(x, y);
					ConsoleLog.addLine("Player moved to (" + x + ", " + y + ")");
				} catch (NumberFormatException e) {
					ConsoleLog.addLine("Not valid integer.");
				}
			} else {
				ConsoleLog.addLine("Need two integer parameters.");
			}
		}
		if (parts[0].equals("markpathing") || parts[0].equals("mp")) {
			if (parts.length == 2 && parts[1].equals("off")) {
				Game.markPathing = false;
				Game.getInstance().getWorld().getLocalPlayerMap().clearAllShowPathing();
				ConsoleLog.addLine("Not marking path search");
			} else {
				Game.markPathing = true;
				ConsoleLog.addLine("Marking path search");
			}
		}
		if (parts[0].equals("aistat")) {
			ConsoleLog.addLine("Querying AI thread...");
			Game.getInstance().getAIBrainMsgq().addB(new MTMessage(MessageType.SENDSTAT, null));
		}
		if (parts[0].equals("oneup")) {
			Player pl = Game.getInstance().getWorld().getLocalPlayer();
			pl.addXp(pl.getXpForNextLevel() - pl.getXp() + 1);
		}
		if (parts[0].equals("low")) {
			this.setTop(2);
			this.setHeight(20);
			this.forceUpdate = true;
		}
		if (parts[0].equals("high")) {
			this.setTop(48);
			this.setHeight(50);
			this.forceUpdate = true;
		}
		if (parts[0].equals("slim")) {
			this.setTop(78);
			this.setHeight(20);
			this.forceUpdate = true;
		}
		if (parts[0].equals("joy")) {
			Game.getInstance().getWorld().getLocalPlayer().getInventory().addGold(10000000);
		}
		if (parts[0].equals("fps")) {
			Game.getInstance().getHUD().toggleFPS();
		}
		if (parts[0].equals("hax")) {
			if (parts.length == 2 && parts[1].equals("off")) {
				Game.getInstance().setDevMode(false);
			} else {
				Game.getInstance().setDevMode(true);
			}
		}
	}

	private void printHelp() {
		ConsoleLog.addLine("Type a command and press enter. Use PGUP/PGDN to scroll.");
		ConsoleLog
				.addLine("Commands: help, clear (or c), exit (or q), caminfo, killmobs (or km), profile, gotomap [all | #] (or gm), iampjaske");
		ConsoleLog
				.addLine("          gotoloc # # (or gl), shake (or s), markpathing [off] (or mp), aistat, high, low, slim, joy, fps, hax");
	}
}
