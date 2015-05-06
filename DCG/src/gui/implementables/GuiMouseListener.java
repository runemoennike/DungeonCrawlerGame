/**
 * Classes registering as mouse event listeners should
 * implement this interface.
 */
package gui.implementables;

import gui.MouseEvent;
import gui.elements.AbstractElement;

public interface GuiMouseListener {
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e);
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e);
}
