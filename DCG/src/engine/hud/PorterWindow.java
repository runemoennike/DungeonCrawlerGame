/**
 * This class represents the porter window, which is used when interacting with the
 * teleportation guy. The window allows the player to teleport between dungeon levels.
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import engine.Game;
import entities.actors.Player;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Pane;
import gui.elements.ScrollingPane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont.Align;

public class PorterWindow extends Pane implements GuiMouseListener {

	private static Player player;

	private ScrollingPane spnMagics;

	private boolean requiresUpdate;

	public PorterWindow(Player player) {
		super("wndPorter", 35, 30, 30, 30);
		PorterWindow.player = player;
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		// Spell scroll pane
		spnMagics = new ScrollingPane("spnSkills", 1, 1, this.width - 2, this.height - 2);
		spnMagics.setMouseListener(this);
		this.addElement(spnMagics);

		// Skills
		TextArea dummytext = new TextArea("dummytext", 0, 0, 100, 0, "");
		this.addElement(dummytext);
		float lineHeight = dummytext.getLineHeight();
		dummytext.hide();

		for (int lvl = 0; lvl <= PorterWindow.player.getDeepestLevelReached(); lvl++) {
			ColorRGBA color = ColorRGBA.white;

			String text = "Dungeon Level " + lvl;
			if (lvl == 0) {
				text = "Sanctuary";
			}
			float height = text.split("\r\n|\r|\n").length * lineHeight + 1f;

			Pane pane = new Pane("porterPane" + lvl, 0, 0, spnMagics.getWidth() - 2, height);
			pane.setTexs("pane2.png", "pane2.png");
			pane.setAttachment((Integer) lvl);
			pane.setMouseListener(this);
			spnMagics.addContent(pane);

			TextArea ta = new TextArea("porterText" + lvl, 0, height, pane.getWidth(), height, text);
			ta.setAlign(Align.Left);
			ta.setColor(color);
			pane.addElement(ta);

		}

		super.rebuild();
	}

	@Override
	public void update(float t) {
		if (this.requiresUpdate) {
			this.rebuild();
			this.requiresUpdate = false;
		}
	}

	@Override
	public void show() {
		this.requiresUpdate = true;
		super.show();
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.getId().startsWith("porterPane") && element.getAttachment() != null) {
			Game.getInstance().movePlayerToMap((Integer) element.getAttachment());
			this.hide();
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

	public void requireUpdate() {
		this.requiresUpdate = true;
	}
}
