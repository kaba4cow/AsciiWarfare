package kaba4cow.warfare.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import kaba4cow.ascii.toolbox.Printer;

public class Client implements Runnable {

	private final int port;
	private final DatagramSocket socket;
	private final InetAddress address;

	private AtomicBoolean running;

	private int id;
	private int attempt;

	private byte[] data;

	public Client(String ip, int port) throws IOException {
		this.port = port;
		this.address = InetAddress.getByName(ip);
		this.socket = new DatagramSocket();
		this.running = new AtomicBoolean(false);
		this.id = -1;
		this.attempt = 0;
		this.data = new byte[1024];
		send(Message.CONNECT.getBytes());
		new Thread(this, "Client").start();
	}

	public Client(InetAddress address, int port, int id) {
		this.port = port;
		this.address = address;
		this.socket = null;
		this.running = new AtomicBoolean(false);
		this.id = id;
		this.attempt = 0;
		this.data = new byte[1024];
	}

	@Override
	public void run() {
		running.set(true);
		String message;
		while (running.get()) {
			try {
				Thread.sleep(10l);
			} catch (InterruptedException e) {
				break;
			}
			message = receive();
			if (message.startsWith(Message.CONNECT) && id == -1) {
				id = Message.getInt(message);
				Printer.println("Connected to server [" + id + "]");
			} else if (message.startsWith(Message.PING))
				send(Message.getBytes(Message.PING, id));
			else if (message.startsWith(Message.DISCONNECT))
				close();
		}
	}

	public String receive() {
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
		}
		String message = new String(packet.getData());
		return message;
	}

	public void send(final byte[] data) {
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

	public void close() {
		running.set(false);
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

	public static void main(String[] args) {
		try {
			new Client("localhost", 5000);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Scanner scanner = new Scanner(System.in);
//		Printer.println("Address: ");
//		String string;
//		while (true) {
//			string = scanner.nextLine();
//
//			if (string.startsWith("localhost") || string.matches(Server.ADDRESS_REGEX))
//				break;
//			else
//				Printer.println("Invalid address format");
//		}
//		scanner.close();
//
//		String[] address = string.split(":");
//		int port;
//		try {
//			port = Integer.parseInt(address[1]);
//		} catch (NumberFormatException e) {
//			Printer.println("Invalid port");
//			return;
//		}
//
//		try {
//			new Client(address[0], port);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
