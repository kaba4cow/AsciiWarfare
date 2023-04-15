package kaba4cow.warfare;

import java.util.prefs.Preferences;

public class Settings {

	private static Preferences preferences;

	private Settings() {

	}

	public static void init() {
		preferences = Preferences.userNodeForPackage(Settings.class);
	}

	public static boolean isFullscreen() {
		return preferences.getBoolean("fullscreen", true);
	}

	public static void setFullscreen(boolean fullscreen) {
		preferences.putBoolean("fullscreen", fullscreen);
	}

	public static String getServerAddress() {
		return preferences.get("ip", "");
	}

	public static String getServerPort() {
		return preferences.get("port", "");
	}

	public static void setServerInfo(String serverAddress, String serverPort) {
		preferences.put("ip", serverAddress);
		preferences.put("port", serverPort);
	}

	public static int getFont() {
		return preferences.getInt("font", 0);
	}

	public static void setFont(int font) {
		preferences.putInt("font", font);
	}

}
