package io.github.haykam821.codebreaker.game.code.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.block.Block;

public class RandomCodeGenerator implements CodeGenerator {
	public static final Codec<RandomCodeGenerator> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("spaces").forGetter(generator -> generator.spaces)
		).apply(instance, RandomCodeGenerator::new);
	});

	private final int spaces;

	public RandomCodeGenerator(int spaces) {
		this.spaces = spaces;
	}

	public Code generate(Random random) {
		Code code = new Code(this.spaces);

		List<Block> pegs = new ArrayList<>(Main.CODE_PEGS.values());
		for (int index = 0; index < this.spaces; index++) {
			// Refill peg choices if empty
			if (pegs.size() == 0) {
				pegs = new ArrayList<>(Main.CODE_PEGS.values());
			}

			int pegIndex = random.nextInt(pegs.size());
			code.setPeg(index, pegs.get(pegIndex).getDefaultState());
			pegs.remove(pegIndex);
		}

		return code;
	}

	@Override
	public Codec<RandomCodeGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "RandomCodeGenerator{spaces=" + this.spaces + "}";
	}
}
