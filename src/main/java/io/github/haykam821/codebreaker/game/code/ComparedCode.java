package io.github.haykam821.codebreaker.game.code;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class ComparedCode extends Code {
	private final List<CodeResult> results = new ArrayList<>();

	public ComparedCode(BlockState[] pegs, Code otherCode) {
		super(pegs);

		List<BlockState> pegList = Lists.newArrayList(pegs);
		for (int index = 0; index < pegList.size(); index++) {
			if (pegs[index] == otherCode.getPegs()[index]) {
				this.results.add(0, CodeResult.HIT);
			} else if (pegList.contains(otherCode.getPegs()[index])) {
				this.results.add(CodeResult.BLOW);
			}
		}
	}

	public boolean isCorrect() {
		// Since the results may be shorter than the pegs, length must be checked first
		if (this.results.size() < this.getPegs().length) {
			return false;
		}

		for (CodeResult result : this.results) {
			if (result != CodeResult.HIT) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void build(WorldAccess world, BlockPos originPos) {
		super.build(world, originPos);
		
		BlockPos.Mutable pos = originPos.mutableCopy().move(0, -this.getPegs().length, 1);
		for (int index = 0; index < this.getPegs().length; index++) {
			if (index >= this.results.size()) {
				world.setBlockState(pos, CodeResult.EMPTY, 3);
			} else {
				CodeResult result = this.results.get(index);
				world.setBlockState(pos, result == null ? CodeResult.EMPTY : result.getState(), 3);
			}

			pos.move(Direction.DOWN);
		}
	}
}