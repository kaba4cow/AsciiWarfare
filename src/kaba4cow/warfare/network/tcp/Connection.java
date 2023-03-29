package kaba4cow.warfare.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import kaba4cow.ascii.toolbox.Printer;
import kaba4cow.warfare.network.Message;

public class Connection implements Runnable {

	private final Server server;
	private final Socket client;

	private final int id;

	private final BufferedReader reader;
	private final BufferedWriter writer;

	private Thread thread;

	public Connection(Server server, Socket client, int id) throws IOException {
		this.server = server;
		this.client = client;

		this.id = id;

		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		this.thread = new Thread(this, "Client #" + id);
		this.thread.start();

		if (id == -1) {
			send(Message.DISCONNECT);
			close();
		} else {
			send(Message.CONNECT, id, server.getWorldSeed(), server.getWorldSize(), server.getWorldSeason());
		}
	}

	@Override
	public void run() {
		String message;
		while (!isClosed()) {
			try {
				message = reader.readLine();
				if (message != null) {
					if (message.startsWith(Message.DISCONNECT))
						close();
					else
						server.send(this, message);
				}
			} catch (IOException e) {
				close();
				break;
			}
		}
	}

	public synchronized void send(String message, Object... parameters) {
		Message.send(writer, message, parameters);
	}

	public synchronized void close() {
		if (isClosed())
			return;
		try {
			client.close();
			reader.close();
			writer.close();
			Printer.println("Connection [" + id + "] closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.removeClient(this);
	}

	public boolean isClosed() {
		return client.isClosed();
	}

	public InetAddress getAddress() {
		return client.getInetAddress();
	}

	public int getID() {
		return id;
	}

}
