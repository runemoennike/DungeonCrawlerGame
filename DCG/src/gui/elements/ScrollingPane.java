/**
 * A scrolling version of the Pane element. Takes any number
 * of elements with any height (the width should be adjusted
 * before hand to match that of the scrolling pane) and handles
 * showing only as many at a time as there is room for.
 * Adds buttons for scrolling up and down through the list.
 */
package gui.elements;

import gui.MouseEvent;
import gui.implementables.GuiMouseListener;

import java.util.ArrayList;

public class ScrollingPane extends Pane implements GuiMouseListener {

	private ArrayList<AbstractElement> content;
	private int scrollPos;
	private boolean showedBottom = false;

	public ScrollingPane(String id, float left, float top, float width, float height) {
		super(id, left, top, width, height);
		this.content = new ArrayList<AbstractElement>();
		this.setTexs("border2.png", "pane2.png");
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		Image imgScrUp = new Image("imgScrUp", "btn_arrow_up.png", 0, this.height - 4, this.width, 4);
		imgScrUp.setMouseListener(this);
		this.addElement(imgScrUp);

		Image imgScrDown = new Image("imgScrDown", "btn_arrow_down.png", 0, 0, this.width, 4);
		imgScrDown.setMouseListener(this);
		this.addElement(imgScrDown);

		float availableHeight = this.getHeight() - imgScrDown.getHeight() - imgScrUp.getHeight() - 2;
		float yPos = this.height - imgScrUp.getHeight() - 1;
		float heightSum = 0;
		int i = 0;

		for (AbstractElement el : this.content) {
			if (i >= scrollPos) {
				if (heightSum + el.getHeight() < availableHeight) {
					this.addElement(el);
					el.setTop(yPos - el.getHeight());
					el.setLeft(1);
					yPos -= el.getHeight();
					heightSum += el.getHeight();
				} else {
					break;
				}
			}
			i++;
		}

		this.showedBottom = (i >= this.content.size());

		super.rebuild();

	}

	public void scrollDown() {
		if (/* this.scrollPos < this.content.size() && */!this.showedBottom) {
			this.scrollPos++;
			this.rebuild();
		}
	}

	public void scrollUp() {
		if (this.scrollPos > 0) {
			this.scrollPos--;
			this.rebuild();
		}
	}

	public void addContent(AbstractElement element) {
		element.setGui(this.guiRef);
		element.setParent(this);
		this.content.add(element);
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.getId().equals("imgScrUp")) {
			this.scrollUp();
		} else if (element.getId().equals("imgScrDown")) {
			this.scrollDown();
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

}
