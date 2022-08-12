package io.github.haykam821.codebreaker.game.code.provider;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public interface CodeProvider {
	public static final TinyRegistry<Codec<? extends CodeProvider>> REGISTRY = TinyRegistry.create();
	public static final MapCodec<CodeProvider> TYPE_CODEC = REGISTRY.dispatchMap(CodeProvider::getCodec, Function.identity());
	
	public Code generate(Random random, CodebreakerConfig config);

	public Codec<? extends CodeProvider> getCodec();
}
