/**
 * Represents a mouse event. Is used by the handleMouse()
 * methods.
 */
package gui;

public class MouseEvent {
	public enum MouseButton {
		NONE, BTN1, BTN2
	};
	public enum MouseEventType {
		MOVE, PRESS, RELEASE, CLICK, NONE
	}

	private MouseButton button = MouseButton.NONE;
	private MouseEventType type = MouseEventType.NONE;
	private int x;
	private int y;

	public MouseEvent(MouseEventType type, MouseButton button, int x, int y) {
		this.type = type;
		this.button = button;
		this.x = x;
		this.y = y;
	}

	public MouseButton getButton() {
		if (button == null)
			return MouseButton.NONE;
		return button;
	}

	public MouseEventType getType() {
		if (type == null)
			return MouseEventType.NONE;
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "MouseEvent: " + this.type + ", " + this.button + " at (" + this.x + ", " + this.y + ")";
	}
}
