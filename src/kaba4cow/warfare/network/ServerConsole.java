package kaba4cow.warfare.network;

import java.io.IOException;
import java.util.ArrayList;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.input.Input;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.warfare.network.tcp.Server;

public class ServerConsole implements MainProgram {

	private ArrayList<String> history;
	private int index;

	private String output;
	private String text;

	private int scroll;
	private int maxScroll;

	private Server server;

	public ServerConsole() {

	}

	@Override
	public void init() {
		scroll = 0;
		maxScroll = 0;

		text = "";

		history = new ArrayList<>();
		index = 0;

		Console.init(this);
		Console.processCommand("");
		output = "Ascii Warfare Server" + Console.getOutput();

		Display.setDrawCursor(false);
	}

	@Override
	public void update(float dt) {
		scroll -= 2 * Mouse.getScroll();
		if (scroll < 0)
			scroll = 0;
		if (scroll > maxScroll)
			scroll = maxScroll;

		if (Keyboard.isKeyDown(Keyboard.KEY_ENTER)) {
			if (Console.processCommand(text))
				Engine.requestClose();
			String cmd = Console.getOutput();
			output += text + "\n" + cmd;
			if (history.isEmpty() || !history.get(history.size() - 1).equalsIgnoreCase(text))
				history.add(text);
			text = "";
			index = history.size();
			render();
			scroll = maxScroll;
		} else if (!history.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			index--;
			if (index < 0)
				index = history.size() - 1;
			text = history.get(index);
		} else if (!history.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			index++;
			if (index >= history.size())
				index = 0;
			text = history.get(index);
		} else
			text = Input.typeString(text);
	}

	@Override
	public void render() {
		int consoleColor = Console.getColor();
		Display.setBackground(' ', Console.getColor());

		int x = 0;
		int y = -scroll;
		for (int i = 0; i < output.length(); i++) {
			char c = output.charAt(i);
			if (c == '\n') {
				x = 0;
				y++;
			} else if (c == '\t')
				x += 4;
			else
				Drawer.draw(x++, y, c, consoleColor);

			if (x >= Display.getWidth()) {
				x = 0;
				y++;
			}
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			Drawer.draw(x++, y, c, consoleColor);

			if (x >= Display.getWidth()) {
				x = 0;
				y++;
			}
		}

		y += scroll;
		if (y < Display.getHeight())
			maxScroll = 0;
		else
			maxScroll = y + 5 - Display.getHeight();
	}

	public Server getServer() {
		return server;
	}

	public boolean startServer(int port, float size, int season) {
		try {
			server = new Server(port, size, season);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void closeServer() {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	public static void main(String[] args) {
//		try {
//			new Server(5000, 0.0f, 2);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		Engine.init("Ascii Warfare Server", 60);
		Display.createWindowed(50, 40);
		Engine.start(new ServerConsole());
	}

}
