package io.github.haykam821.codebreaker.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class CodebreakerMap {
	private final MapTemplate template;
	private final BlockPos codeOrigin;
	private final BlockBounds bounds;

	public CodebreakerMap(MapTemplate template, BlockPos codeOrigin, BlockBounds bounds) {
		this.template = template;
		this.codeOrigin = codeOrigin;
		this.bounds = bounds;
	}

	public BlockPos getCodeOrigin() {
		return this.codeOrigin;
	}

	public BlockBounds getBounds() {
		return this.bounds;
	}

	public boolean isBelowPlatform(ServerPlayerEntity player) {
		return player.getY() < this.bounds.getMin().getY();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}