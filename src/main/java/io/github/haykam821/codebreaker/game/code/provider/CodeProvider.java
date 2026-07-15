package io.github.haykam821.codebreaker.game.code.provider;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;

import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

public interface CodeProvider {
	public static final TinyRegistry<MapCodec<? extends CodeProvider>> REGISTRY = TinyRegistry.create();
	public static final MapCodec<CodeProvider> TYPE_CODEC = REGISTRY.dispatchMap(CodeProvider::getCodec, Function.identity());
	
	public Code generate(RandomSource random, CodebreakerConfig config);
	public boolean hasDuplicatePegs(CodebreakerConfig config);

	public MapCodec<? extends CodeProvider> getCodec();
}
