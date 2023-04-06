package kaba4cow.warfare.gui.info;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.ascii.toolbox.utils.StringUtils;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.Player;

public class PlayerFrame extends GUIFrame {

	public PlayerFrame(Player player) {
		super(Game.GUI_COLOR, false, false);
		setTitle("Player");

		new GUIText(this, -1,
				"Level: " + (int) (player.getLevel() + 1f) + " (" + StringUtils.percent(player.getLevel() % 1f) + ")");
		new GUIText(this, -1, "Cash: " + player.getCash());
		new GUIText(this, -1, "Income: " + player.getIncome());
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 4, 0, Display.getWidth() - Display.getWidth() / 4, Display.getHeight() / 5,
				false);
	}

}
