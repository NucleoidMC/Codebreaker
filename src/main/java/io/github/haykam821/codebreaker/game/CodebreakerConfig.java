package io.github.haykam821.codebreaker.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.code.generator.CodeGenerator;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import io.github.haykam821.codebreaker.game.turn.CyclicTurnManager;
import io.github.haykam821.codebreaker.game.turn.NoTurnManager;
import io.github.haykam821.codebreaker.game.turn.TurnManager;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CodebreakerConfig {
	public static final Codec<CodebreakerConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(CodebreakerConfig::getPlayerConfig),
			CodebreakerMapConfig.CODEC.optionalFieldOf("map", CodebreakerMapConfig.DEFAULT).forGetter(CodebreakerConfig::getMapConfig),
			CodeGenerator.TYPE_CODEC.fieldOf("code_generator").forGetter(CodebreakerConfig::getCodeGenerator),
			Identifier.CODEC.fieldOf("code_pegs").forGetter(config -> config.codePegs),
			Codec.INT.optionalFieldOf("guide_ticks", -1).forGetter(CodebreakerConfig::getGuideTicks),
			Codec.INT.fieldOf("chances").forGetter(CodebreakerConfig::getChances),
			Codec.BOOL.optionalFieldOf("turns", true).forGetter(config -> config.turns)
		).apply(instance, CodebreakerConfig::new);
	});

	private final PlayerConfig playerConfig;
	private final CodebreakerMapConfig mapConfig;
	private final CodeGenerator codeGenerator;
	private final Identifier codePegs;
	private final int guideTicks;
	private final int chances;
	private final boolean turns;

	public CodebreakerConfig(PlayerConfig playerConfig, CodebreakerMapConfig mapConfig, CodeGenerator codeGenerator, Identifier codePegs, int guideTicks, int chances, boolean turns) {
		this.playerConfig = playerConfig;
		this.mapConfig = mapConfig;
		this.codeGenerator = codeGenerator;
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

	public CodeGenerator getCodeGenerator() {
		return this.codeGenerator;
	}

	public Tag<Block> getCodePegs() {
		Tag<Block> tag = BlockTags.getTagGroup().getTag(this.codePegs);
		return tag == null ? Main.CODE_PEGS : tag;
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