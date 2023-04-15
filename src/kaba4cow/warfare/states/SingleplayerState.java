package kaba4cow.warfare.states;

import java.io.File;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;

public class SingleplayerState extends AbstractState {

	private static final SingleplayerState instance = new SingleplayerState();

	private World world;

	public SingleplayerState() {

	}

	public static SingleplayerState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (world.canExit() && Input.isKeyDown(Input.KEY_ESCAPE))
			Game.switchState(SingleplayerPauseState.getInstance());

		world.update(dt);
	}

	@Override
	public void render() {
		world.render();

		if (!world.isPlayerTurn() && !world.isGameOver()) {
			progressBar.setTitle("Opponent's turn");
			AbstractState.renderProgressBar();
		}
	}
	
	@Override
	public void onLostFocus() {
		Game.switchState(SingleplayerPauseState.getInstance());
	}

	public void generateWorld() {
		AbstractState.thread("Generating", f -> {
			world = new World();
			world.setCurrentPlayer(0, true);
			Game.switchState(instance);
		});
	}

	public void saveWorld() {
		AbstractState.thread("Saving", f -> {
			world.save();
			Game.message("Game saved");
		});
	}

	public void loadWorld() {
		File file = new File("save");
		if (!file.exists()) {
			Game.message("No save found");
			return;
		}

		AbstractState.thread("Loading", f -> {
			World newWorld;
			try {
				newWorld = new World(DataFile.read(file), -1);
			} catch (Exception e) {
				newWorld = null;
			}
			if (newWorld != null) {
				world = newWorld;
				Game.switchState(instance);
			}
		});
	}

}
