package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.gui.GUI;
import kaba4cow.warfare.states.MenuState;

public class GameOverFrame extends GUIFrame {

	public GameOverFrame(World world, Player winner, Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Game Over");

		if (player == winner)
			new GUIText(this, -1, "You win! " + Glyphs.STAR);
		else
			new GUIText(this, -1, "You lose! " + Glyphs.CROSS);
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Time elapsed:");
		new GUIText(this, -1, world.getWorldWeek() + " weeks");
		new GUIText(this, -1, world.getWorldDay() + " days");
		new GUIText(this, -1, world.getWorldHour() + " hours");
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Map uncovered: " + GUI.percent(player.getMapUncovered()));
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Units hired: " + player.getUnitsHired());
		new GUIText(this, -1, "Units lost: " + player.getUnitsLost());
		new GUIText(this, -1, "Units destroyed: " + player.getUnitsKilled());
		new GUISeparator(this, -1, true);

		new GUIText(this, -1, "Cash earned: " + player.getCashEarned());
		new GUIText(this, -1, "Cash spent: " + player.getCashSpent());
		new GUISeparator(this, -1, true);

		new GUIButton(this, -1, "Menu", f -> {
			if (world.getClient() != null)
				world.getClient().close();
			Game.switchState(MenuState.getInstance());
		});
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 2, Display.getHeight() / 2, Display.getWidth() / 2,
				Display.getHeight() - Display.getHeight() / 3, true);
	}

}
