package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.Codebreaker;
import io.github.haykam821.codebreaker.game.CbConfig;
import io.github.haykam821.codebreaker.game.map.generic.CbGenericMapConfig;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Random;

public class CbMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
	private static final int FLOOR_WIDTH = 18;

	private final CbConfig config;

	public CbMapBuilder(CbConfig config) {
		this.config = config;
	}

	public CbMap create() {
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

	public CbMap buildDefault(CbGenericMapConfig mapConfig) {
		MapTemplate template = MapTemplate.createEmpty();
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

		CbMap map = new CbMap(template, new BlockPos(floorBounds.getCenter()));

		map.addControlPad(new CbControlPad(controlPadOrigin, Direction.EAST, Codebreaker.CODE_PEGS.values()));
		map.addBoard(new CbBoard(codeOrigin, Direction.SOUTH));

		return map;
	}

	// TODO: Implement custom map loading
	private CbMap buildFromTemplate(MapTemplate template) {
		return this.buildDefault(new CbGenericMapConfig(new SimpleBlockStateProvider(Blocks.STONE.getDefaultState())));
	}
}