/**
 * An abstract class describing an element (or really anything) 
 * that can contain other elements.
 * Provides update(), rebuild(), and handleMosue() methods that
 * are essential to the workings of the GUI system. They should
 * be called when overwriting, since they trigger the same 
 * methods in the element's children.
 */
package gui;

import gui.elements.AbstractElement;
import gui.implementables.GuiMouseListener;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import com.jme.scene.Node;

public abstract class ElementContainer {

	protected ArrayList<AbstractElement> elements;
	protected Gui guiRef;
	private boolean enabled;
	private boolean visible = true;
	protected ElementContainer parent;

	protected GuiMouseListener mouseListener;

	protected Node n;

	public abstract int getScrTop();
	public abstract int getScrLeft();

	public ElementContainer() {
		this.elements = new ArrayList<AbstractElement>();
		this.n = new Node();
	}

	public void update(float t) {
		for (AbstractElement e : this.elements) {
			if (e.isVisible()) {
				e.update(t);
			}
		}
	}

	public void addElement(AbstractElement element) {
		element.setGui(this.guiRef);
		element.setParent(this);
		if (element.getMouseListener() == null)
			element.setMouseListener(this.mouseListener);
		element.rebuild();
		this.elements.add(element);
		this.n.attachChild(element.getNode());
	}

	public void rebuild() {
		if (this.visible) {
			for (AbstractElement e : this.elements) {
				e.rebuild();
				this.n.attachChild(e.getNode());
			}
			this.n.updateRenderState();
		}
	}

	public ElementContainer getElement(String Id) {
		ElementContainer result = null;

		for (AbstractElement el : this.elements) {
			if (el.getId() == Id) {
				result = el;
				break;
			}
		}

		return result;
	}

	public boolean handleMouse(MouseEvent e) {
		boolean handled = false;

		if (!this.visible) {
			return false;
		}

		try {
			for (ElementContainer el : this.elements) {
				if (el.handleMouse(e)) {
					handled = true;
				}
			}
		} catch (ConcurrentModificationException ex) {
		}

		return handled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void hide() {
		this.n.removeFromParent();
		this.visible = false;
	}

	public void show() {
		this.parent.getNode().attachChild(this.getNode());
		this.visible = true;
	}

	public boolean isVisible() {
		return visible;
	}

	public Node getNode() {
		return n;
	}

	public void setMouseListener(GuiMouseListener mouseListener) {
		this.mouseListener = mouseListener;
	}

	public GuiMouseListener getMouseListener() {
		return mouseListener;
	}

	public void removeAllElements() {
		this.n.detachAllChildren();
		this.elements.clear();
	}
}