package kaba4cow.warfare.network;

public class Message {

	public static final String CONNECT = "C";
	public static final String DISCONNECT = "D";
	public static final String PING = "P";

	private Message() {

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
