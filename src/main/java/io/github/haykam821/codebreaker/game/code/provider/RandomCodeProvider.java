package io.github.haykam821.codebreaker.game.code.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.block.Block;

public class RandomCodeProvider implements CodeProvider {
	public static final Codec<RandomCodeProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("spaces").forGetter(provider -> provider.spaces)
		).apply(instance, RandomCodeProvider::new);
	});

	private final int spaces;

	public RandomCodeProvider(int spaces) {
		this.spaces = spaces;
	}

	public Code generate(Random random, CodebreakerConfig config) {
		Code code = new Code(this.spaces);
		List<Block> allPegs = config.getCodePegs().values();

		List<Block> pegs = new ArrayList<>(allPegs);
		for (int index = 0; index < this.spaces; index++) {
			// Refill peg choices if empty
			if (pegs.size() == 0) {
				pegs = new ArrayList<>(allPegs);
			}

			int pegIndex = random.nextInt(pegs.size());
			code.setPeg(index, pegs.get(pegIndex).getDefaultState());
			pegs.remove(pegIndex);
		}

		return code;
	}

	@Override
	public Codec<RandomCodeProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "RandomCodeProvider{spaces=" + this.spaces + "}";
	}
}
