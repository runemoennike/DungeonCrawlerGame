/**
 * This is the Main class of the game. It extends the jME class BaseGame and contains the most
 * basic update and render methods in our code. It also contains timers and manages the Game 
 * and loading screen.
 */

package engine;

import infostore.DataManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.app.AbstractGame;
import com.jme.app.BaseGame;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.joystick.JoystickInput;
import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.Timer;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.LoadingGameState;
import com.jmex.game.state.load.TransitionGameState;

import engine.Settings.Fields;
import engine.hud.ConsoleLog;
import engine.hud.HUD;
import engine.hud.MapOverlayState;

public class Main extends BaseGame {
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	private static AbstractGame instance;

	private Timer timer;
	private float tpf;
	private boolean isLoading = true;

	private LoadingGameState loadingState;
	private Game gameState;
	private HUD hudState;
	private MapOverlayState mapOverlayState;

	private Camera cam;

	public static void main(String[] args) {
		System.out.println("Starting...");
		Profiler.enable();
		Profiler.init();
		// Profiler.disable();
		ConsoleLog.init();

		ConsoleLog.addLine("DCG (Java " + System.getProperty("java.version") + " on " + System.getProperty("os.name")
				+ ", " + System.getProperty("os.arch") + ", " + System.getProperty("os.version") + ")");

		DataManager.load();

		Settings.init();
		Settings.load();
		Settings.dump();

		ConsoleLog.addLine("Settings loaded.");

		Main app = new Main();

		app.setConfigShowMode(ConfigShowMode.NeverShow);
		app.start();
	}

	protected final void update(float interpolation) {
		Profiler.start("Main.update");

		timer.update();
		tpf = timer.getTimePerFrame();

		if (this.isLoading) {
			if (this.loadingState == null) {
				this.loadingState = new TransitionGameState(Game.class.getClassLoader().getResource(
						"dcg/data/images/logo.jpg"));
				GameStateManager.getInstance().attachChild(this.loadingState);

				this.loadingState.setActive(true);
				this.loadingState.setProgress(0.0f, "Loading...");
			} else if (this.gameState == null) {
				this.gameState = new Game();
				this.gameState.prepareLoad();
			} else {
				this.gameState.load();
				this.loadingState.setProgress((float) this.gameState.getLoadProgress() / 100.0f, this.gameState
						.getLoadText());

				if (this.gameState.getLoadProgress() == 100) {
					GameStateManager.getInstance().attachChild(this.gameState);
					this.gameState.setActive(true);
					this.isLoading = false;

					if (this.hudState == null) {
						this.hudState = new HUD(this.gameState);
						GameStateManager.getInstance().attachChild(this.hudState);
					}

					if (this.mapOverlayState == null) {
						this.mapOverlayState = new MapOverlayState(this.gameState);
						GameStateManager.getInstance().attachChild(this.mapOverlayState);
					}

					this.hudState.setActive(true);
					this.mapOverlayState.setActive(false);
					this.loadingState.setActive(false);

					this.gameState.firstTime();

					ConsoleLog.addLine("Map loaded, game started.");
				}
			}
		}

		Profiler.start("Main.update.GameStateManager");
		GameStateManager.getInstance().update(tpf);
		Profiler.stop("Main.update.GameStateManager");

		Profiler.stop("Main.update");

		Profiler.lap();
	}

	protected final void render(float interpolation) {
		Profiler.start("Main.render");

		display.getRenderer().clearBuffers();
		GameStateManager.getInstance().render(tpf);

		Profiler.stop("Main.render");
	}

	protected final void initSystem() {
		this.settings.setFullscreen(Settings.get(Fields.SCR_FULL).b);
		this.settings.setWidth(Settings.get(Fields.SCR_W).i);
		this.settings.setHeight(Settings.get(Fields.SCR_H).i);
		this.settings.setFrequency(Settings.get(Fields.SCR_FREQ).i);
		this.settings.setDepth(Settings.get(Fields.SCR_BITS).i);

		try {
			display = DisplaySystem.getDisplaySystem(settings.getRenderer());

			display.createWindow(settings.getWidth(), settings.getHeight(), settings.getDepth(), settings
					.getFrequency(), settings.isFullscreen());
		} catch (JmeException e) {
			logger.log(Level.SEVERE, "Could not create display system", e);
			System.exit(1);
		}

		this.cam = display.getRenderer().createCamera(display.getWidth(), display.getHeight());

		display.getRenderer().setCamera(cam);

		timer = Timer.getTimer();
	}

	protected final void initGame() {
		instance = this;
		display.setTitle("Dungeon Crawler Game");

		GameStateManager.create();
	}

	protected void reinit() {
	}

	protected void cleanup() {
		logger.info("Cleaning up resources.");

		GameStateManager.getInstance().cleanup();

		KeyInput.destroyIfInitalized();
		MouseInput.destroyIfInitalized();
		JoystickInput.destroyIfInitalized();

		Profiler.dumpTotals();
	}

	public static void exit() {
		Settings.save();
		Game.getInstance().savePlayer();
		instance.finish();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public static Main getInstance() {
		return (Main) instance;
	}

	public void toggleLoadingScreen(boolean on, String caption) {
		GameStateManager.getInstance().detachChild(this.loadingState);
		GameStateManager.getInstance().attachChild(this.loadingState);
		this.loadingState.setProgress(0.5f, caption);
		this.loadingState.setActive(on);
	}
}
