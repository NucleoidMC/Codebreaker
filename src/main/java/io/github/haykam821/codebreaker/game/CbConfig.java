package io.github.haykam821.codebreaker.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.map.generic.CbGenericMapConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CbConfig {
	public static final Codec<CbConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PlayerConfig.CODEC.fieldOf("players").forGetter(CbConfig::getPlayerConfig),
		Codec.either(CbGenericMapConfig.CODEC, Identifier.CODEC).fieldOf("map").forGetter(CbConfig::getMapConfig),
		BlockState.CODEC.fieldOf("board_block").forGetter(CbConfig::getBoardBlock),
		Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CbConfig::getGuideTicks),
		Codec.INT.fieldOf("chances").forGetter(CbConfig::getChances),
		Codec.INT.fieldOf("spaces").forGetter(CbConfig::getSpaces)
	).apply(instance, CbConfig::new));

	private final PlayerConfig playerConfig;
	private final Either<CbGenericMapConfig, Identifier> mapConfig;
	private final BlockState boardBlock;
	private final int guideTicks;
	private final int chances;
	private final int spaces;

	public CbConfig(PlayerConfig playerConfig, Either<CbGenericMapConfig, Identifier> mapConfig, BlockState boardBlock, int guideTicks, int chances, int spaces) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.boardBlock = boardBlock;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.spaces = spaces;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public Either<CbGenericMapConfig, Identifier> getMapConfig() {
		return mapConfig;
	}

	public BlockState getBoardBlock() {
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
}