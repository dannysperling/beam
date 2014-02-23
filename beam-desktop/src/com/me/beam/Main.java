package com.me.beam;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "beam";
		cfg.useGL20 = false;

		cfg.width = 360;
		cfg.height = 640;

		cfg.foregroundFPS = 60;

		new LwjglApplication(new GameEngine(), cfg);
	}
}
