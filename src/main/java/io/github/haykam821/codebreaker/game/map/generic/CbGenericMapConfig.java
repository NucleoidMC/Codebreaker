package io.github.haykam821.codebreaker.game.map.generic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public final class CbGenericMapConfig {
	public static final Codec<CbGenericMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("floor_block").forGetter(CbGenericMapConfig::getFloorBlock)
	).apply(instance, CbGenericMapConfig::new));

	private final BlockState floorBlock;

	public CbGenericMapConfig(BlockState floorBlock) {
		this.floorBlock = floorBlock;
	}

	public BlockState getFloorBlock() {
		return floorBlock;
	}
}