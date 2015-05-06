/**
 * This class represents the item info window. It is shown when hovering over items.
 * 
 * It extends Pane.
 */


package engine.hud;

import gui.elements.Pane;
import gui.elements.TextArea;

import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont.Align;

public class ItemInfoWindow extends Pane {

	private TextArea txtTitle, txtContent;
	String title = "TITLE", content = "Content\ngoes\nhere";
	private ColorRGBA titleColor = ColorRGBA.white;

	public ItemInfoWindow() {
		super("wndItemInfo", 0, 0, 30, 20);
		this.setAlpha(true);
		this.setTexs("border4.png", "pane4.png");
		this.setBorderWidth(2);
	}

	@Override
	public void rebuild() {
		super.rebuild();

		String[] lines = this.content.split("\n");
		int lineCount = lines.length;

		this.height = ((lineCount + 2) * 3);

		this.txtTitle = new TextArea("paneInventoryInfoTitle", 0, this.height, 30, 5, this.title);
		this.txtTitle.setAlign(Align.Left);
		this.txtTitle.setColor(this.titleColor);
		this.txtContent = new TextArea("paneInventoryInfoContent", 0, this.height - 6, 30, 5, this.content);
		this.txtContent.setAlign(Align.Left);

		this.addElement(this.txtTitle);
		this.addElement(this.txtContent);
	}

	public void setTitle(String t) {
		this.title = t;
	}

	public void setContent(String t) {
		this.content = t;
	}

	public void setTitleColor(ColorRGBA color) {
		this.titleColor = color;
	}

}
