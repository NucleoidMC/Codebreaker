package io.github.haykam821.codebreaker.game.code;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.core.Direction;

public enum CodeResult {
	HIT(Blocks.ACACIA_BUTTON),
	BLOW(Blocks.STONE_BUTTON);

	public static final BlockState EMPTY = Blocks.BIRCH_BUTTON.defaultBlockState()
		.setValue(ButtonBlock.FACING, Direction.SOUTH)
		.setValue(ButtonBlock.POWERED, true);

	private final BlockState state;

	private CodeResult(BlockState state) {
		this.state = state;
	}

	private CodeResult(Block block) {
		this(block.defaultBlockState()
			.setValue(ButtonBlock.FACING, Direction.SOUTH)
			.setValue(ButtonBlock.POWERED, true));
	}

	public BlockState getState() {
		return this.state;
	}
}