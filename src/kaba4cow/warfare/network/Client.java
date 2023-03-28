package kaba4cow.warfare.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;

public class Client {

	private final int port;
	private final DatagramSocket socket;
	private final InetAddress address;

	private Thread receiveThread;

	private int id;
	private int attempt;

	private Game game;
	private World world;

	public Client(Game game, String ip, int port) throws IOException {
		this.game = game;
		this.port = port;
		this.address = InetAddress.getByName(ip);
		this.socket = new DatagramSocket();
		this.id = -1;
		this.attempt = 0;
	}

	public Client(InetAddress address, int port, int id) {
		this.port = port;
		this.address = address;
		this.socket = null;
		this.id = id;
		this.attempt = 0;
	}

	public void connect() throws IOException {
		send(Message.CONNECT.getBytes());
		receiveThread();

		try {
			Thread.sleep(Server.MAX_ATTEMPTS * 1000l);
			if (id == -1) {
				close(false);
				throw new IOException("Server not responding");
			}
		} catch (InterruptedException e) {
		}
	}

	private void receiveThread() {
		new Thread("Receive") {
			@Override
			public void run() {
				String message;
				while (true) {
					try {
						Thread.sleep(10l);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					message = receive();
					process(message);
				}
			}
		}.start();
	}

	private void process(String message) {
		if (message.startsWith(Message.CONNECT) && id == -1) {
			String[] strings = Message.getString(message).split("/");
			id = Integer.parseInt(strings[0]);
			Printer.println("Connected to server [" + id + "]");
			long seed = Long.parseLong(strings[1]);
			float size = Float.parseFloat(strings[2]);
			int season = Integer.parseInt(strings[3]);
			game.generateWorld(size, season, seed, id);
		} else if (message.startsWith(Message.PING)) {
			send(Message.getBytes(Message.PING, id));
		} else if (message.startsWith(Message.DISCONNECT)) {
			close(false);
		} else if (message.startsWith(Message.PROJECTILE)) {
			String[] parameters = Message.getString(message).split("/");
			int id = Integer.parseInt(parameters[0]);
			if (id != this.id) {
				int index = Integer.parseInt(parameters[1]);
				int weapon = Integer.parseInt(parameters[2]);
				int x = Integer.parseInt(parameters[3]);
				int y = Integer.parseInt(parameters[4]);
				world.createProjectile(index, weapon, x, y, false);
			}
		} else if (message.startsWith(Message.MOVE)) {
			String[] parameters = Message.getString(message).split("/");
			int id = Integer.parseInt(parameters[0]);
			if (id != this.id) {
				int index = Integer.parseInt(parameters[1]);
				int x = Integer.parseInt(parameters[2]);
				int y = Integer.parseInt(parameters[3]);
				world.moveUnit(index, x, y, false);
			}
		} else if (message.startsWith(Message.TURN)) {
			if (Message.getInt(message) != this.id)
				world.newTurn(world.getEnemyPlayer(), false);
		}
	}

	public String receive() {
		final byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
		}
		String message = new String(packet.getData()).trim();
		Printer.println("RECEIVED: " + message);
		return message;
	}

	public void send(final byte[] data) {
		Printer.println("SENDING: " + new String(data));
		new Thread("Send") {
			@Override
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
				}
			}
		}.start();
	}

	public void close(boolean manual) {
		if (manual)
			send(Message.getBytes(Message.DISCONNECT, id));
		if (receiveThread != null)
			receiveThread.interrupt();
		synchronized (socket) {
			socket.close();
		}
	}

	public int getPort() {
		return port;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getID() {
		return id;
	}

	public int getAttempt() {
		return attempt;
	}

	public void nextAttempt() {
		attempt++;
	}

	public void resetAttempt() {
		attempt = 0;
	}

	public void setWorld(World world) {
		this.world = world;
	}

}
