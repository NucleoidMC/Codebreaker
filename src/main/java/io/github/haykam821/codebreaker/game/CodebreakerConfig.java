package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CodebreakerConfig {
	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			CodebreakerMapConfig.CODEC.optionalFieldOf("map", CodebreakerMapConfig.DEFAULT).forGetter(CodebreakerConfig::getMapConfig),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.INT.fieldOf("spaces").forGetter(CodebreakerConfig::getSpaces),
			Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
		).apply(instance, CodebreakerConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final CodebreakerMapConfig mapConfig;
	private final int guideTicks;
	private final int chances;
	private final int spaces;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, CodebreakerMapConfig mapConfig, int guideTicks, int chances, int spaces, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.spaces = spaces;
		this.turns = turns;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public CodebreakerMapConfig getMapConfig() {
		return this.mapConfig;
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