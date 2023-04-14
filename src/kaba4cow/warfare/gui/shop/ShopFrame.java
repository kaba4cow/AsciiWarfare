package kaba4cow.warfare.gui.shop;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.UnitTypeFile;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.gui.GUI;

public class ShopFrame extends GUIFrame {

	private PlayerFrame playerFrame;
	private ShopTypeFrame typeFrame;

	public ShopFrame(Player player) {
		super(GUI.COLOR, false, false);
		setTitle("Types");

		playerFrame = new PlayerFrame(player);

		LinkedHashMap<String, ArrayList<UnitFile>> map = UnitFile.getSorted();
		for (String type : map.keySet()) {
			ArrayList<UnitFile> units = map.get(type);
			ArrayList<UnitFile> available = new ArrayList<>();
			for (int i = 0; i < units.size(); i++)
				if (player.isUnitAvailable(units.get(i)))
					available.add(units.get(i));
			if (available.isEmpty())
				continue;

			new GUIButton(this, -1, UnitTypeFile.get(type).getName(), f -> {
				typeFrame = new ShopTypeFrame(player, UnitTypeFile.get(type).getName(), available);
			});
		}

		typeFrame = null;
	}

	@Override
	public void update() {
		if (typeFrame == null)
			super.update();
		else
			typeFrame.update();
		if (Input.isKeyDown(Input.KEY_ESCAPE))
			typeFrame = null;
	}

	@Override
	public void render() {
		playerFrame.render(canExit());
		if (typeFrame == null)
			super.render(0, Window.getHeight() / 5, Window.getWidth(), Window.getHeight() - Window.getHeight() / 5,
					false);
		else
			typeFrame.render();
	}

	public boolean canExit() {
		return typeFrame == null;
	}

}
