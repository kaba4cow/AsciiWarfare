package kaba4cow.warfare.game;

import kaba4cow.ascii.toolbox.files.DataFile;

public class Village {

	public final int x;
	public final int y;
	public final int radius;

	public Village(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public Village(DataFile data) {
		this.x = data.getInt(0);
		this.y = data.getInt(1);
		this.radius = data.getInt(2);
	}

	public void save(DataFile data) {
		data.setInt(x).setInt(y).setInt(radius);
	}

}
