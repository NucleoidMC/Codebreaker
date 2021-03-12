package io.github.haykam821.codebreaker.game.code;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;

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

	public List<CodeResult> getResults() {
		return results;
	}
}