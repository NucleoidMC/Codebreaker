package io.github.haykam821.codebreaker.game.code.generator;

import java.util.Random;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.haykam821.codebreaker.game.code.Code;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public interface CodeGenerator {
	public static final TinyRegistry<Codec<? extends CodeGenerator>> REGISTRY = TinyRegistry.newStable();
	public static final MapCodec<CodeGenerator> TYPE_CODEC = REGISTRY.dispatchMap(CodeGenerator::getCodec, Function.identity());
	
	public Code generate(Random random);

	public Codec<? extends CodeGenerator> getCodec();
}
