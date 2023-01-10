package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.code.provider.CodeProvider;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public class CodebreakerConfig {
	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			CodebreakerMapConfig.CODEC.optionalFieldOf("map", CodebreakerMapConfig.DEFAULT).forGetter(CodebreakerConfig::getMapConfig),
			CodeProvider.TYPE_CODEC.fieldOf("code_provider").forGetter(CodebreakerConfig::getCodeProvider),
			RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("code_pegs").forGetter(config -> config.codePegs),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(CodebreakerConfig::getTicksUntilClose),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
		).apply(instance, CodebreakerConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final CodebreakerMapConfig mapConfig;
	private final CodeProvider codeProvider;
	private final RegistryEntryList<Block> codePegs;
	private final IntProvider ticksUntilClose;
	private final int guideTicks;
	private final int chances;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, CodebreakerMapConfig mapConfig, CodeProvider codeProvider, RegistryEntryList<Block> codePegs, IntProvider ticksUntilClose, int guideTicks, int chances, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.codeProvider = codeProvider;
		this.codePegs = codePegs;
		this.ticksUntilClose = ticksUntilClose;
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

	public IntProvider getTicksUntilClose() {
		return this.ticksUntilClose;
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