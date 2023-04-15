package kaba4cow.warfare.gui.info;

import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.World;

public class InfoFrame {

	private GUIFrame[] frames;

	public InfoFrame(World world, Player player) {
		frames = new GUIFrame[3];
		frames[0] = new PlayerFrame(player);
		frames[1] = new UnitFrame(player);
		frames[2] = new VillageFrame(world, player);
	}

	public void update() {
		for (int i = 0; i < frames.length; i++)
			frames[i].update();
	}

	public void render() {
		for (int i = 0; i < frames.length; i++)
			frames[i].render();
	}

}
