package io.github.haykam821.codebreaker.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.haykam821.codebreaker.game.map.generic.CodebreakerGenericMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CodebreakerConfig {
	private static final BlockStateProvider DEFAULT_BOARD_PROVIDER = new SimpleBlockStateProvider(Blocks.WHITE_CONCRETE.getDefaultState());

	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
		Codec.either(CodebreakerGenericMapConfig.CODEC, Identifier.CODEC).fieldOf("map").forGetter(CodebreakerConfig::getMapConfig),
		BlockStateProvider.TYPE_CODEC.optionalFieldOf("board_block", DEFAULT_BOARD_PROVIDER).forGetter(CodebreakerConfig::getBoardProvider),
		Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
		Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
		Codec.INT.fieldOf("spaces").forGetter(CodebreakerConfig::getSpaces),
		Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
	).apply(instance, CodebreakerConfig::new));

	private final PlayerConfig playerConfig;
	private final Either<CodebreakerGenericMapConfig, Identifier> mapConfig;
	private final BlockStateProvider boardBlock;
	private final int guideTicks;
	private final int chances;
	private final int spaces;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, Either<CodebreakerGenericMapConfig, Identifier> mapConfig, BlockStateProvider boardBlock, int guideTicks, int chances, int spaces, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.boardBlock = boardBlock;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.spaces = spaces;
		this.turns = turns;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public Either<CodebreakerGenericMapConfig, Identifier> getMapConfig() {
		return mapConfig;
	}

	public BlockStateProvider getBoardProvider() {
		return boardBlock;
	}

	public int getGuideTicks() {
		return this.guideTicks;
	}

	public int getChances() {
		return this.chances;
	}

	public int getSpaces() {
		return this.spaces;
	}

	public TurnManager createTurnManager(CodebreakerActivePhase phase, ServerPlayerEntity initialTurn) {
		return this.turns ? new CyclicTurnManager(phase, initialTurn) : new NoTurnManager(phase);
	}
}