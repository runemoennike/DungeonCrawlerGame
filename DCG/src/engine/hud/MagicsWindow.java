/**
 * This class the spells window. It is used to select the active spell.
 * 
 * It extends Pane and implements GuiMouseListener.
 */


package engine.hud;

import entities.actors.Player;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Pane;
import gui.elements.ScrollingPane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

import java.util.LinkedList;

import misc.Magic;

import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont.Align;

public class MagicsWindow extends Pane implements GuiMouseListener {

	private static Player player;

	private ScrollingPane spnMagics;

	private boolean requiresUpdate;

	public MagicsWindow(Player player) {
		super("wndMagics", 63, 11, 27, 30);
		MagicsWindow.player = player;
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
		LinkedList<Magic> magics = player.getMagics();

		TextArea dummytext = new TextArea("dummytext", 0, 0, 100, 0, "");
		this.addElement(dummytext);
		float lineHeight = dummytext.getLineHeight();
		dummytext.hide();

		for (Magic m : magics) {
			ColorRGBA color = ColorRGBA.white;

			String text = m.toString();
			float height = text.split("\r\n|\r|\n").length * lineHeight + 1f;

			Pane pane = new Pane("magicPane" + m, 0, 0, spnMagics.getWidth() - 2, height);
			pane.setTexs("pane2.png", "pane2.png");
			pane.setAttachment(m);
			pane.setMouseListener(this);
			spnMagics.addContent(pane);

			TextArea ta = new TextArea("magicText" + m, 0, height, pane.getWidth(), height, text);
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
		if (element.getId().startsWith("magicPane") && element.getAttachment() != null) {
			MagicsWindow.player.setCurMagic((Magic) element.getAttachment());
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
