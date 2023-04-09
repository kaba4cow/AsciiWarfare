package kaba4cow.warfare;

import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;

public enum Controls {

	SCREENSHOT("Screenshot", Keyboard.KEY_F12, true), //
	FULLSCREEN("Fullscreen", Keyboard.KEY_F11, true), //
	FPS("Show FPS", Keyboard.KEY_F3, true), //

	HELP("Help", Keyboard.KEY_H, true), //
	PAUSE("Pause", Keyboard.KEY_ESCAPE, true), //

	HIRE("Hire Units", Keyboard.KEY_R, true), //
	INFO("Player Info", Keyboard.KEY_T, true), //

	NEW_TURN("End Turn", Keyboard.KEY_TAB, true), //
	FIRE_MODE("Fire Mode", Keyboard.KEY_F, true), //
	PREV_UNIT("Prev Unit", Keyboard.KEY_Q, true), //
	NEXT_UNIT("Next Unit", Keyboard.KEY_E, true), //
	SELECT_UNIT("Select Unit", Mouse.RIGHT, false), //
	PATH("Move / Shoot", Mouse.LEFT, false), //
	MOVE_UNIT("Start / Stop Moving", Keyboard.KEY_SPACE, true), //

	CAMERA_FOLLOW("Follow Unit", Keyboard.KEY_C, true), //
	CAMERA_MOVE("Move Camera", Mouse.MIDDLE, false), //
	CAMERA_UP("Camera Up", Keyboard.KEY_W, true), //
	CAMERA_DOWN("Camera Down", Keyboard.KEY_S, true), //
	CAMERA_LEFT("Camera Left", Keyboard.KEY_A, true), //
	CAMERA_RIGHT("Camera Right", Keyboard.KEY_D, true), //
	CAMERA_SCROLL("Horizontal Scrolling", Keyboard.KEY_SHIFT_LEFT, true);

	private final String name;
	private final int code;
	private final boolean keyboard;

	private Controls(String name, int code, boolean keyboard) {
		this.name = name;
		this.code = code;
		this.keyboard = keyboard;
	}

	public String getName() {
		return name;
	}

	public String getCodeName() {
		if (keyboard)
			return Keyboard.name(code);
		return Mouse.name(code);
	}

	public boolean isKey() {
		if (keyboard)
			return Keyboard.isKey(code);
		return Mouse.isKey(code);
	}

	public boolean isKeyDown() {
		if (keyboard)
			return Keyboard.isKeyDown(code);
		return Mouse.isKeyDown(code);
	}

	public boolean isKeyUp() {
		if (keyboard)
			return Keyboard.isKeyUp(code);
		return Mouse.isKeyUp(code);
	}

}
