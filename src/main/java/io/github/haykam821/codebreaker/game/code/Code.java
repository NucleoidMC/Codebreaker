package io.github.haykam821.codebreaker.game.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.haykam821.codebreaker.Codebreaker;
import io.github.haykam821.codebreaker.game.CbConfig;
import io.github.haykam821.codebreaker.game.map.CbBoard;
import io.github.haykam821.codebreaker.game.map.CbMapBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class Code {
	private final BlockState[] pegs;

	public Code(int spaces) {
		this.pegs = new BlockState[spaces];
	}

	public Code(BlockState[] pegs) {
		this.pegs = pegs;
	}

	public BlockState[] getPegs() {
		return this.pegs;
	}

	public void setPeg(int index, BlockState state) {
		this.pegs[index] = state;
	}

	public void setNext(BlockState state) {
		for (int index = 0; index < this.pegs.length; index++) {
			if (this.pegs[index] == null) {
				this.pegs[index] = state;
				return;
			}
		}
	}

	public boolean isCompletelyFilled() {
		for(BlockState peg : this.pegs) {
			if(peg == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder("Code{pegs=");
		for (int index = 0; index < this.pegs.length; index++) {
			BlockState state = this.pegs[index];
			string.append(state == null ? "<empty>" : state.toString());

			if (index + 1 < this.pegs.length) {
				string.append(", ");
			}
		}
		return string + "}";
	}

	/**
	 * Creates a code consisting of random pegs.
	 * @param spaces the number of spaces in the code
	 */
	public static Code createRandom(int spaces, Random random) {
		Code code = new Code(spaces);

		List<Block> pegs = new ArrayList<>(Codebreaker.CODE_PEGS.values());
		for (int index = 0; index < spaces; index++) {
			// Refill peg choices if empty
			if (pegs.size() == 0) {
				pegs = new ArrayList<>(Codebreaker.CODE_PEGS.values());
			}

			int pegIndex = random.nextInt(pegs.size());
			code.setPeg(index, pegs.get(pegIndex).getDefaultState());
			// TODO: Harder mode that should just remove this line right under
			pegs.remove(pegIndex);
		}

		return code;
	}
}