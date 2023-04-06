package kaba4cow.warfare.states;

import java.io.File;

import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;

public class SingleplayerState extends State {

	private static final SingleplayerState instance = new SingleplayerState();

	private World world;

	public SingleplayerState() {

	}

	public static SingleplayerState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (world.canExit() && Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Game.switchState(SingleplayerPauseState.getInstance());

		world.update(dt);
	}

	@Override
	public void render() {
		world.render();

		if (!world.isPlayerTurn() && !world.isGameOver()) {
			progressBar.setTitle("Opponent's turn");
			State.renderProgressBar();
		}
	}

	public void generateWorld(int season) {
		State.thread("Generating", f -> {
			world = new World(season, RNG.randomLong());
			world.setCurrentPlayer(0, true);
			Game.switchState(instance);
		});
	}

	public void saveWorld() {
		State.thread("Saving", f -> {
			world.save();
		});
	}

	public void loadWorld() {
		State.thread("Loading", f -> {
			World newWorld;
			try {
				newWorld = new World(DataFile.read(new File("SAVE")), -1);
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
