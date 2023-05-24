package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.block.CodeControlBlock;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class CodebreakerMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
	private static final int FLOOR_WIDTH = 18;

	private static final BlockState CODE_CONTROL = Main.CODE_CONTROL.getDefaultState().with(CodeControlBlock.FACING, Direction.SOUTH);

	private final CodebreakerConfig config;

	public CodebreakerMapBuilder(CodebreakerConfig config) {
		this.config = config;
	}

	public CodebreakerMap create(Random random, Code correctCode, RegistryEntryList<Block> codePegs) {
		MapTemplate template = MapTemplate.createEmpty();

		CodebreakerMapConfig mapConfig = this.config.getMapConfig();
		int spaces = correctCode.getLength();

		// Board
		BlockPos boardOrigin = ORIGIN.add(1, 1, 1);
		BlockBounds boardBounds = BlockBounds.of(boardOrigin, boardOrigin.add(this.config.getChances() + 1, spaces * 2, 0));
		for (BlockPos pos : boardBounds) {
			template.setBlockState(pos, mapConfig.getBoardProvider().get(random, pos));
		}

		BlockPos codeOrigin = boardOrigin.add(1, spaces * 2 - 1, 0);
		template.setBlockState(codeOrigin, Blocks.EMERALD_BLOCK.getDefaultState());

		// Floor
		BlockPos floorOrigin = ORIGIN.add(0, 0, 0);
		BlockBounds floorBounds = BlockBounds.of(floorOrigin, floorOrigin.add(this.config.getChances() + 3, 0, FLOOR_WIDTH - 1));
		for (BlockPos pos : floorBounds) {
			template.setBlockState(pos, mapConfig.getFloorProvider().get(random, pos));
		}

		// Code controls
		int codeControls = codePegs.size();
		BlockPos codeControlOrigin = ORIGIN.add(floorBounds.size().getX() / 2 - codeControls / 2, 1, 6);
		BlockBounds codeControlBounds = BlockBounds.of(codeControlOrigin, codeControlOrigin.add(codeControls, 0, 0));
		int codeControlIndex = 0;
		for (BlockPos pos : codeControlBounds) {
			CodeControlBlockEntity blockEntity = new CodeControlBlockEntity(pos, CODE_CONTROL);

			if (codeControlIndex < codeControls) {
				blockEntity.setBlock(codePegs.get(codeControlIndex).value().getDefaultState());
			} else {
				blockEntity.setBlock(Blocks.BEDROCK.getDefaultState());
			}

			template.setBlockState(pos, CODE_CONTROL);
			template.setBlockEntity(pos, blockEntity);

			codeControlIndex += 1;
		}

		return new CodebreakerMap(template, codeOrigin, floorBounds);
	}
}