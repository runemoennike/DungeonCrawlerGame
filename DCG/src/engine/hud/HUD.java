/**
 * This class represents the HUD. All elements of the hud are attached to this object
 * 
 * The HUD class calls update on all the elements.
 * 
 * It extends GameState and implements GuiMouseListener.
 */

package engine.hud;

import engine.Game;
import engine.Profiler;
import gui.Gui;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Image;
import gui.elements.Label;
import gui.elements.Pane;
import gui.elements.ProgBar;
import gui.elements.ScrollingTextArea;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;
import tasks.TaskScheduler;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.system.DisplaySystem;
import com.jmex.angelfont.BitmapFont.Align;
import com.jmex.game.state.GameState;

public class HUD extends GameState implements GuiMouseListener {

	private Node rootNode;
	private Text txtSelection;
	private Game game;
	private Gui gui;

	private Label lblFps, lblTaskLoad, lblTasks;
	private TextArea txtLevel;

	private Pane paneStoryBox;
	private ScrollingTextArea textStoryBox;

	private ProgBar pbarHP;
	private ProgBar pbarMana;
	private InventoryWindow wndInventory;
	private CharacterWindow wndCharacter;
	private BeltWindow wndBelt;
	private ConsoleWindow wndConsole;
	private SkillWindow wndSkill;
	private ConfirmExitWindow wndConfirmExit;
	private TraderWindow wndTrader;
	private MagicsWindow wndMagics;
	private PorterWindow wndPorter;
	private GameWonWindow wndGameWon;
	private DeathWindow wndDeath;
	private TextArea wndMessageText;
	private long messageTimer;
	private Image imgToggleCharacterSheet;
	private Image imgToggleInventory;
	private Image imgToggleSkillSheet;

	private Image imgSelSpell;
	private Pane paneSelSpell;
	private TextArea txtSelSpell;

	private int dungeonLevel = -123;
	private String curSpellName;

	public HUD(Game game) {
		super();

		this.game = game;
		game.setHUD(this);

		rootNode = new Node();
		rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);

		txtSelection = Text.createDefaultTextLabel("selection", "");
		txtSelection.setLocalTranslation(0, DisplaySystem.getDisplaySystem().getHeight() - txtSelection.getHeight(), 0);
		rootNode.attachChild(txtSelection);

		// Init gui
		gui = new Gui(DisplaySystem.getDisplaySystem().getWidth(), DisplaySystem.getDisplaySystem().getHeight());
		gui.setMouseListener(this);

		// Set up fps
		lblFps = new Label("lblFps", 0, 95, "FPS: ");
		lblTaskLoad = new Label("lblTaskLoad", 0, 92, "TaskLoad: ");
		lblTasks = new Label("lblTasks", 0, 89, "Tasks: ");

		gui.addElement(lblFps);
		gui.addElement(lblTasks);
		gui.addElement(lblTaskLoad);

		// Dungeon level
		this.txtLevel = new TextArea("txtLevel", 80, 100, 20, 10, "Dungeon Level: ?");
		this.txtLevel.setColor(ColorRGBA.orange);
		this.txtLevel.setAlign(Align.Right);
		gui.addElement(this.txtLevel);

		// Story box
		this.paneStoryBox = new Pane("paneStoryBox", 25, 70, 50, 20);
		gui.addElement(this.paneStoryBox);

		this.textStoryBox = new ScrollingTextArea("textStoryBox", 0, 20, 50, 20, "");
		this.paneStoryBox.addElement(this.textStoryBox);

		// HP and Mana bars
		this.pbarHP = new ProgBar("pbarHP", 1, 1, 7, 20);
		this.pbarHP.setBgTex("hpbarbg.png");
		this.pbarHP.setBorderTex("hpbarborder.png");

		this.pbarMana = new ProgBar("pbarHP", 92, 1, 7, 20);
		this.pbarMana.setBgTex("manabarbg.png");
		this.pbarMana.setBorderTex("manabarborder.png");

		gui.addElement(this.pbarHP);
		gui.addElement(this.pbarMana);

