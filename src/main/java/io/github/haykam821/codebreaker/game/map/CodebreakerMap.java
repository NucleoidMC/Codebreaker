package io.github.haykam821.codebreaker.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

public final class CodebreakerMap {
	private final MapTemplate template;
	private final BlockPos codeOrigin;
	private final BlockBounds bounds;
	private final Vec3 spawnPos;

	public CodebreakerMap(MapTemplate template, BlockPos codeOrigin, BlockBounds bounds) {
		this.template = template;
		this.codeOrigin = codeOrigin;
		this.bounds = bounds;

		Vec3 centerPos = this.bounds.center();
		this.spawnPos = new Vec3(centerPos.x(), 65, centerPos.z());
	}

	public BlockPos getCodeOrigin() {
		return this.codeOrigin;
	}

	public BlockBounds getBounds() {
		return this.bounds;
	}

	public Vec3 getSpawnPos() {
		return this.spawnPos;
	}

	public boolean isBelowPlatform(ServerPlayer player) {
		return player.getY() < this.bounds.min().getY();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}