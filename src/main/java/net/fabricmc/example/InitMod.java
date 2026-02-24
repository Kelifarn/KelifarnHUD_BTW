package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;

public class InitMod implements ModInitializer {
	@Override
	public void onInitialize() {
		new Holder();
	}
}
