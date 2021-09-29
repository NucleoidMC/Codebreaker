package io.github.haykam821.codebreaker.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public final class CodebreakerMap {
	private final MapTemplate template;
	private final BlockPos codeOrigin;
	private final BlockBounds bounds;
	private final Vec3d spawnPos;

	public CodebreakerMap(MapTemplate template, BlockPos codeOrigin, BlockBounds bounds) {
		this.template = template;
		this.codeOrigin = codeOrigin;
		this.bounds = bounds;

		Vec3d centerPos = this.bounds.center();
		this.spawnPos = new Vec3d(centerPos.getX(), 65, centerPos.getZ());
	}

	public BlockPos getCodeOrigin() {
		return this.codeOrigin;
	}

	public BlockBounds getBounds() {
		return this.bounds;
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	public boolean isBelowPlatform(ServerPlayerEntity player) {
		return player.getY() < this.bounds.min().getY();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}