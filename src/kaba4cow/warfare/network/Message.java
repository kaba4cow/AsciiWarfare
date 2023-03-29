package kaba4cow.warfare.network;

import java.io.BufferedWriter;
import java.io.IOException;

import kaba4cow.ascii.toolbox.Printer;

public class Message {

	public static final String CONNECT = "C";
	public static final String DISCONNECT = "D";
	public static final String PING = "P";

	public static final String TURN = "t";
	public static final String PROJECTILE = "e";
	public static final String MOVE = "m";

	private Message() {

	}

	public static synchronized void send(BufferedWriter writer, String message, Object... parameters) {
		try {
			StringBuilder builder = new StringBuilder(message);
			if (parameters != null)
				for (int i = 0; i < parameters.length; i++) {
					builder.append(parameters[i].toString());
					if (i < parameters.length - 1)
						builder.append(',');
				}
			writer.write(builder.toString());
			writer.newLine();
			writer.flush();
			Printer.println("Sent: " + builder.toString());
		} catch (IOException e) {
		}
	}

	public static String getString(String input) {
		return input.substring(1).trim();
	}

	public static int getInt(String input) {
		try {
			return Integer.parseInt(getString(input));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static byte[] getBytes(Object... data) {
		String output = "";
		for (int i = 0; i < data.length; i++)
			output += data[i].toString();
		return output.getBytes();
	}

}
