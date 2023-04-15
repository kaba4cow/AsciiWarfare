package kaba4cow.warfare.gui.info;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUIText;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.gui.GUI;

public class PlayerFrame extends GUIFrame {

	public PlayerFrame(Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Player");

		new GUIText(this, -1, "Level: " + GUI.level(player.getLevel()));
		new GUIText(this, -1, "Cash: " + player.getCash());
		new GUIText(this, -1, "Income: " + player.getIncome());
	}

	@Override
	public void render() {
		super.render(Window.getWidth() / 4, 0, Window.getWidth() - Window.getWidth() / 4, Window.getHeight() / 5,
				false);
	}

}
