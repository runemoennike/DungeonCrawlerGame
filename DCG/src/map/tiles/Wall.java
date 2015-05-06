/**
 * Class that represents a wall. Chooses different textures depending on dungeon level.
 * Also makes sure the walls faces the right ways.
 */

package map.tiles;

import map.Map;

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

public class Wall extends MapTile {

	protected Box b1, b2, b3, b4;

	public Wall(float x, float y, float blocksize, int orient, int tier) {
		super();

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);
		ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader().getResource(
				"dcg/data/textures/" + Settings.get(Fields.TEXTURE_QUALITY).str + "/wall" + tier + ".jpeg"),
				Texture.MinificationFilter.NearestNeighborLinearMipMap, Texture.MagnificationFilter.Bilinear));

		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		ms.setEmissive(new ColorRGBA(0f, .2f, 0f, 1));

		if ((orient & Map.ORIENT_S) != 0) {
			b1 = new Box("kranz", new Vector3f(0, 0 - blocksize, blocksize), blocksize, 0.1f, blocksize);

			b1.setRenderState(ts);
			b1.setRenderState(ms);
			n.attachChild(b1);
		}
		if ((orient & Map.ORIENT_N) != 0) {
			b2 = new Box("kranz", new Vector3f(0, 0 + blocksize, blocksize), blocksize, 0.1f, blocksize);

			b2.setRenderState(ts);
			b2.setRenderState(ms);
			n.attachChild(b2);
		}
		if ((orient & Map.ORIENT_E) != 0) {
			b3 = new Box("kranz", new Vector3f(0 + blocksize, 0, blocksize), 0.1f, blocksize, blocksize);

			b3.setRenderState(ts);
			b3.setRenderState(ms);
			n.attachChild(b3);
		}
		if ((orient & Map.ORIENT_W) != 0) {
			b4 = new Box("kranz", new Vector3f(0 - blocksize, 0, blocksize), 0.1f, blocksize, blocksize);

			b4.setRenderState(ts);
			b4.setRenderState(ms);
			n.attachChild(b4);
		}

		this.n.setLocalTranslation(new Vector3f(x * blocksize * 2, y * blocksize * 2, -0.05f));
	}
}
