package kaba4cow.warfare.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import kaba4cow.ascii.toolbox.Printer;

public class Server implements Runnable {

	public static final String ADDRESS_REGEX = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])" + "\\."
			+ "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])" + "\\." + "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])" + "\\."
			+ "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])" + ":{1}\\d{4,5}";

	public static final int MIN_PORT = 1024;
	public static final int MAX_PORT = 49151;

	public static final int MAX_ATTEMPTS = 5;

	private final DatagramSocket socket;

	private final ArrayList<Client> clients;
	private final ArrayList<Integer> response;

	private AtomicBoolean running;

	private Thread clientThread;
	private Thread sendThread;
	private Thread receiveThread;

	public Server(int port) throws IOException {
		this.socket = new DatagramSocket(port);
		this.clients = new ArrayList<>();
		this.response = new ArrayList<>();
		this.running = new AtomicBoolean(false);
		new Thread(this, "Server").start();
	}

	@Override
	public void run() {
		running.set(true);
		clientThread();
		receiveThread();
		while (running.get()) {
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
			}
		}
	}

	public void close() {
		running.set(false);
		if (clientThread != null)
			clientThread.interrupt();
		if (receiveThread != null)
			receiveThread.interrupt();
		if (sendThread != null)
			sendThread.interrupt();
		synchronized (socket) {
			socket.close();
		}
	}

	private void clientThread() {
		clientThread = new Thread("Ping") {
			@Override
			public void run() {
				while (running.get()) {
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
		clientThread.start();
	}

	private void receiveThread() {
		byte[] data = new byte[1024];
		receiveThread = new Thread("Receive") {
			@Override
			public void run() {
				while (running.get()) {
					try {
						Thread.sleep(10l);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
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
		String string = new String(packet.getData());
		if (string.startsWith(Message.CONNECT))
			connect(string, packet);
		else if (string.startsWith(Message.DISCONNECT))
			disconnect(string, true);
		else if (string.startsWith(Message.PING))
			response(string);
	}

	private void response(String string) {
		response.add(Message.getInt(string));
	}

	private void connect(String string, DatagramPacket packet) {
		int id = clients.size();
		Client client = new Client(packet.getAddress(), packet.getPort(), id);
		clients.add(client);
		send(Message.getBytes(Message.CONNECT, id), client);
		Printer.println("Client connected [" + id + "]");
	}

	private void disconnect(String string, boolean manual) {
		int id = Message.getInt(string);
		for (int i = clients.size() - 1; i >= 0; i--) {
			Client client = clients.get(i);
			if (client.getID() == id) {
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

	private void send(final byte[] data, Client client) {
		send(data, client.getAddress(), client.getPort());
	}

	private void send(final byte[] data, InetAddress address, int port) {
		sendThread = new Thread("Send") {
			@Override
			public void run() {
				DatagramPacket p = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		sendThread.start();
	}

	public int getPort() {
		return socket.getPort();
	}

	public ArrayList<Client> getClients() {
		return clients;
	}

}
