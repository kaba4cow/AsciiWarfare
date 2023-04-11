package kaba4cow.warfare.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.Message;
import kaba4cow.warfare.states.MultiplayerState;

public class Client implements Runnable {

	private final Socket client;

	private int id;

	private final BufferedReader reader;
	private final BufferedWriter writer;

	private final MultiplayerState game;
	private World world;

	private StringBuilder worldBuilder;

	public Client(MultiplayerState game, String address, int port) throws IOException {
		game.setClient(this);
		this.game = game;
		this.client = new Socket(address, port);

		this.id = -1;
		this.worldBuilder = null;

		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		new Thread(this, "Client").start();
		new Thread("Ping") {
			@Override
			public void run() {
				ping();
			}
		}.start();
	}

	private void ping() {
		while (!isClosed()) {
			try {
				Thread.sleep(2000l);
			} catch (InterruptedException e) {
			}
			synchronized (writer) {
				send(Message.PING);
			}
		}
	}

	@Override
	public void run() {
		String message;
		while (!isClosed()) {
			try {
				Thread.sleep(10l);
			} catch (InterruptedException e) {
			}

			try {
				message = reader.readLine();
				if (message == null)
					continue;
				Printer.println("Received: " + message);
				if (message.length() > 1) {
					String[] parameters = Message.getParameters(message);
					message = Message.getMessage(message);
					process(message, parameters);
				} else
					process(message);
			} catch (IOException e) {
			}
		}
	}

	private void process(String message, String... parameters) {
		if (message.equals(Message.CONNECT)) {
			id = Integer.parseInt(parameters[0]);
		} else if (message.equals(Message.WORLD)) {
			if (worldBuilder == null) {
				worldBuilder = new StringBuilder();
			} else if (parameters[0].equals(Message.EOF)) {
				DataFile data = Message.decompressData(worldBuilder);
				game.generateWorld(data, id);
				worldBuilder = null;
			} else {
				worldBuilder.append(parameters[0]);
			}
		} else if (message.equals(Message.DISCONNECT)) {
			close(true);
		} else if (message.equals(Message.TURN)) {
			int player = Integer.parseInt(parameters[0]);
			world.newTurn(player, false);
		} else if (message.equals(Message.MOVE)) {
			int player = Integer.parseInt(parameters[0]);
			int index = Integer.parseInt(parameters[1]);
			int x = Integer.parseInt(parameters[2]);
			int y = Integer.parseInt(parameters[3]);
			world.moveUnit(player, index, x, y, false);
		} else if (message.equals(Message.PROJECTILE)) {
			int player = Integer.parseInt(parameters[0]);
			int index = Integer.parseInt(parameters[1]);
			int weapon = Integer.parseInt(parameters[2]);
			int x = Integer.parseInt(parameters[3]);
			int y = Integer.parseInt(parameters[4]);
			long seed = Long.parseLong(parameters[5]);
			world.createProjectile(player, index, weapon, x, y, seed, false);
		} else if (message.equals(Message.UNIT)) {
			int player = Integer.parseInt(parameters[0]);
			String unit = parameters[1];
			int x = Integer.parseInt(parameters[2]);
			int y = Integer.parseInt(parameters[3]);
			world.addUnit(player, unit, x, y, false);
		} else if (message.equals(Message.STATS)) {
			int player = Integer.parseInt(parameters[0]);
			float level = Float.parseFloat(parameters[1]);
			int cashEarned = Integer.parseInt(parameters[2]);
			int cashSpent = Integer.parseInt(parameters[3]);
			int unitsHired = Integer.parseInt(parameters[4]);
			int unitsLost = Integer.parseInt(parameters[5]);
			int unitsKilled = Integer.parseInt(parameters[6]);
			world.setStats(player, level, cashEarned, cashSpent, unitsHired, unitsLost, unitsKilled, false);
		} else if (message.equals(Message.JOIN)) {
			int player = Integer.parseInt(parameters[0]);
			int index1 = Integer.parseInt(parameters[1]);
			int index2 = Integer.parseInt(parameters[2]);
			world.joinUnits(player, index1, index2, false);
		}
	}

	public synchronized void send(String message, Object... parameters) {
		String output = Message.send(writer, message, parameters);
		if (output == null)
			close(false);
		else
			Printer.println("Sent: " + output);
	}

	public synchronized void close(boolean manual) {
		if (isClosed())
			return;
		try {
			Printer.println("Client closing");
			if (manual)
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
