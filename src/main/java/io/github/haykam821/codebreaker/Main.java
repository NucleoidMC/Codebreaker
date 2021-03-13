package io.github.haykam821.codebreaker;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	private static final String ID = "codebreaker";
	public static final Logger LOGGER = LogManager.getLogger(ID);

	public static final GameType<CodebreakerConfig> CODEBREAKER_TYPE = GameType.register(id("codebreaker"), CodebreakerWaitingPhase::open, CodebreakerConfig.CODEC);

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String s) {
		return new Identifier(ID, s);
	}
}