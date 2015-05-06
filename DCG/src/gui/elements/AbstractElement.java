/**
 * Any element that appears on screen. Extends ElementContainer
 * to enable a tree structure of elements. Contains the position
 * and size of the element, as well as a handleMouse() impl. to 
 * check if an event is relevant for this element.
 */
package gui.elements;

import gui.ElementContainer;
import gui.Gui;
import gui.MouseEvent;

import com.jme.renderer.ColorRGBA;

public abstract class AbstractElement extends ElementContainer {
	private String id;

	protected float top;
	protected float left;
	protected float width;
	protected float height;
	private ColorRGBA bgColor;

	private Object attachment;

	public boolean handleMouse(MouseEvent e) {
		if (!this.isVisible())
			return false;

		if (this.isInsideScrBounds(e.getX(), e.getY())) {
			this.guiRef.markElementHadMouseOver();
		}

		boolean result = false;
		if ((e.getButton().equals(MouseEvent.MouseButton.BTN1) || e.getButton().equals(MouseEvent.MouseButton.BTN2))
				&& e.getType().equals(MouseEvent.MouseEventType.CLICK) && this.isInsideScrBounds(e.getX(), e.getY())
				&& this.getMouseListener() != null) {
			if (this.isEnabled()) {
				this.getMouseListener().guiMouseClickEvent(this, e);
			}
			result = true;
		} else if (e.getType().equals(MouseEvent.MouseEventType.MOVE) && this.isInsideScrBounds(e.getX(), e.getY())
				&& this.getMouseListener() != null) {
			if (this.isEnabled()) {
				this.getMouseListener().guiMouseMoveEvent(this, e);
			}

			result = true;
		}

		return result | super.handleMouse(e);
	}

	public AbstractElement() {
		this.setEnabled(true);
		this.setBgColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
	}

	public AbstractElement(String id, float left, float top, float width, float height) {
		this.setEnabled(true);
		this.setId(id);
		this.top = top;
		this.left = left;
		this.width = width;
		this.height = height;
		this.setBgColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
	}

	public boolean isInsideScrBounds(int x, int y) {
		if (x > this.getScrLeft() && x < this.getScrLeft() + this.getScrWidth() && y > this.getScrTop()
				&& y < this.getScrTop() + this.getScrHeight()) {
			return true;
		}
		return false;
	}

	public int pctToScrX(float x) {
		return (int) ((float) this.guiRef.getScrW() * (x / 100.0f));
	}

	public int pctToScrY(float y) {
		return (int) ((float) this.guiRef.getScrH() * (y / 100.0f));
	}

	public float scrToPctX(int i) {
		return ((float) i) / ((float) this.guiRef.getScrW()) * 100.0f - this.getLeft();
	}

	public float scrToPctY(int i) {
		return ((float) i) / ((float) this.guiRef.getScrH()) * 100.0f - this.getTop();
	}

	public int getScrTop() {
		return this.parent.getScrTop() + this.pctToScrY(this.getTop());
	}

	public int getScrLeft() {
		return this.parent.getScrLeft() + this.pctToScrX(this.getLeft());
	}

	protected int getScrWidth() {
		return this.pctToScrX(this.getWidth());
	}

	protected int getScrHeight() {
		return this.pctToScrY(this.getHeight());
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
		rebuild();
	}

	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
		rebuild();
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
		rebuild();
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
		rebuild();
	}

	public void setGui(Gui gui) {
		this.guiRef = gui;
	}

	public Gui getGui() {
		return guiRef;
	}

	public void setBgColor(ColorRGBA bgColor) {
		this.bgColor = bgColor;
	}

	public ColorRGBA getBgColor() {
		return bgColor;
	}

	public void setParent(ElementContainer parent) {
		this.parent = parent;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public Object getAttachment() {
		return attachment;
	}
}
