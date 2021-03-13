package io.github.haykam821.codebreaker;

import io.github.haykam821.codebreaker.game.CbConfig;
import io.github.haykam821.codebreaker.game.phase.CbWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;

public class Codebreaker implements ModInitializer {
	private static final String ID = "codebreaker";
	public static final Logger LOGGER = LogManager.getLogger(ID);

	private static final Identifier CODE_PEGS_ID = new Identifier(ID, "code_pegs");
	public static final Tag<Block> CODE_PEGS = TagRegistry.block(CODE_PEGS_ID);

	private static final Identifier CODEBREAKER_ID = new Identifier(ID, "simple");
	public static final GameType<CbConfig> CODEBREAKER_TYPE = GameType.register(CODEBREAKER_ID, CbWaitingPhase::open, CbConfig.CODEC);

	@Override
	public void onInitialize() {
	}
}