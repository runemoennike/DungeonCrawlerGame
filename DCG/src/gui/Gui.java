/**
 * Should be used as the root when using the GUI system,
 * and all other elements should be attached here (or to
 * children of children). Provides a method to convert a 
 * jME mouse event into one usable by the GUI system.
 */
package gui;

import gui.MouseEvent.MouseButton;
import gui.MouseEvent.MouseEventType;

import com.jme.input.MouseInput;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;

public class Gui extends ElementContainer {
	private int scrW;
	private int scrH;

	private MouseButton lastBtn;
	private boolean elementHadMouseOver;

	public Gui(int scrW, int scrH) {
		super();
		this.setScrH(scrH);
		this.setScrW(scrW);

		this.guiRef = this;

		this.getNode().setRenderQueueMode(Renderer.QUEUE_ORTHO);
		this.getNode().setLightCombineMode(Spatial.LightCombineMode.Off);
	}

	public void setScrW(int scrW) {
		this.scrW = scrW;
	}

	public int getScrW() {
		return scrW;
	}

	public void setScrH(int scrH) {
		this.scrH = scrH;
	}

	public int getScrH() {
		return scrH;
	}

	public boolean handleJMEMouseInput(MouseInput mi) {
		boolean handled = false;
		int x = mi.getXAbsolute();
		int y = mi.getYAbsolute();
		MouseButton b = MouseButton.NONE;

		this.elementHadMouseOver = false;

		if (mi.isButtonDown(0)) {
			b = MouseButton.BTN1;
		} else if (mi.isButtonDown(1)) {
			b = MouseButton.BTN2;
		}

		if (lastBtn != MouseButton.NONE && b == MouseButton.NONE && (mi.getXDelta() == 0 && mi.getYDelta() == 0)) {
			handled |= this.handleMouse(new MouseEvent(MouseEventType.CLICK, lastBtn, x, y));
		}

		if (lastBtn == MouseButton.NONE && b != MouseButton.NONE) {
			handled |= this.handleMouse(new MouseEvent(MouseEventType.PRESS, b, x, y));
		} else if (lastBtn != MouseButton.NONE && b == MouseButton.NONE) {
			handled |= this.handleMouse(new MouseEvent(MouseEventType.RELEASE, lastBtn, x, y));
		}

		if (mi.getXDelta() != 0 || mi.getYDelta() != 0) {
			handled |= this.handleMouse(new MouseEvent(MouseEventType.MOVE, b, x, y));
		}

		handled |= this.handleMouse(new MouseEvent(MouseEventType.NONE, b, x, y));

		lastBtn = b;

		return handled | this.elementHadMouseOver;
	}

	@Override
	public int getScrLeft() {
		return 0;
	}

	@Override
	public int getScrTop() {
		return 0;
	}

	public void markElementHadMouseOver() {
		this.elementHadMouseOver = true;
	}
}
