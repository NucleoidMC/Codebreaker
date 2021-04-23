package io.github.haykam821.codebreaker.game.map;

import java.util.Random;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class CodebreakerMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
	private static final int FLOOR_WIDTH = 18;

	private final CodebreakerConfig config;

	public CodebreakerMapBuilder(CodebreakerConfig config) {
		this.config = config;
	}

	public CodebreakerMap create(Random random, Code correctCode) {
		MapTemplate template = MapTemplate.createEmpty();

		CodebreakerMapConfig mapConfig = this.config.getMapConfig();
		int spaces = correctCode.getLength();

		// Board
		BlockPos boardOrigin = ORIGIN.add(1, 1, 1);
		BlockBounds boardBounds = new BlockBounds(boardOrigin, boardOrigin.add(this.config.getChances() + 1, spaces * 2, 0));
		for (BlockPos pos : boardBounds) {
			template.setBlockState(pos, mapConfig.getBoardProvider().getBlockState(random, pos));
		}

		BlockPos codeOrigin = boardOrigin.add(1, spaces * 2 - 1, 0);
		template.setBlockState(codeOrigin, Blocks.EMERALD_BLOCK.getDefaultState());

		// Floor
		BlockPos floorOrigin = ORIGIN.add(0, 0, 0);
		BlockBounds floorBounds = new BlockBounds(floorOrigin, floorOrigin.add(this.config.getChances() + 3, 0, FLOOR_WIDTH - 1));
		for (BlockPos pos : floorBounds) {
			template.setBlockState(pos, mapConfig.getFloorProvider().getBlockState(random, pos));
		}

		// Code controls
		int codeControls = Main.CODE_PEGS.values().size();
		BlockPos codeControlOrigin = ORIGIN.add(floorBounds.getSize().getX() / 2 - codeControls / 2, 0, 6);
		BlockBounds codeControlBounds = new BlockBounds(codeControlOrigin, codeControlOrigin.add(codeControls, 0, 0));
		int codeControlIndex = 0;
		for (BlockPos pos : codeControlBounds) {
			if (codeControlIndex < codeControls) {
				template.setBlockState(pos, Main.CODE_PEGS.values().get(codeControlIndex).getDefaultState());
			} else {
				template.setBlockState(pos, Blocks.BEDROCK.getDefaultState());
			}
			codeControlIndex += 1;
		}

		return new CodebreakerMap(template, codeOrigin, floorBounds);
	}
}