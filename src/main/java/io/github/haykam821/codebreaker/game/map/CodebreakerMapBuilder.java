package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.Codebreaker;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.map.generic.CodebreakerGenericMapConfig;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import xyz.nucleoid.plasmid.map.template.*;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Random;

public class CodebreakerMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
	private static final int FLOOR_WIDTH = 18;

	private final CodebreakerConfig config;

	public CodebreakerMapBuilder(CodebreakerConfig config) {
		this.config = config;
	}

	public CodebreakerMap create(MinecraftServer server) {
		return this.config.getMapConfig().map(
				this::buildDefault,
				path -> {
					MapTemplate template;
					try {
						template = MapTemplateSerializer.INSTANCE.loadFromResource(path);
					} catch (IOException e) {
						template = MapTemplate.createEmpty();
						Codebreaker.LOGGER.error("Failed to find map template at {}", path, e);
					}

					return this.buildFromTemplate(template);
				}
		);
	}

	public CodebreakerMap buildDefault(CodebreakerGenericMapConfig mapConfig) {
		MapTemplate template = MapTemplate.createEmpty();
		CodebreakerMap map = new CodebreakerMap(template);

		Random random = new Random();

		// Board
		BlockPos boardOrigin = ORIGIN.add(1, 1, 1);
		BlockBounds boardBounds = new BlockBounds(boardOrigin, boardOrigin.add(this.config.getChances() + 1, this.config.getSpaces() * 2, 0));
		for (BlockPos pos : boardBounds) {
			template.setBlockState(pos, config.getBoardProvider().getBlockState(random, pos));
		}

		BlockPos codeOrigin = boardOrigin.add(1, this.config.getSpaces() * 2 - 1, 0);
		template.setBlockState(codeOrigin, Blocks.EMERALD_BLOCK.getDefaultState());

		// Floor
		BlockPos floorOrigin = ORIGIN.add(0, 0, 0);
		BlockBounds floorBounds = new BlockBounds(floorOrigin, floorOrigin.add(this.config.getChances() + 3, 0, FLOOR_WIDTH - 1));
		for (BlockPos pos : floorBounds) {
			template.setBlockState(pos, mapConfig.getFloorProvider().getBlockState(random, pos));
		}

		// Control Pad
		BlockPos controlPadOrigin = ORIGIN.add(2 + floorBounds.getCenter().getX() - Codebreaker.CODE_PEGS.values().size(), 0, 6);

		map.addControlPad(new ControlPad(controlPadOrigin, Direction.EAST, Codebreaker.CODE_PEGS.values()));
		map.addBoard(new Board(codeOrigin, Direction.SOUTH));
		map.setSpawnPos(new BlockPos(floorBounds.getCenter()));
		map.setRulesPos(new BlockPos(floorBounds.getCenter()).add(0, 2.8, 9));

		return map;
	}

	private CodebreakerMap buildFromTemplate(MapTemplate template) {
		CodebreakerMap map = new CodebreakerMap(template);

		MapTemplateMetadata metadata = template.getMetadata();
		metadata.getRegions("board").forEach(region -> {
			BlockPos pos = new BlockPos(region.getBounds().getCenter());
			map.addBoard(new Board(pos, getDirectionForRegion(region)));
		});
		metadata.getRegions("control_pad").forEach(region -> {
			BlockPos pos = new BlockPos(region.getBounds().getCenter());
			map.addControlPad(new ControlPad(pos, getDirectionForRegion(region), Codebreaker.CODE_PEGS.values()));
		});
		BlockBounds rulesBounds = metadata.getFirstRegionBounds("rules");
		if (rulesBounds == null) {
			rulesBounds = template.getBounds();
		}

		BlockPos rulesPos = new BlockPos(rulesBounds.getCenter());
		map.setRulesPos(rulesPos);

		BlockBounds centerSpawnBounds = metadata.getFirstRegionBounds("spawn");
		if (centerSpawnBounds == null) {
			centerSpawnBounds = template.getBounds();
		}

		BlockPos spawnPos = new BlockPos(centerSpawnBounds.getCenter());
		spawnPos = template.getTopPos(spawnPos.getX(), spawnPos.getZ(), Heightmap.Type.WORLD_SURFACE).up();

		map.setSpawnPos(spawnPos);

		return map;
	}

	private static Direction getDirectionForRegion(TemplateRegion region) {
		String key = region.getData().getString("direction");
		for (Direction direction : Direction.values()) {
			if (direction.getName().equalsIgnoreCase(key)) {
				return direction;
			}
		}
		return Direction.NORTH;
	}
}