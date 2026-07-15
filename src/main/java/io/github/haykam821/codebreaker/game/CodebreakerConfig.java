package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.code.provider.CodeProvider;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.SharedConstants;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class CodebreakerConfig {
	public static final MapCodec<CodebreakerConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
			WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			CodebreakerMapConfig.CODEC.optionalFieldOf("map", CodebreakerMapConfig.DEFAULT).forGetter(CodebreakerConfig::getMapConfig),
			CodeProvider.TYPE_CODEC.fieldOf("code_provider").forGetter(CodebreakerConfig::getCodeProvider),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("code_pegs").forGetter(config -> config.codePegs),
			IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(CodebreakerConfig::getTicksUntilClose),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
		).apply(instance, CodebreakerConfig::new);
	});

	private final WaitingLobbyConfig playerConfig;
	private final CodebreakerMapConfig mapConfig;
	private final CodeProvider codeProvider;
	private final HolderSet<Block> codePegs;
	private final IntProvider ticksUntilClose;
	private final int guideTicks;
	private final int chances;
	private final boolean turns;

	public CodebreakerConfig(WaitingLobbyConfig playerConfig, CodebreakerMapConfig mapConfig, CodeProvider codeProvider, HolderSet<Block> codePegs, IntProvider ticksUntilClose, int guideTicks, int chances, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.codeProvider = codeProvider;
		this.codePegs = codePegs;
		this.ticksUntilClose = ticksUntilClose;
		this.guideTicks = guideTicks;
		this.chances = chances;
		this.turns = turns;
	}

	public WaitingLobbyConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public CodebreakerMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public CodeProvider getCodeProvider() {
		return this.codeProvider;
	}

	public HolderSet<Block> getCodePegs() {
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

	public TurnManager createTurnManager(CodebreakerActivePhase phase, ServerPlayer initialTurn) {
		return this.turns ? new CyclicTurnManager(phase, initialTurn) : new NoTurnManager(phase);
	}
}