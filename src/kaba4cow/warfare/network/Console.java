package kaba4cow.warfare.network;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kaba4cow.warfare.network.tcp.Connection;
import kaba4cow.warfare.network.tcp.Server;

public class Console {

	private static LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

	private static String[] parameterArray = new String[32];
	private static boolean exit = false;

	private static int color = 0x000FFF;

	private static StringBuilder output = new StringBuilder();

	private Console() {

	}

	public static void init(ServerConsole program) {
		new Command("help", "", "Prints all available commands") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				for (String name : commands.keySet()) {
					Command command = commands.get(name);
					output.append("-> " + name.toUpperCase() + " " + command.parameters + "\n");
					output.append(command.description + "\n\n");
				}
			}
		};

		new Command("exit", "", "Closes the program") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				exit = true;
			}
		};

		new Command("color", "[color]", "Sets console color (000000-FFFFFF)") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;
				try {
					color = Integer.parseInt(parameters[0], 16);
				} catch (NumberFormatException e) {
					invalidParameters(output);
				}
			}
		};

		new Command("server-start", "[port] [size] [season]", "Starts new server and creates new map") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (program.getServer() != null) {
					output.append("Server already running\n");
					return;
				}
				if (invalidParameters(numParameters, 3, output))
					return;
				int port = 0;
				try {
					port = Integer.parseInt(parameters[0]);
				} catch (NumberFormatException e) {
					output.append("Invalid port");
					return;
				}
				int size = 0;
				try {
					size = Integer.parseInt(parameters[1]);
				} catch (NumberFormatException e) {
					output.append("Invalid size");
					return;
				}
				int season = 0;
				try {
					season = Integer.parseInt(parameters[2]);
				} catch (NumberFormatException e) {
					output.append("Invalid season");
					return;
				}
				if (port < Server.MIN_PORT || port > Server.MAX_PORT)
					output.append("Port out of range [" + Server.MIN_PORT + "-" + Server.MAX_PORT + "]\n");
				else if (program.startServer(port, size, season))
					output.append("Server started\n");
				else
					output.append("Could not start a server\n");
			}
		};

		new Command("server-close", "", "Closes current server") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (program.getServer() != null) {
					program.closeServer();
					output.append("Server closed\n");
				} else
					output.append("No server running\n");
			}
		};

		new Command("server-list", "", "Lists all connected clients") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (program.getServer() != null) {
					ArrayList<Connection> list = program.getServer().getClients();
					output.append(String.format("%6s | %12s", "ID", "IP"));
					output.append('\n');
					for (int i = 0; i < 21; i++)
						output.append('-');
					output.append('\n');
					for (Connection client : list) {
						output.append(
								String.format("%6s | %12s", client.getID(), client.getAddress().getHostAddress()));
						output.append('\n');
						output.append(String.format("%6s | %12s", "", ""));
						output.append('\n');
					}
					output.append('\n');
				} else
					output.append("No server running\n");
			}
		};
	}

	public static void addCommand(Command command) {
		commands.put(command.name, command);
	}

	public static boolean processCommand(String line) {
		output.append('\n');
		String name = getCommandName(line);
		int numParameters = getCommandParameters(name, line);

		Command command = commands.get(name);

		if (line.isEmpty())
			output.append('\n');
		else if (command == null)
			output.append("Unknown command: " + line + "\n");
		else
			command.execute(parameterArray, numParameters, output);
		output.append('\n');

		if (exit)
			return true;

		output.append(" -> ");

		return false;
	}

	private static String getCommandName(String string) {
		String name = "";
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == ' ')
				break;
			else
				name += c;
		}
		return name;
	}

	private static int getCommandParameters(String name, String string) {
		if (name.length() == string.length())
			return 0;

		string = string.substring(name.length()) + " ";
		final int length = string.length();

		int index = 0;
		boolean backslash = false;
		boolean space = false;
		String token = "";

		for (int i = 1; i < length; i++) {
			char c = string.charAt(i);
			if (!space && !backslash && c == ' ') {
				parameterArray[index++] = token;
				token = "";
				space = true;
			} else if (c == '\\') {
				backslash = true;
				space = false;
			} else {
				token += c;
				space = false;
				backslash = false;
			}

			if (index >= parameterArray.length)
				break;
		}

		for (int i = index; i < parameterArray.length; i++)
			parameterArray[i] = null;

		return index;
	}

	public static String getOutput() {
		String string = output.toString();
		output = new StringBuilder();
		return string;
	}

	public static int getColor() {
		return color;
	}

}
