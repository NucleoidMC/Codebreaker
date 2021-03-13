package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class ControlPad {
	private final CodebreakerConfig config;
	private final BlockPos leftPos;
	private final Direction direction;

	public ControlPad(CodebreakerConfig config, BlockPos leftPos, Direction direction) {
		this.config = config;
		this.leftPos = leftPos;
		this.direction = direction;
	}

	public void build(WorldAccess world, boolean confirm) {
		BlockPos.Mutable pos = new BlockPos(leftPos).mutableCopy();
		for(Block peg : config.getPegTag().values()) {
			world.setBlockState(pos, confirm ? config.getConfirmBlock() : peg.getDefaultState(), 3);
			pos.move(direction, 1);
		}
		world.setBlockState(pos, config.getResetBlock(), 3);
	}

	public BlockBounds getBounds() {
		return new BlockBounds(leftPos, leftPos.offset(direction, config.getPegTag().values().size()));
	}
}
