package kaba4cow.warfare.network.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.network.Message;

public class Server implements Runnable {

	private final ServerSocket server;

	private final ArrayList<Connection> clients;
	private final LinkedList<Integer> ids;

	private Thread thread;

	private final World world;

	private final StringBuilder output;

	public Server(int port, int season) throws IOException {
		this.server = new ServerSocket(port);
		this.output = new StringBuilder();
		log("Server started on port " + server.getLocalPort());

		this.world = new World(season, RNG.randomLong());

		this.clients = new ArrayList<>();
		this.ids = new LinkedList<>();
		ids.add(0);
		ids.add(1);

		this.thread = new Thread(this, "Server");
		this.thread.start();
	}

	public void update(float dt) {
		world.update(dt);
	}

	@Override
	public void run() {
		while (!isClosed()) {
			try {
				Socket socket = server.accept();
				if (ids.isEmpty()) {
					new Connection(Server.this, socket, -1);
				} else {
					Connection client = new Connection(Server.this, socket, ids.removeFirst());
					clients.add(client);
				}
			} catch (IOException e) {
			}
		}
	}

	public synchronized void send(Connection sender, String message) {
		if (message == null)
			return;
		log("Received: " + message);
		process(message);
		for (Connection client : clients)
			if (client != sender)
				client.send(message);
	}

	private void process(String message) {
		if (message.length() <= 1)
			return;
		String[] parameters = Message.getParameters(message);
		message = Message.getMessage(message);
		if (message.equals(Message.TURN)) {
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
		} else if (message.equals(Message.CASH)) {
			int player = Integer.parseInt(parameters[0]);
			int cash = Integer.parseInt(parameters[1]);
			world.getPlayer(player).setCash(cash);
		} else if (message.equals(Message.UNIT)) {
			int player = Integer.parseInt(parameters[0]);
			String unit = parameters[1];
			int x = Integer.parseInt(parameters[2]);
			int y = Integer.parseInt(parameters[3]);
			if (x != -1 && y != -1)
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

	public ArrayList<Connection> getClients() {
		return clients;
	}

	public synchronized void removeClient(Connection client) {
		if (client == null || !clients.contains(client))
			return;
		ids.add(client.getID());
		Collections.sort(ids);
		clients.remove(client);
		client.send(Message.DISCONNECT);
		client.close();
	}

	public synchronized void close() {
		if (isClosed())
			return;
		for (int i = clients.size() - 1; i >= 0; i--)
			clients.get(i).close();
		try {
			server.close();
			log("Server closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DataFile getWorldData(int id) {
		DataFile data = world.getDataFile();
		world.setCurrentPlayer(id);
		return data;
	}

	public synchronized boolean isClosed() {
		return server.isClosed();
	}

	public void log(String message) {
		output.append(message);
		output.append('\n');
		Printer.println(message);
	}

	public String getOutput() {
		return output.toString();
	}

}
