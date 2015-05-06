/**
 * This class represents the confirm exit window.
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import engine.Main;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Button;
import gui.elements.Pane;
import gui.implementables.GuiMouseListener;

public class ConfirmExitWindow extends Pane implements GuiMouseListener {

	private Button btnExit, btnCancel;

	public ConfirmExitWindow() {
		super("wndConfirmExit", 35, 50, 30, 10);
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		btnExit = new Button("btnExit", 2, 2, 12, 5, "Exit Game");
		btnExit.setMouseListener(this);
		this.addElement(btnExit);

		btnCancel = new Button("btnCancel", 16, 2, 12, 5, "Cancel");
		btnCancel.setMouseListener(this);
		this.addElement(btnCancel);

		super.rebuild();
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.equals(this.btnExit)) {
			Main.exit();
		}
		if (element.equals(this.btnCancel)) {
			this.hide();
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

}
