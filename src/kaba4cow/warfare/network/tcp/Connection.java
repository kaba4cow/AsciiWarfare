package kaba4cow.warfare.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import kaba4cow.warfare.network.Message;

public class Connection {

	private final Server server;
	private final Socket client;

	private final int id;

	private final BufferedReader reader;
	private final BufferedWriter writer;

	public Connection(Server server, Socket client, int id) throws IOException {
		this.server = server;
		this.client = client;

		this.id = id;

		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		new Thread("Server-Connection[" + id + "]-Listen") {
			@Override
			public void run() {
				listen();
			}
		}.start();
		new Thread("Server-Connection[" + id + "]-Ping") {
			@Override
			public void run() {
				ping();
			}
		}.start();

		if (id == -1) {
			server.log("Connection rejected: " + client.getInetAddress().getHostAddress());
			send(Message.DISCONNECT);
			close();
		} else {
			server.log("Connected [" + id + "] : " + client.getInetAddress().getHostAddress());
			send(Message.CONNECT, id);
			send(Message.WORLD);
			String[] parts = Message.compressData(server.getWorldData(id));
			for (int i = 0; i < parts.length; i++)
				send(Message.WORLD, parts[i]);
			send(Message.WORLD, Message.EOF);
		}
	}

	private void ping() {
		while (!isClosed()) {
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
			}
			synchronized (writer) {
				send(Message.PING);
			}
		}
	}

	private void listen() {
		String message;
		while (!isClosed()) {
			try {
				message = reader.readLine();
				if (message == null || message.startsWith(Message.DISCONNECT))
					close();
				else
					server.send(this, message);
			} catch (IOException e) {
				close();
				break;
			}
		}
	}

	public synchronized void send(String message, Object... parameters) {
		String output = Message.send(writer, message, parameters);
		if (output == null)
			close();
		else
			server.log("Sent: " + output);
	}

	public synchronized void close() {
		if (isClosed())
			return;
		try {
			client.close();
			reader.close();
			writer.close();
			if (id != -1)
				server.log("Connection [" + id + "] closed");
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
