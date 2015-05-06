/**
 * An image. Supports transparency of the applied texture.
 */
package gui.elements;

import java.util.HashMap;

import com.jme.image.Texture;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class Image extends AbstractElement {

	private String image = "";

	private static HashMap<String, TextureState> textureStates = new HashMap<String, TextureState>();
	private static BlendState blendState;

	public Image(String id, String image, float left, float top, float width, float height) {
		super(id, left, top, width, height);
		this.image = image;
	}

	@Override
	public void rebuild() {
		int x = this.getScrLeft() + this.getScrWidth() - this.getScrWidth() / 2;
		int y = this.getScrTop() + this.getScrHeight() - this.getScrHeight() / 2;
		int w = this.getScrWidth();
		int h = this.getScrHeight();

		Quad q = new Quad(this.getId(), w, h);

		q.setLocalTranslation(x, y, 0);

		if (!this.textureStates.containsKey(this.image)) {
			TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
			ts.setTexture(TextureManager.loadTexture(getClass().getClassLoader().getResource(
					"dcg/data/textures/" + this.image), Texture.MinificationFilter.NearestNeighborNearestMipMap,
					Texture.MagnificationFilter.NearestNeighbor, 1.0f, true));
			ts.setEnabled(true);
			this.textureStates.put(this.image, ts);
		}

		q.setRenderState(this.textureStates.get(this.image));

		if (Image.blendState == null) {
			Image.blendState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
			Image.blendState.setEnabled(true);
			Image.blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
			Image.blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
			Image.blendState.setBlendEnabled(true);
			Image.blendState.setTestEnabled(true);
			Image.blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
			Image.blendState.setReference(0.1f);
		}

		q.setRenderState(this.blendState);
		// q.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

		q.updateRenderState();

		this.getNode().detachAllChildren();
		this.getNode().attachChild(q);

		super.rebuild();
	}

	public void setImage(String image) {
		this.image = image;
	}

}
