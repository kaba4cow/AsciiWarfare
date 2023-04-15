package kaba4cow.warfare.states;

import java.io.IOException;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.tcp.Client;

public class MultiplayerState extends AbstractState {

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
		if (world.canExit() && Input.isKeyDown(Input.KEY_ESCAPE))
			Game.switchState(MultiplayerPauseState.getInstance());

		world.update(dt);

		if (client.isClosed()) {
			disconnect();
			Game.message("Connection lost");
		}
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
		Game.switchState(MultiplayerPauseState.getInstance());
	}

	public void generateWorld(DataFile data, int id) {
		AbstractState.thread("Generating", f -> {
			world = new World(data, id);
			client.setWorld(world);
			world.setClient(client);
			Game.switchState(instance);
		});
	}

	public void connect(String ip, int port) {
		AbstractState.thread("Connecting", f -> {
			try {
				new Client(this, ip, port);
			} catch (IOException e) {
				setClient(null);
				Game.message("Connection failed");
			}
		});
	}

	public void disconnect() {
		AbstractState.thread("Disconnecting", f -> {
			client.close(true);
			Game.switchState(MenuState.getInstance());
		});
	}

	public void setClient(Client client) {
		this.client = client;
	}

}
