/**
 * This class represents the game won window, which appears when the player kills the final boss.
 * 
 * It extends Pane.
 */


package engine.hud;

import gui.elements.Image;
import gui.elements.Pane;

public class GameWonWindow extends Pane {

	public GameWonWindow() {
		super("wndGameWon", 1, 1, 98, 98);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		Image img = new Image("imgGameWon", "winscreen.jpg", 0, 0, 98, 98);
		this.addElement(img);
	}

}
