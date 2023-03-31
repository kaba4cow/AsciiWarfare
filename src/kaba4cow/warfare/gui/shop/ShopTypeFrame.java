package kaba4cow.warfare.gui.shop;

import java.util.ArrayList;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.game.Player;

public class ShopTypeFrame extends GUIFrame {

	private ShopUnitFrame unitFrame;

	public ShopTypeFrame(Player player, String type, ArrayList<UnitFile> units) {
		super(Game.GUI_COLOR, false, false);
		setTitle(type);

		for (UnitFile unit : units) {
			new GUIText(this, -1, "Name: " + unit.getName());
			new GUIText(this, -1, "Price: " + unit.getPrice());
			new GUIButton(this, -1, "Info", f -> {
				unitFrame = new ShopUnitFrame(player, unit);
			});
			new GUISeparator(this, -1, false);
		}

		unitFrame = new ShopUnitFrame(player, units.get(0));
	}

	@Override
	public void update() {
		super.update();
		unitFrame.update();
	}

	@Override
	public void render() {
		super.render(Display.getWidth() / 4, Display.getHeight() / 5, Display.getWidth() - Display.getWidth() / 4,
				Display.getHeight() - Display.getHeight() / 5, false);
		unitFrame.render();
	}

}
