package kaba4cow.warfare.network;

import java.io.BufferedWriter;
import java.io.IOException;

import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.utils.StringUtils;

public class Message {

	public static final String CONNECT = "C";
	public static final String DISCONNECT = "D";
	public static final String WORLD = "W";

	public static final String TURN = "t";
	public static final String PROJECTILE = "p";
	public static final String MOVE = "m";
	public static final String CASH = "c";
	public static final String UNIT = "u";

	public static final String EOF = "/EOF/";

	private Message() {

	}

	public static synchronized String[] compressData(DataFile data) {
		String string = data.toString();
		string = string.replaceAll("\t", "");
		string = string.replaceAll("\r", "");
		string = string.replaceAll("\n\n", "\n");
		string = string.replaceAll("\n", "\t");
		return StringUtils.divideString(string, 1020);
	}

	public static synchronized DataFile decompressData(StringBuilder builder) {
		String string = builder.toString();
		string = string.replaceAll("\t", "\n");
		return DataFile.fromString(string);
	}

	public static synchronized String send(BufferedWriter writer, String message, Object... parameters) {
		try {
			StringBuilder builder = new StringBuilder(message);
			if (parameters != null)
				for (int i = 0; i < parameters.length; i++) {
					builder.append(parameters[i].toString());
					if (i < parameters.length - 1)
						builder.append('|');
				}
			writer.write(builder.toString());
			writer.newLine();
			writer.flush();
			return builder.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static String getMessage(String message) {
		return message.substring(0, 1);
	}

	public static String[] getParameters(String message) {
		message = message.substring(1);
		return message.split("\\|");
	}

}
