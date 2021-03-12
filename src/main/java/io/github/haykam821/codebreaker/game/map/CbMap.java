package io.github.haykam821.codebreaker.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.List;

public final class CbMap {
	private final MapTemplate template;
	private final BlockPos spawnPos;
	private final List<CbBoard> boards = new ArrayList<>();
	private final List<CbControlPad> controlPads = new ArrayList<>();

	public CbMap(MapTemplate template, BlockPos spawnPos) {
		this.template = template;
		this.spawnPos = spawnPos;
	}

	public BlockPos getSpawnPos() {
		return this.spawnPos;
	}

	public void addBoard(CbBoard board) {
		boards.add(board);
	}

	public List<CbBoard> getBoards() {
		return boards;
	}

	public void addControlPad(CbControlPad controlPad) {
		controlPads.add(controlPad);
	}

	public List<CbControlPad> getControlPads() {
		return controlPads;
	}

	public boolean isBelowZero(ServerPlayerEntity player) {
		return player.getY() < 0;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}