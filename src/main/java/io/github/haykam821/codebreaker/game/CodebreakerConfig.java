package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.code.provider.CodeProvider;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryCodecs;
import net.minecraft.util.registry.RegistryEntryList;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public class CodebreakerConfig {
	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			CodebreakerMapConfig.CODEC.optionalFieldOf("map", CodebreakerMapConfig.DEFAULT).forGetter(CodebreakerConfig::getMapConfig),
			CodeProvider.TYPE_CODEC.fieldOf("code_provider").forGetter(CodebreakerConfig::getCodeProvider),
			RegistryCodecs.entryList(Registry.BLOCK_KEY).fieldOf("code_pegs").forGetter(config -> config.codePegs),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
		).apply(instance, CodebreakerConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final CodebreakerMapConfig mapConfig;
	private final CodeProvider codeProvider;
	private final RegistryEntryList<Block> codePegs;
	private final int guideTicks;
	private final int chances;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, CodebreakerMapConfig mapConfig, CodeProvider codeProvider, RegistryEntryList<Block> codePegs, int guideTicks, int chances, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.codeProvider = codeProvider;
		this.codePegs = codePegs;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.turns = turns;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public CodebreakerMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public CodeProvider getCodeProvider() {
		return this.codeProvider;
	}

	public RegistryEntryList<Block> getCodePegs() {
		return this.codePegs;
	}

	public int getGuideTicks() {
		return this.guideTicks;
	}

	public int getChances() {
		return this.chances;
	}

	public TurnManager createTurnManager(CodebreakerActivePhase phase, ServerPlayerEntity initialTurn) {
		return this.turns ? new CyclicTurnManager(phase, initialTurn) : new NoTurnManager(phase);
	}
}