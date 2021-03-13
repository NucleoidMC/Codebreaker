package io.github.haykam821.codebreaker.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.map.generic.CodebreakerGenericMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CodebreakerConfig {
	private static final BlockStateProvider DEFAULT_BOARD_PROVIDER = new SimpleBlockStateProvider(Blocks.WHITE_CONCRETE.getDefaultState());
	private static final Identifier DEFAULT_PEG_TAG = Main.id("pegs/concrete_six");
	private static final BlockState DEFAULT_CONFIRM_BLOCK = Blocks.SEA_LANTERN.getDefaultState();
	private static final BlockState DEFAULT_RESET_BLOCK = Blocks.BEDROCK.getDefaultState();

	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
		Codec.either(CodebreakerGenericMapConfig.CODEC, Identifier.CODEC).fieldOf("map").forGetter(CodebreakerConfig::getMapConfig),
		BlockStateProvider.TYPE_CODEC.optionalFieldOf("board_block", DEFAULT_BOARD_PROVIDER).forGetter(CodebreakerConfig::getBoardProvider),
		Identifier.CODEC.optionalFieldOf("peg_tag", DEFAULT_PEG_TAG).forGetter(config -> config.pegTag),
		BlockState.CODEC.optionalFieldOf("reset_block", DEFAULT_RESET_BLOCK).forGetter(CodebreakerConfig::getResetBlock),
		BlockState.CODEC.optionalFieldOf("confirm_block", DEFAULT_CONFIRM_BLOCK).forGetter(CodebreakerConfig::getConfirmBlock),
		Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
		Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
		Codec.INT.fieldOf("spaces").forGetter(CodebreakerConfig::getSpaces),
		Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
	).apply(instance, CodebreakerConfig::new));

	private final PlayerConfig playerConfig;
	private final Either<CodebreakerGenericMapConfig, Identifier> mapConfig;
	private final BlockStateProvider boardBlock;
	private final Identifier pegTag;
	private final BlockState resetBlock;
	private final BlockState confirmBlock;
	private final int guideTicks;
	private final int chances;
	private final int spaces;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, Either<CodebreakerGenericMapConfig, Identifier> mapConfig, BlockStateProvider boardBlock, Identifier pegTag, BlockState resetBlock, BlockState confirmBlock, int guideTicks, int chances, int spaces, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.boardBlock = boardBlock;
		this.pegTag = pegTag;
		this.resetBlock = resetBlock;
		this.confirmBlock = confirmBlock;
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

	public Tag<Block> getPegTag() {
		return TagRegistry.block(pegTag);
	}

	public BlockState getConfirmBlock() {
		return confirmBlock;
	}

	public BlockState getResetBlock() {
		return resetBlock;
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