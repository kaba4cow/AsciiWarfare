package kaba4cow.warfare.network;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.ascii.drawing.gui.GUITextField;
import kaba4cow.warfare.files.GameFiles;
import kaba4cow.warfare.gui.GUI;
import kaba4cow.warfare.network.tcp.Server;

public class ServerConsole implements MainProgram {

	private int scroll;
	private int maxScroll;
	private int prevOutput;

	private Server server;

	private GUIFrame infoFrame;
	private GUIText portText;
	private GUIText clientText;
	private GUIButton serverButton;

	private boolean starting;
	private GUIFrame startFrame;
	private GUITextField portTextField;

	public ServerConsole() {

	}

	@Override
	public void init() {
		scroll = 0;
		maxScroll = 0;
		prevOutput = 0;

		starting = false;

		infoFrame = new GUIFrame(GUI.COLOR, false, false);
		serverButton = new GUIButton(infoFrame, -1, "Start", f -> {
			if (server == null || server.isClosed())
				starting = true;
			else {
				server.close();
				serverButton.setText("Start");
				portText.setText("");
				clientText.setText("");
			}
		});
		new GUISeparator(infoFrame, -1, false);
		portText = new GUIText(infoFrame, -1, "");
		clientText = new GUIText(infoFrame, -1, "");

		startFrame = new GUIFrame(GUI.COLOR, false, false).setTitle("Properties");
		new GUIText(startFrame, -1, "Port");
		portTextField = new GUITextField(startFrame, -1, "").setCharset("0123456789");

		new GUIButton(startFrame, -1, "Start", f -> {
			try {
				int port = Integer.parseInt(portTextField.getText());
				server = new Server(port);
				serverButton.setText("Close");
				portText.setText("Port: " + server.getPort());
				starting = false;
			} catch (Exception e) {
			}
		});
		new GUIButton(startFrame, -1, "Cancel", f -> {
			starting = false;
		});
	}

	@Override
	public void update(float dt) {
		if (Input.isKeyDown(Input.KEY_ESCAPE))
			if (server != null && !server.isClosed()) {
				server.close();
				serverButton.setText("Start");
				portText.setText("");
				clientText.setText("");
			} else
				Engine.requestClose();

		infoFrame.update();
		startFrame.update();

		scroll -= 2 * Input.getScroll();
		if (scroll < 0)
			scroll = 0;
		if (scroll > maxScroll)
			scroll = maxScroll;

		if (server != null && !server.isClosed()) {
			server.update(dt);
			clientText.setText("Clients: " + server.getClients().size());
		}
	}

	@Override
	public void render() {
		drawServerLog();

		infoFrame.render(0, 0, Window.getWidth() / 4, Window.getHeight(), false);
		if (starting)
			startFrame.render(Window.getWidth() / 4, 0, Window.getWidth() - Window.getWidth() / 4, Window.getHeight(),
					false);
	}

	private void drawServerLog() {
		if (server == null)
			return;

		final int startX = Window.getWidth() / 4;

		String output = server.getOutput();
		int x = startX;
		int y = -scroll;
		for (int i = 0; i < output.length(); i++) {
			char c = output.charAt(i);
			if (c == '\n') {
				x = startX;
				y++;
			} else if (c == '\t')
				x += 4;
			else
				Drawer.draw(x++, y, c, GUI.COLOR);

			if (x >= Window.getWidth()) {
				x = startX;
				y++;
			}
		}

		y += scroll;
		if (y < Window.getHeight())
			maxScroll = 0;
		else
			maxScroll = y + 2 - Window.getHeight();

		if (output.length() != prevOutput)
			scroll = maxScroll;
		prevOutput = output.length();
	}

	public static void main(String[] args) {
		GameFiles.init();
		Engine.init("Ascii Warfare Server", 60);
		Window.createWindowed(60, 30);
		Engine.start(new ServerConsole());
	}

}
