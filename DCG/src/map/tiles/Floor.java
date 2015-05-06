/**
 * This class represents a floor tile. Chooses different textures depending on dungeon level.
 */

package map.tiles;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

import engine.Game;
import engine.Settings;
import engine.Settings.Fields;

public class Floor extends MapTile {

	protected Box b;

	// private static Node staticNode;

	public Floor(float x, float y, float blocksize, int tier) {
		super();

		b = new Box("Map block " + x + "x" + y, new Vector3f(0, 0, 0), blocksize, blocksize, 0.1f);

		// s.rotateUpTo(new Vector3f(10, 15, 17));
		// b.setModelBound(new BoundingBox());
		// b.updateModelBound();

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);
		ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader().getResource(
				"dcg/data/textures/" + Settings.get(Fields.TEXTURE_QUALITY).str + "/floor" + tier + ".jpeg"),
				Texture.MinificationFilter.NearestNeighborLinearMipMap, Texture.MagnificationFilter.Bilinear));

		b.setRenderState(ts);

		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		ms.setEmissive(new ColorRGBA(0f, .2f, 0f, 1));

		b.setRenderState(ms);

		this.n.attachChild(b);
		this.n.setLocalTranslation(new Vector3f(x * blocksize * 2, y * blocksize * 2, -0.05f));

		// if (staticNode == null) {
		// staticNode = new Node();
		// staticNode.attachChild(b);
		// }
		// this.n = new SharedNode(staticNode);
		// this.n.setLocalTranslation(new Vector3f(x
		// * blocksize * 2, y * blocksize * 2, -0.05f));
	}
}
