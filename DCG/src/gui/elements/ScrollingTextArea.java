/**
 * Scrolls a given string vertically over an area.
 * The string should be broken into appropriate length
 * before hand. Hides itself after the last line has
 * scrolled out of view.
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

public class ScrollingTextArea extends AbstractElement {

	private String text;
	private String[] lines;
	private BitmapText txt[];
	private Align align;
	private int size;
	private ColorRGBA color;
	private BitmapFont fnt;
	private float yPos = 0;
	private boolean done = false;

	public ScrollingTextArea(String id, float left, float top, float width, float height, String text) {
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
		int y = this.getScrTop();

		this.yPos = 0;
		this.txt = new BitmapText[this.lines.length];
		this.getNode().detachAllChildren();
		this.done = false;

		for (int i = 0; i < this.lines.length; i++) {
			txt[i] = new BitmapText(this.fnt, false);

			txt[i].setSize(this.getRelSize());
			txt[i].setBox(new Rectangle(0, 0, this.getScrWidth(), this.getScrHeight()));
			txt[i].setAlignment(this.align);
			txt[i].setDefaultColor(this.color);
			txt[i].setText(this.lines[i]);
			txt[i].update();
			this.getNode().attachChild(txt[i]);
		}

		txt[0].setLocalTranslation(x, y + this.getRelSize(), 0);

		super.rebuild();
	}

	private float getRelSize() {
		return this.size / (600f / Settings.get(Fields.SCR_H).i);
	}

	@Override
	public void update(float t) {
		super.update(t);

		this.yPos += t * 10f;

		float bx = this.getScrLeft();
		float by = this.getScrTop() + this.getRelSize();
		boolean showedLines = false;

		for (int i = 0; i < this.txt.length; i++) {
			float y = by + this.yPos - i * this.getRelSize();
			if (y > this.getScrTop() + this.getRelSize() && y < this.getScrTop() + this.getScrHeight()) {
				this.txt[i].setLocalTranslation(bx, y, 0);
				showedLines = true;
			} else {
				this.txt[i].setLocalTranslation(0, 0, 0);
			}
		}

		this.done = !showedLines;
	}

	public boolean isDone() {
		return this.done;
	}

	public float getLineHeight() {
		return this.scrToPctY((int) this.txt[0].getLineHeight());
	}

	public String getText() {
		return text;
	}

	public void setText(String caption) {
		this.text = caption;

		this.lines = this.text.split("\n");
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

}
