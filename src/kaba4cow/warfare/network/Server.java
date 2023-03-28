package kaba4cow.warfare.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.ascii.toolbox.rng.RNG;

public class Server {

	public static final int MIN_PORT = 1024;
	public static final int MAX_PORT = 49151;

	public static final int MAX_ATTEMPTS = 6;

	private final DatagramSocket socket;

	private final ArrayList<Client> clients;
	private final ArrayList<Integer> response;

	private Thread pingThread;
	private Thread receiveThread;

	private final long worldSeed;
	private final float worldSize;
	private final int worldSeason;

	public Server(int port, float size, int season) throws IOException {
		this.socket = new DatagramSocket(port);
		this.clients = new ArrayList<>();
		this.response = new ArrayList<>();

		this.worldSeed = RNG.randomLong();
		this.worldSize = size;
		this.worldSeason = season;

		clientThread();
		receiveThread();
	}

	public void close() {
		if (pingThread != null)
			pingThread.interrupt();
		if (receiveThread != null)
			receiveThread.interrupt();
		synchronized (socket) {
			socket.close();
		}
	}

	private void clientThread() {
		pingThread = new Thread("Ping") {
			@Override
			public void run() {
				while (true) {
					sendToAll(Message.PING);
					try {
						Thread.sleep(1000l);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					for (int i = clients.size() - 1; i >= 0; i--) {
						Client client = clients.get(i);
						if (!response.contains(client.getID())) {
							if (client.getAttempt() >= MAX_ATTEMPTS)
								disconnect(Integer.toString(client.getID()), false);
							else
								client.nextAttempt();
						} else {
							response.remove(i);
							client.resetAttempt();
						}
					}
				}
			}
		};
		pingThread.start();
	}

	private void receiveThread() {
		receiveThread = new Thread("Receive") {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10l);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receiveThread.start();
	}

	private void process(DatagramPacket packet) {
		String string = new String(packet.getData()).trim();
		Printer.println("RECEIVED: " + string);
		if (string.startsWith(Message.CONNECT))
			connect(string, packet);
		else if (string.startsWith(Message.DISCONNECT))
			disconnect(string, true);
		else if (string.startsWith(Message.PING))
			response(string);
		else if (string.startsWith(Message.PROJECTILE) || string.startsWith(Message.MOVE)
				|| string.startsWith(Message.TURN)) {
			Printer.println(string);
			String[] parameters = Message.getString(string).split("/");
			int id = Integer.parseInt(parameters[0]);
			for (int i = 0; i < clients.size(); i++)
				if (clients.get(i).getID() != id)
					sendByID(string, clients.get(i).getID());
		}
	}

	private void response(String string) {
		response.add(Message.getInt(string));
	}

	private void connect(String string, DatagramPacket packet) {
//		if (clients.size() >= 2) {
//			send(Message.getBytes(Message.DISCONNECT), packet.getAddress(), packet.getPort());
//			return;
//		}
		int id = clients.size();
		Client client = new Client(packet.getAddress(), packet.getPort(), id);
		clients.add(client);
		send(Message.getBytes(Message.CONNECT, id, "/", worldSeed, "/", worldSize, "/", worldSeason), client);
		Printer.println("Client connected [" + id + "]");
	}

	private void disconnect(String string, boolean manual) {
		int id = Message.getInt(string);
		for (int i = clients.size() - 1; i >= 0; i--) {
			Client client = clients.get(i);
			if (client.getID() == id) {
				for (int j = response.size() - 1; j >= 0; j--)
					if (response.get(j) == id)
						response.remove(j);
				clients.remove(i);
				if (manual) {
					Printer.println("Client disconnected [" + id + "]");
				} else {
					send(Message.DISCONNECT.getBytes(), client);
					Printer.println("Client timed out [" + id + "]");
				}
				break;
			}
		}
	}

	private void sendToAll(String message) {
		final byte[] data = message.getBytes();
		for (int i = clients.size() - 1; i >= 0; i--) {
			Client client = clients.get(i);
			send(data, client);
		}
	}

	private void sendByID(String message, int id) {
		for (int i = 0; i < clients.size(); i++)
			if (clients.get(i).getID() == id)
				send(message.getBytes(), clients.get(i));
	}

	private void send(final byte[] data, Client client) {
		send(data, client.getAddress(), client.getPort());
	}

	private void send(final byte[] data, InetAddress address, int port) {
		Printer.println("SENDING: " + new String(data));
		new Thread("Send") {
			@Override
			public void run() {
				DatagramPacket p = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public int getPort() {
		return socket.getPort();
	}

	public ArrayList<Client> getClients() {
		return clients;
	}

}
