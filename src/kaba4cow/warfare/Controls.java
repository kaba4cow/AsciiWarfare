package kaba4cow.warfare;

import kaba4cow.ascii.core.Input;

public enum Controls {

	FULLSCREEN("Fullscreen", Input.KEY_F11, true), //
	FPS("Show FPS", Input.KEY_F3, true), //

	HELP("Help", Input.KEY_H, true), //
	PAUSE("Pause", Input.KEY_ESCAPE, true), //
	CHAT("Chat", Input.KEY_T, true), //

	HIRE("Hire Units", Input.KEY_R, true), //
	INFO("Player Info", Input.KEY_I, true), //

	NEW_TURN("End Turn", Input.KEY_TAB, true), //
	FIRE_MODE("Fire Mode", Input.KEY_F, true), //
	PREV_UNIT("Prev Unit", Input.KEY_Q, true), //
	NEXT_UNIT("Next Unit", Input.KEY_E, true), //
	SELECT_UNIT("Select Unit", Input.RIGHT, false), //
	PATH("Move / Shoot", Input.LEFT, false), //
	MOVE_UNIT("Start / Stop Moving", Input.KEY_SPACE, true), //

	CAMERA_FOLLOW("Follow Unit", Input.KEY_C, true), //
	CAMERA_MOVE("Move Camera", Input.MIDDLE, false), //
	CAMERA_UP("Camera Up", Input.KEY_W, true), //
	CAMERA_DOWN("Camera Down", Input.KEY_S, true), //
	CAMERA_LEFT("Camera Left", Input.KEY_A, true), //
	CAMERA_RIGHT("Camera Right", Input.KEY_D, true), //
	CAMERA_SCROLL("Horizontal Scrolling", Input.KEY_SHIFT_LEFT, true);

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
			return Input.nameKeyboard(code);
		return Input.nameMouse(code);
	}

	public boolean isKey() {
		if (keyboard)
			return Input.isKey(code);
		return Input.isButton(code);
	}

	public boolean isKeyDown() {
		if (keyboard)
			return Input.isKeyDown(code);
		return Input.isButtonDown(code);
	}

	public boolean isKeyUp() {
		if (keyboard)
			return Input.isKeyUp(code);
		return Input.isButtonUp(code);
	}

}
