package kaba4cow.warfare.network.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.network.Message;

public class Server implements Runnable {

	public static final int MIN_PORT = 1024;
	public static final int MAX_PORT = 49151;

	public static final int MAX_ATTEMPTS = 6;

	private final ServerSocket server;

	private final ArrayList<Connection> clients;
	private final LinkedList<Integer> ids;

	private Thread thread;

	private final long worldSeed;
	private final float worldSize;
	private final int worldSeason;

	public Server(int port, float worldSize, int worldSeason) throws IOException {
		this.server = new ServerSocket(port);

		this.worldSeed = RNG.randomLong();
		this.worldSize = worldSize;
		this.worldSeason = worldSeason;

		this.clients = new ArrayList<>();
		this.ids = new LinkedList<>();
		ids.add(0);
		ids.add(1);

		this.thread = new Thread(this, "Server");
		this.thread.start();
	}

	@Override
	public void run() {
		while (!isClosed()) {
			try {
				Thread.sleep(50l);
			} catch (InterruptedException e) {
			}

			try {
				Socket socket = server.accept();
				if (ids.isEmpty()) {
					new Connection(Server.this, socket, -1);
					Printer.println("Connection rejected");
				} else {
					Connection client = new Connection(Server.this, socket, ids.removeFirst());
					clients.add(client);
					Printer.println("Connected [" + client.getID() + "] : " + socket.getInetAddress().getHostAddress());
				}
			} catch (IOException e) {
			}
		}
	}

	public synchronized void send(Connection sender, String message) {
		if (message == null)
			return;
		for (Connection client : clients)
			if (client != sender)
				client.send(message);
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
			Printer.println("Server closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getWorldSeed() {
		return worldSeed;
	}

	public float getWorldSize() {
		return worldSize;
	}

	public int getWorldSeason() {
		return worldSeason;
	}

	public synchronized boolean isClosed() {
		return server.isClosed();
	}

}
