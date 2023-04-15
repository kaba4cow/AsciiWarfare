package kaba4cow.warfare.gui.shop;

import java.util.ArrayList;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUISeparator;
import kaba4cow.ascii.gui.GUIText;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.gui.GUI;

public class ShopTypeFrame extends GUIFrame {

	private ShopUnitFrame unitFrame;

	public ShopTypeFrame(Player player, String type, ArrayList<UnitFile> units) {
		super(GUI.COLOR, false, false);
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
		super.render(Window.getWidth() / 4, Window.getHeight() / 5, Window.getWidth() - Window.getWidth() / 4,
				Window.getHeight() - Window.getHeight() / 5, false);
		unitFrame.render();
	}

}
