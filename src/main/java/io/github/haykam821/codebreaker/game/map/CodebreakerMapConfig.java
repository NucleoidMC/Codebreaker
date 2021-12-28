package io.github.haykam821.codebreaker.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class CodebreakerMapConfig {
	private static final BlockStateProvider DEFAULT_BOARD_PROVIDER = BlockStateProvider.of(Blocks.WHITE_CONCRETE);
	private static final BlockStateProvider DEFAULT_FLOOR_PROVIDER = BlockStateProvider.of(Blocks.GRAY_CONCRETE);
	public static final CodebreakerMapConfig DEFAULT = new CodebreakerMapConfig(DEFAULT_BOARD_PROVIDER, DEFAULT_FLOOR_PROVIDER);

	public static final Codec<CodebreakerMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("board_provider", DEFAULT_BOARD_PROVIDER).forGetter(CodebreakerMapConfig::getBoardProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_provider", DEFAULT_FLOOR_PROVIDER).forGetter(CodebreakerMapConfig::getFloorProvider)
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