package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.Codebreaker;
import io.github.haykam821.codebreaker.game.CbConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class CbMap {
	private final MapTemplate template;
	private final BlockPos codeOrigin;
	private final BlockBounds floorBounds;
	private final BlockBounds pegBounds;

	public CbMap(MapTemplate template, BlockPos codeOrigin, BlockBounds floorBounds, BlockBounds pegBounds) {
		this.template = template;
		this.codeOrigin = codeOrigin;
		this.floorBounds = floorBounds;
		this.pegBounds = pegBounds;
	}

	public BlockPos getCodeOrigin() {
		return this.codeOrigin;
	}

	public BlockBounds getFloorBounds() {
		return this.floorBounds;
	}

	public BlockBounds getPegBounds() {
		return pegBounds;
	}

	public boolean isBelowPlatform(ServerPlayerEntity player) {
		return player.getY() < this.floorBounds.getMin().getY();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}