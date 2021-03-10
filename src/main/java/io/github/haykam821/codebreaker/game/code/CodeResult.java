package io.github.haykam821.codebreaker.game.code;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;

public enum CodeResult {
	HIT(Blocks.ACACIA_BUTTON),
	BLOW(Blocks.STONE_BUTTON);

	public static final BlockState EMPTY = Blocks.BIRCH_BUTTON.getDefaultState()
		.with(AbstractButtonBlock.FACING, Direction.SOUTH)
		.with(AbstractButtonBlock.POWERED, true);

	private final BlockState state;

	CodeResult(BlockState state) {
		this.state = state;
	}

	private CodeResult(Block block) {
		this(block.getDefaultState()
			.with(AbstractButtonBlock.FACING, Direction.SOUTH)
			.with(AbstractButtonBlock.POWERED, true));
	}

	public BlockState getState() {
		return this.state;
	}
}