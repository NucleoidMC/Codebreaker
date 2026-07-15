package io.github.haykam821.codebreaker.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class CodebreakerMapConfig {
	private static final BlockStateProvider DEFAULT_BOARD_PROVIDER = BlockStateProvider.simple(Blocks.CONCRETE.white());
	private static final BlockStateProvider DEFAULT_FLOOR_PROVIDER = BlockStateProvider.simple(Blocks.CONCRETE.gray());
	public static final CodebreakerMapConfig DEFAULT = new CodebreakerMapConfig(DEFAULT_BOARD_PROVIDER, DEFAULT_FLOOR_PROVIDER);

	public static final Codec<CodebreakerMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.CODEC.optionalFieldOf("board_provider", DEFAULT_BOARD_PROVIDER).forGetter(CodebreakerMapConfig::getBoardProvider),
			BlockStateProvider.CODEC.optionalFieldOf("floor_provider", DEFAULT_FLOOR_PROVIDER).forGetter(CodebreakerMapConfig::getFloorProvider)
		).apply(instance, CodebreakerMapConfig::new);
	});

	private final BlockStateProvider boardProvider;
	private final BlockStateProvider floorProvider;

	public CodebreakerMapConfig(BlockStateProvider boardProvider, BlockStateProvider floorProvider) {
		this.boardProvider = boardProvider;
		this.floorProvider = floorProvider;
	}

	public BlockStateProvider getBoardProvider() {
		return this.boardProvider;
	}

	public BlockStateProvider getFloorProvider() {
		return this.floorProvider;
	}
}