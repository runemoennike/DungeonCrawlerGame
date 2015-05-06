/**
 * A label. Obsolete, does not support scaling to different
 * resolutions - should use TextArea instead (in button).
 */
package gui.elements;

import com.jme.scene.Text;

public class Label extends AbstractElement {

	private String caption;
	private Text txt;

	public Label(String id, float left, float top, String caption) {
		super(id, left, top, 0, 0);

		this.setCaption(caption);
	}

	@Override
	public void rebuild() {
		int x = this.getScrLeft();
		int y = this.getScrTop();

		// TODO change to using BitmapFont instead (like TextArea)

		this.txt = Text.createDefaultTextLabel(this.getId(), this.caption);
		this.txt.setLocalTranslation(x, y, 0);

		this.getNode().detachAllChildren();
		this.getNode().attachChild(txt);

		super.rebuild();
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void fastCaption(String caption) {
		this.caption = caption;

		this.txt.print(caption);
	}

	public float getWidth() {
		return this.scrToPctX((int) this.txt.getWidth());
	}

	public float getHeight() {
		return this.scrToPctY((int) this.txt.getHeight());
	}

}