		// Inventory
		this.wndInventory = new InventoryWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndInventory);

		// Character Sheet
		this.wndCharacter = new CharacterWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndCharacter);

		// Belt
		this.wndBelt = new BeltWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndBelt);

		// Console
		this.wndConsole = new ConsoleWindow(this.game);
		gui.addElement(this.wndConsole);

		// Skills
		this.wndSkill = new SkillWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndSkill);

		// Trader
		this.wndTrader = new TraderWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndTrader);

		// Exit confirmation
		this.wndConfirmExit = new ConfirmExitWindow();
		gui.addElement(this.wndConfirmExit);

		// Info text
		this.wndMessageText = new TextArea("MessageText", 30, 25, 50, 10, "");
		this.wndMessageText.setAlign(Align.Center);
		gui.addElement(this.wndMessageText);

		// Magics
		this.wndMagics = new MagicsWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndMagics);

		// Teleportation
		this.wndPorter = new PorterWindow(this.game.getWorld().getLocalPlayer());
		gui.addElement(this.wndPorter);

		// Death
		this.wndDeath = new DeathWindow();
		gui.addElement(this.wndDeath);

		// Buttons
		this.imgToggleCharacterSheet = new Image("imgToggleCharacterSheet", "btn_charactersheet.png", 9, 1, 7, 8);
		gui.addElement(this.imgToggleCharacterSheet);

		this.imgToggleInventory = new Image("imgToggleInventory", "btn_inventory.png", 17, 1, 7, 8);
		gui.addElement(this.imgToggleInventory);

		this.imgToggleSkillSheet = new Image("imgToggleSkillSheet", "btn_skillsheet.png", 25, 1, 7, 8);
		gui.addElement(this.imgToggleSkillSheet);

		// Selected spell
		this.paneSelSpell = new Pane("paneSelSpell", 62.5f, 2, 22, 6);
		this.paneSelSpell.setBorderWidth(4);
		gui.addElement(this.paneSelSpell);

		this.imgSelSpell = new Image("imgSelSpell", "btn_spells.png", 84, 1, 7, 8);
		gui.addElement(this.imgSelSpell);

		this.txtSelSpell = new TextArea("txtSelSpell", 1, 4.5f, 20.5f, 8, "No spell selected");
		this.txtSelSpell.setAlign(Align.Center);
		this.paneSelSpell.addElement(this.txtSelSpell);

		// win screen
		this.wndGameWon = new GameWonWindow();
		gui.addElement(this.wndGameWon);

		// Finalize gui
		gui.rebuild();

		this.wndInventory.hide();
		this.wndCharacter.hide();
		this.wndSkill.hide();
		this.wndConsole.hide();
		this.wndConfirmExit.hide();
		this.wndTrader.hide();
		this.wndMagics.hide();
		this.wndGameWon.hide();
		this.wndPorter.hide();
		this.wndDeath.hide();

		this.lblFps.hide();
		this.lblTaskLoad.hide();
		this.lblTasks.hide();

		rootNode.attachChild(this.gui.getNode());

		rootNode.setLightCombineMode(Spatial.LightCombineMode.Off);
		rootNode.updateRenderState();
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float tpf) {
		Profiler.start("HUD.render");
		DisplaySystem.getDisplaySystem().getRenderer().draw(this.rootNode);
		Profiler.stop("HUD.render");
	}

	@Override
	public void update(float tpf) {
		Profiler.start("HUD.update");

		if (lblFps.isVisible()) {
			lblFps.fastCaption("FPS: " + String.format("%1$.1f", this.game.getFPS()));
			lblTaskLoad.fastCaption("Task Load: " + String.format("%1$.0f", TaskScheduler.load * 100f) + "%");
			lblTasks.fastCaption("Tasks: " + TaskScheduler.running + "/" + TaskScheduler.total);
		}

		gui.update(tpf);

		updateHpAndMana();

		if (this.textStoryBox.isDone()) {
			this.paneStoryBox.hide();
		}

		if (this.messageTimer < System.currentTimeMillis()) {
			this.wndMessageText.hide();
		}

		if (Game.getInstance().getWorld().getLocalPlayerMap().getLevel() != this.dungeonLevel) {
			this.dungeonLevel = Game.getInstance().getWorld().getLocalPlayerMap().getLevel();
			if (this.dungeonLevel == 0) {
				this.txtLevel.setText("Sanctuary");
			} else {
				this.txtLevel.setText("Dungeon Level: " + this.dungeonLevel);
			}
			this.txtLevel.rebuild();
		}

		if (Game.getInstance().getWorld().getLocalPlayer().getCurMagic() != null
				&& !Game.getInstance().getWorld().getLocalPlayer().getCurMagic().getName().equals(this.curSpellName)) {
			this.curSpellName = Game.getInstance().getWorld().getLocalPlayer().getCurMagic().getName();
			this.txtSelSpell.setText(this.curSpellName);
			this.txtSelSpell.rebuild();
		}

		Profiler.stop("HUD.update");
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Gui getGui() {
		return this.gui;
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		Profiler.start("HUD.guiClickEvent");

		if (element.getId().equals("textStoryBox")) {
			paneStoryBox.hide();
		} else if (element.getId().equals("imgToggleCharacterSheet")) {
			toggleCharacterSheet();
		} else if (element.getId().equals("imgToggleInventory")) {
			toggleInventory();
		} else if (element.getId().equals("imgToggleSkillSheet")) {
			toggleSkillSheet();
		} else if (element.getId().equals("imgSelSpell") || element.getId().equals("paneSelSpell")) {
			toggleMagics();
		}

		Profiler.stop("HUD.guiClickEvent");
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {
	}

	public void toggleFPS() {
		if (this.lblFps.isVisible()) {
			this.lblFps.hide();
			this.lblTaskLoad.hide();
			this.lblTasks.hide();
		} else {
			this.lblFps.show();
			this.lblTaskLoad.show();
			this.lblTasks.show();
		}
	}

	public void toggleInventory() {
		if (this.wndInventory.isVisible()) {
			this.wndInventory.hide();
		} else {
			this.wndInventory.show();
		}
	}

	public void toggleCharacterSheet() {
		if (this.wndCharacter.isVisible()) {
			this.wndCharacter.hide();
		} else {
			this.wndCharacter.show();
		}
	}

	public void toggleSkillSheet() {
		if (this.wndSkill.isVisible()) {
			this.wndSkill.hide();
		} else {
			this.wndSkill.show();
		}
	}

	public void toggleConsole() {
		if (this.wndConsole.isVisible()) {
			this.wndConsole.hide();
		} else {
			this.wndConsole.show();
		}
	}

	public void toggleConfirmExit() {
		if (this.wndConfirmExit.isVisible()) {
			this.wndConfirmExit.hide();
		} else {
			this.wndConfirmExit.show();
		}
	}

	public void toggleMagics() {
		if (this.wndMagics.isVisible()) {
			this.wndMagics.hide();
		} else {
			this.wndMagics.show();
		}
	}

	public void showInfoMessage(String message) {
		showInfoMessage(message, 5000, ColorRGBA.red, 22);
	}

	public void showInfoMessage(String message, int showTime, ColorRGBA color, int fontSize) {
		this.wndMessageText.setColor(color);
		this.wndMessageText.setSize(fontSize);
		this.messageTimer = System.currentTimeMillis() + showTime;
		this.wndMessageText.show();
	}

	public boolean isConsoleOpen() {
		return this.wndConsole.isVisible();
	}

	public void updateHpAndMana() {
		this.pbarHP.setPercentage((int) ((float) this.game.getWorld().getLocalPlayer().getCurStats().getTotalHp()
				/ this.game.getWorld().getLocalPlayer().getTotalStats().getTotalHp() * 100f));
		this.pbarMana.setPercentage((int) ((float) this.game.getWorld().getLocalPlayer().getCurStats().getTotalMana()
				/ this.game.getWorld().getLocalPlayer().getTotalStats().getTotalMana() * 100f));
	}

	public void updateSelectionText() {
		this.txtSelection.print("" + this.game.getSelectionText());
		this.txtSelection.setLocalTranslation(DisplaySystem.getDisplaySystem().getWidth() / 2
				- this.txtSelection.getWidth() / 2, DisplaySystem.getDisplaySystem().getHeight()
				- this.txtSelection.getHeight(), 0);
	}

	public void hideAll() {
		this.wndCharacter.hide();
		this.wndInventory.hide();
		this.wndSkill.hide();
		this.wndTrader.hide();
		this.wndMagics.hide();
		this.wndGameWon.hide();
		this.wndPorter.hide();
	}

	public void requireSkillSheetUpdate() {
		this.wndSkill.requireUpdate();
	}

	public void showTraderWindow() {
		this.wndTrader.show();
		this.wndInventory.show();
	}

	public boolean isTrading() {
		return this.wndTrader.isVisible();
	}

	public void gameWon() {
		this.wndGameWon.show();
	}

	public void showPorterWindow() {
		this.wndPorter.show();
	}

	public void showStoryBox(String text, boolean fixLineBreaks) {
		String t = new String(text);
		if (fixLineBreaks) {
			t = text.replaceAll("!#", "\n");
		}
		this.textStoryBox.setText(t);
		this.textStoryBox.rebuild();
		this.paneStoryBox.show();
	}

	public void showDeathWindow() {
		this.wndDeath.show();
	}

	public void hideDeathWindow() {
		this.wndDeath.hide();
	}

	public void hidePorterWindow() {
		this.wndPorter.hide();
	}
}
