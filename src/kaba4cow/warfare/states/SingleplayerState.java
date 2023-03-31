package kaba4cow.warfare.states;

import kaba4cow.ascii.input.Keyboard;
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
		if (!world.inShop() && world.isPlayerTurn() && Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Game.switchState(SingleplayerPauseState.getInstance());

		world.update(dt);
	}

	@Override
	public void render() {
		world.render();

		if (!world.isPlayerTurn()) {
			PROGRESS = 0f;
			progressBar.setTitle("Waiting for Player " + (world.getTurnPlayer() + 1));
			State.renderProgressBar();
		}
	}

	public void generateWorld(int size, int season) {
		State.thread("Generating", f -> {
			world = new World(size, season);
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
				newWorld = new World();
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
