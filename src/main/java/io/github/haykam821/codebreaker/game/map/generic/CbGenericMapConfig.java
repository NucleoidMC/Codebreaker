package io.github.haykam821.codebreaker.game.map.generic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public final class CbGenericMapConfig {
	private static final BlockStateProvider DEFAULT_FLOOR_PROVIDER = new SimpleBlockStateProvider(Blocks.GRAY_CONCRETE.getDefaultState());

	public static final Codec<CbGenericMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_provider", DEFAULT_FLOOR_PROVIDER).forGetter(CbGenericMapConfig::getFloorProvider)
	).apply(instance, CbGenericMapConfig::new));

	private final BlockStateProvider floorBlock;

	public CbGenericMapConfig(BlockStateProvider floorBlock) {
		this.floorBlock = floorBlock;
	}

	public BlockStateProvider getFloorProvider() {
		return floorBlock;
	}
}