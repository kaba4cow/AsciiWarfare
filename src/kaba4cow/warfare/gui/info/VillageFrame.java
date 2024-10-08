package kaba4cow.warfare.gui.info;

import java.util.ArrayList;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUISeparator;
import kaba4cow.ascii.gui.GUIText;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.Village;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.gui.GUI;

public class VillageFrame extends GUIFrame {

	public VillageFrame(World world, Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Villages");

		ArrayList<Village> villages = world.getVillages();
		for (int i = 0; i < villages.size(); i++) {
			Village village = villages.get(i);
			if (village.getOccupier(world) != player)
				continue;

			new GUIText(this, -1, "Village [" + village.x + ", " + village.y + "]");
			new GUISeparator(this, -1, true);

			new GUIText(this, -1, "Houses: " + village.getHouses());
			new GUIText(this, -1, "Income: " + village.getIncome());
			new GUIText(this, -1, "Units: " + village.getTotalUnits(world, player));
			new GUISeparator(this, -1, false);
		}
	}

	@Override
	public void render() {
		super.render(0, 0, Window.getWidth() / 4, Window.getHeight(), false);
	}

}
