package kaba4cow.warfare;

import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.warfare.game.World;

public class Camera {

	private static final float MOVE_SPEED = 80f;
	private static final float SCROLL_SPEED = 10f;

	private World world;

	private float x;
	private float y;

	private boolean drag;
	private int dragX;
	private int dragY;

	public Camera(World world) {
		this.world = world;
		this.x = 0f;
		this.y = 0f;
		this.drag = false;
		this.dragX = 0;
		this.dragY = 0;
	}

	public void update(float dt) {
		if (isMouseInViewport()) {
			if (Keyboard.isKey(Keyboard.KEY_W))
				y -= MOVE_SPEED * dt;
			if (Keyboard.isKey(Keyboard.KEY_S))
				y += MOVE_SPEED * dt;
			if (Keyboard.isKey(Keyboard.KEY_A))
				x -= MOVE_SPEED * dt;
			if (Keyboard.isKey(Keyboard.KEY_D))
				x += MOVE_SPEED * dt;

			if (Keyboard.isKey(Keyboard.KEY_SHIFT_LEFT))
				x += SCROLL_SPEED * Mouse.getScroll();
			else
				y -= SCROLL_SPEED * Mouse.getScroll();
		}

		int mX = Mouse.getTileX();
		int mY = Mouse.getTileY();

		if (isMouseInViewport() && Mouse.isKeyDown(Mouse.MIDDLE)) {
			drag = true;
			dragX = (int) x + mX;
			dragY = (int) y + mY;
		} else if (Mouse.isKeyUp(Mouse.MIDDLE))
			drag = false;

		if (drag) {
			x = dragX - mX;
			y = dragY - mY;
		}

		clampPosition();
	}

	private void clampPosition() {
		if (x < 0f)
			x = 0f;
		if (x > world.getSize() - world.getViewport().width)
			x = world.getSize() - world.getViewport().width;
		if (y < 0f)
			y = 0f;
		if (y > world.getSize() - world.getViewport().height)
			y = world.getSize() - world.getViewport().height;
	}

	public void setPosition(int x, int y) {
		this.x = x - world.getViewport().width / 2;
		this.y = y - world.getViewport().height / 2;
		clampPosition();
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public int getMouseX() {
		return Mouse.getTileX() - world.getViewport().x + (int) x;
	}

	public int getMouseY() {
		return Mouse.getTileY() - world.getViewport().y + (int) y;
	}

	public boolean isMouseInViewport() {
		int x = Mouse.getTileX() - world.getViewport().x;
		int y = Mouse.getTileY() - world.getViewport().y;
		return x >= 0 && x < world.getViewport().width && y >= 0 && y < world.getViewport().height;
	}

}
