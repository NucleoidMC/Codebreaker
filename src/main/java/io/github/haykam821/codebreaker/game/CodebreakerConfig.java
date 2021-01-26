package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CodebreakerConfig {
	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.INT.fieldOf("spaces").forGetter(CodebreakerConfig::getSpaces)
		).apply(instance, CodebreakerConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final int guideTicks;
	private final int chances;
	private final int spaces;

	public CodebreakerConfig(PlayerConfig playerConfig, int guideTicks, int chances, int spaces) {
		this.playerConfig = playerConfig;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.spaces = spaces;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
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