package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.game.CbConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.code.CodeResult;
import io.github.haykam821.codebreaker.game.code.ComparedCode;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class CbBoard {
	private final BlockPos topLeftPos;
	private final Direction facingDirection;

	public CbBoard(BlockPos topLeftPos, Direction facingDirection) {
		this.topLeftPos = topLeftPos;
		this.facingDirection = facingDirection;
	}

	public BlockPos getTopLeftPos() {
		return topLeftPos;
	}

	public Direction getFacingDirection() {
		return facingDirection;
	}

	public void build(WorldAccess world, CbConfig config, Code code, int index) {
		BlockPos.Mutable pos = new BlockPos(getTopLeftPos()).mutableCopy().move(getFacingDirection().rotateYCounterclockwise(), index);
		for(BlockState state : code.getPegs()) {
			world.setBlockState(pos, state == null ? config.getBoardProvider().getBlockState(world.getRandom(), pos) : state, 3);
			pos.move(Direction.DOWN);
		}
	}

	public void buildResult(WorldAccess world, CbConfig config, ComparedCode comparedCode, int index) {
		BlockPos.Mutable pos = new BlockPos(getTopLeftPos()).mutableCopy().move(getFacingDirection().rotateYCounterclockwise(), index).move(getFacingDirection(), 1).move(Direction.DOWN, comparedCode.getPegs().length);
		for (int i = 0; i < comparedCode.getPegs().length; i++) {
			if (i >= comparedCode.getResults().size()) {
				world.setBlockState(pos, CodeResult.EMPTY, 3);
			} else {
				CodeResult result = comparedCode.getResults().get(i);
				world.setBlockState(pos, result == null ? CodeResult.EMPTY : result.getState(), 3);
			}
			pos.move(Direction.DOWN);
		}
	}
}
