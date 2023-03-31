package kaba4cow.warfare.states;

import java.io.IOException;

import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.tcp.Client;

public class MultiplayerState extends State {

	private static final MultiplayerState instance = new MultiplayerState();

	private World world;
	private Client client;

	public MultiplayerState() {

	}

	public static MultiplayerState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (!world.inShop() && Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Game.switchState(MultiplayerPauseState.getInstance());

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

	public void generateWorld(long seed, int size, int season, int id) {
		State.thread("Generating", f -> {
			world = new World(size, season, seed);
			client.setWorld(world);
			world.setClient(client);
			world.setCurrentPlayer(id, false);
			Game.switchState(instance);
		});
	}

	public void generateWorld(DataFile data, int id) {
		State.thread("Generating", f -> {
			world = new World(data, id);
			client.setWorld(world);
			world.setClient(client);
			Game.switchState(instance);
		});
	}

	public void connect(String ip, int port) {
		State.thread("Connecting", f -> {
			try {
				new Client(this, ip, port);
			} catch (IOException e) {
				setClient(null);
			}
		});
	}

	public void disconnect() {
		State.thread("Disconnecting", f -> {
			client.close();
			Game.switchState(MenuState.getInstance());
		});
	}

	public void setClient(Client client) {
		this.client = client;
	}

}
