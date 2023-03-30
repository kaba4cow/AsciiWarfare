package kaba4cow.warfare.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.Message;
import kaba4cow.warfare.states.MultiplayerState;

public class Client implements Runnable {

	private final Socket client;

	private int id;

	private final BufferedReader reader;
	private final BufferedWriter writer;

	private Thread thread;

	private final MultiplayerState game;
	private World world;

	public Client(MultiplayerState game, String address, int port) throws IOException {
		game.setClient(this);
		this.game = game;
		this.client = new Socket(address, port);

		this.id = -1;

		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		this.thread = new Thread(this, "Client");
		this.thread.start();
	}

	@Override
	public void run() {
		String message;
		while (!isClosed()) {
			try {
				Thread.sleep(50l);
			} catch (InterruptedException e) {
			}

			try {
				message = reader.readLine();
				if (message == null)
					continue;
				Printer.println("Received: " + message);
				if (message.length() > 1) {
					String[] parameters = message.substring(1).split(",");
					message = message.substring(0, 1);
					process(message, parameters);
				} else
					process(message);
			} catch (IOException e) {
			}
		}
	}

	private void process(String message, String... parameters) {
		if (message.startsWith(Message.CONNECT)) {
			id = Integer.parseInt(parameters[0]);
			long seed = Long.parseLong(parameters[1]);
			float size = Float.parseFloat(parameters[2]);
			int season = Integer.parseInt(parameters[3]);
			game.generateWorld(seed, size, season, id);
		} else if (message.startsWith(Message.DISCONNECT)) {
			close();
		} else if (message.startsWith(Message.TURN)) {
			world.newTurn(world.getEnemyPlayer(), false);
		} else if (message.startsWith(Message.MOVE)) {
			int index = Integer.parseInt(parameters[0]);
			int x = Integer.parseInt(parameters[1]);
			int y = Integer.parseInt(parameters[2]);
			world.moveUnit(index, x, y, false);
		} else if (message.startsWith(Message.PROJECTILE)) {
			int index = Integer.parseInt(parameters[0]);
			int weapon = Integer.parseInt(parameters[1]);
			int x = Integer.parseInt(parameters[2]);
			int y = Integer.parseInt(parameters[3]);
			world.createProjectile(index, weapon, x, y, false);
		}
	}

	public synchronized void send(String message, Object... parameters) {
		Message.send(writer, message, parameters);
	}

	public synchronized void close() {
		if (isClosed())
			return;
		try {
			Printer.println("Client closing");
			send(Message.DISCONNECT);
			try {
				Thread.sleep(500l);
			} catch (InterruptedException e) {
			}
			client.close();
			reader.close();
			writer.close();
			Printer.println("Client closed");
		} catch (IOException e) {
		}
	}

	public boolean isClosed() {
		return client.isClosed();
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public int getID() {
		return id;
	}

}
