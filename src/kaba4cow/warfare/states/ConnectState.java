package kaba4cow.warfare.states;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUIText;
import kaba4cow.ascii.gui.GUITextField;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.Settings;
import kaba4cow.warfare.gui.GUI;

public class ConnectState extends AbstractState {

	private static final ConnectState instance = new ConnectState();

	private final GUIFrame frame;
	private final GUITextField ipTextField;
	private final GUITextField portTextField;

	public ConnectState() {
		frame = new GUIFrame(GUI.COLOR, false, false).setTitle("Multiplayer");

		new GUIText(frame, -1, "IP");
		ipTextField = new GUITextField(frame, -1, Settings.getServerAddress());
		ipTextField.setMaxCharacters(15);

		new GUIText(frame, -1, "Port");
		portTextField = new GUITextField(frame, -1, Settings.getServerPort()).setCharset("0123456789")
				.setMaxCharacters(5);

		new GUIButton(frame, -1, "Connect", f -> {
			Settings.setServerInfo(ipTextField.getText(), portTextField.getText());
			connect();
		});

		new GUIButton(frame, -1, "Return", f -> {
			Game.switchState(MenuState.getInstance());
		});
	}

	public static ConnectState getInstance() {
		return instance;
	}

	@Override
	public void update(float dt) {
		if (Input.isKeyDown(Input.KEY_ESCAPE))
			Game.switchState(MenuState.getInstance());

		frame.update();
	}

	@Override
	public void render() {
		renderTitle();
		renderFrame(frame);
	}

	private void connect() {
		String ip = ipTextField.getText();
		int port;
		try {
			port = Integer.parseInt(portTextField.getText());
		} catch (NumberFormatException e) {
			return;
		}

		MultiplayerState.getInstance().connect(ip, port);
	}

}
