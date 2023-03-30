package kaba4cow.warfare.states;

import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.ascii.drawing.gui.GUITextField;
import kaba4cow.warfare.Game;

public class ConnectState extends State {

	private static final ConnectState instance = new ConnectState();

	private GUIFrame frame;
	private GUITextField ipTextField;
	private GUITextField portTextField;

	public ConnectState() {
		frame = new GUIFrame(Game.GUI_COLOR, false, false).setTitle("Multiplayer");

		new GUISeparator(frame, -1, true);

		new GUIText(frame, -1, "IP");
		ipTextField = new GUITextField(frame, -1, "");
		ipTextField.setMaxCharacters(15);

		new GUIText(frame, -1, "Port");
		portTextField = new GUITextField(frame, -1, "");
		portTextField.setMaxCharacters(5);

		new GUIButton(frame, -1, "Connect", f -> {
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
