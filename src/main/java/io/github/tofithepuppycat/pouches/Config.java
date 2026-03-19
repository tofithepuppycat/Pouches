package io.github.tofithepuppycat.pouches;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {

	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final Client CLIENT;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		CLIENT = new Client(builder);
		CLIENT_SPEC = builder.build();
	}

	private Config() {
	}

	public static final class Client {
		public final ForgeConfigSpec.BooleanValue togglePouchWheelMode;

		private Client(ForgeConfigSpec.Builder builder) {
			builder.push("input");
			togglePouchWheelMode = builder
					.comment("If true, pouch wheel uses toggle mode (press once to open, press again to confirm).", "If false, pouch wheel uses hold mode (hold to open, release to confirm).")
					.define("togglePouchWheelMode", false);
			builder.pop();
		}
	}
}
