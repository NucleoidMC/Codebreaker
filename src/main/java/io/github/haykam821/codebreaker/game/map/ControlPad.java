package io.github.haykam821.codebreaker.game.map;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.List;

public class ControlPad {
	private final BlockPos leftPos;
	private final Direction direction;
	private final List<Block> pegs;

	public ControlPad(BlockPos leftPos, Direction direction, List<Block> pegs) {
		this.leftPos = leftPos;
		this.direction = direction;
		this.pegs = pegs;
	}

	public void build(WorldAccess world, boolean confirm) {
		BlockPos.Mutable pos = new BlockPos(leftPos).mutableCopy();
		for(Block peg : pegs) {
			world.setBlockState(pos, (confirm ? Blocks.SEA_LANTERN : peg).getDefaultState(), 3);
			pos.move(direction, 1);
		}
		world.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), 3);
	}

	public Direction getDirection() {
		return direction;
	}

	public BlockPos getLeftPos() {
		return leftPos;
	}

	public List<Block> getPegs() {
		return pegs;
	}

	public BlockBounds getBounds() {
		return new BlockBounds(leftPos, leftPos.offset(direction, pegs.size()));
	}
}
