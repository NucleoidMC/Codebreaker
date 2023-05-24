package io.github.haykam821.codebreaker.block;

import java.util.Optional;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import io.github.haykam821.codebreaker.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CodeControlBlock extends BlockWithEntity implements PolymerBlock {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public CodeControlBlock(Block.Settings settings) {
		super(settings);

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		Optional<CodeControlBlockEntity> maybeBlockEntity = world.getBlockEntity(pos, Main.CODE_CONTROL_BLOCK_ENTITY);

		if (maybeBlockEntity.isPresent()) {
			CodeControlBlockEntity blockEntity = maybeBlockEntity.get();

			if (blockEntity.getBlock().isAir()) {
				ItemStack stack = player.getStackInHand(hand);

				if (stack.getItem() instanceof BlockItem blockItem) {
					BlockState block = blockItem.getBlock().getDefaultState();
					blockEntity.setBlock(block);

					return ActionResult.SUCCESS;
				}
			}
		}

		return ActionResult.FAIL;
	}

	@Override
	public Block getPolymerBlock(BlockState state) {
		return Blocks.LECTERN;
	}

	@Override
	public BlockState getPolymerBlockState(BlockState state) {
		return this.getPolymerBlock(state).getDefaultState().with(FACING, state.get(FACING));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		var facing = context.getHorizontalPlayerFacing().getOpposite();
		return super.getPlacementState(context).with(FACING, facing);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CodeControlBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient() ? null : checkType(type, Main.CODE_CONTROL_BLOCK_ENTITY, CodeControlBlockEntity::tick);
	}
}
