/**
 * A text area. Automatically applies word wrapping to the 
 * given string. Will resize the font to be of the same relative
 * size no matter what resolution is used.
 */
package gui.elements;

import engine.Settings;
import engine.Settings.Fields;

import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont;
import com.jmex.angelfont.BitmapFontLoader;
import com.jmex.angelfont.BitmapText;
import com.jmex.angelfont.Rectangle;
import com.jmex.angelfont.BitmapFont.Align;

public class TextArea extends AbstractElement {

	private String text;
	private BitmapText txt;
	private Align align;
	private int size;
	private ColorRGBA color;
	private BitmapFont fnt;

	public TextArea(String id, float left, float top, float width, float height, String text) {
		super(id, left, top - height, width, height);

		this.setText(text);
		this.align = Align.Center;
		this.size = 16;
		this.color = ColorRGBA.white;
		this.fnt = BitmapFontLoader.loadDefaultFont();
	}

	@Override
	public void rebuild() {
		int x = this.getScrLeft();
		int y = this.getScrTop() + this.getScrHeight();

		txt = new BitmapText(this.fnt, false);

		txt.setBox(new Rectangle(x, y, this.getScrWidth(), this.getScrHeight()));
		txt.setSize(this.size / (600f / Settings.get(Fields.SCR_H).i));
		txt.setAlignment(this.align);
		txt.setDefaultColor(this.color.clone());
		txt.setText(this.text);
		txt.update();

		this.getNode().detachAllChildren();
		this.getNode().attachChild(txt);

		super.rebuild();
	}

	public float getLineHeight() {
		return this.scrToPctY((int) this.txt.getLineHeight());
	}

	public String getText() {
		return text;
	}

	public void setText(String caption) {
		this.text = caption;
	}

	public void setAlign(Align align) {
		this.align = align;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setColor(ColorRGBA color) {
		this.color = color;
	}

	public void setFnt(BitmapFont fnt) {
		this.fnt = fnt;
	}

	public float getTotalHeight() {
		return this.txt.getHeight();
	}

}
