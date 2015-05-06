/**
 * This class represents the death window, appearing of the player dies.
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Pane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

public class DeathWindow extends Pane implements GuiMouseListener {

	public DeathWindow() {
		super("wndDeath", 35, 50, 30, 10);
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		TextArea txt = new TextArea("txtDeath", 1, this.height, this.width - 2, this.height - 2,
				"You have died and lost experience. Press ENTER to respawn in the sanctuary.");
		this.addElement(txt);

		super.rebuild();
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {

	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

}
