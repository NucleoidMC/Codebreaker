package io.github.haykam821.codebreaker.game.code;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.util.math.Direction;

public enum CodeResult {
	HIT(Blocks.ACACIA_BUTTON),
	BLOW(Blocks.STONE_BUTTON);

	public static final BlockState EMPTY = Blocks.BIRCH_BUTTON.getDefaultState()
		.with(ButtonBlock.FACING, Direction.SOUTH)
		.with(ButtonBlock.POWERED, true);

	private final BlockState state;

	private CodeResult(BlockState state) {
		this.state = state;
	}

	private CodeResult(Block block) {
		this(block.getDefaultState()
			.with(ButtonBlock.FACING, Direction.SOUTH)
			.with(ButtonBlock.POWERED, true));
	}

	public BlockState getState() {
		return this.state;
	}
}