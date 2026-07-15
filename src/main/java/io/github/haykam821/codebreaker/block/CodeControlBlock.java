package io.github.haykam821.codebreaker.block;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import io.github.haykam821.codebreaker.Main;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class CodeControlBlock extends BaseEntityBlock implements PolymerBlock {
	public static final MapCodec<CodeControlBlock> CODEC = Block.simpleCodec(CodeControlBlock::new);

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public CodeControlBlock(Block.Properties settings) {
		super(settings);

		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		Optional<CodeControlBlockEntity> maybeBlockEntity = world.getBlockEntity(pos, Main.CODE_CONTROL_BLOCK_ENTITY);

		if (maybeBlockEntity.isPresent()) {
			CodeControlBlockEntity blockEntity = maybeBlockEntity.get();

			if (blockEntity.getBlock().isAir() && stack.getItem() instanceof BlockItem blockItem) {
				BlockState block = blockItem.getBlock().defaultBlockState();
				blockEntity.setBlock(block);

				return InteractionResult.SUCCESS_SERVER;
			}
		}

		return InteractionResult.FAIL;
	}

	@Override
	public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
		return Blocks.LECTERN.defaultBlockState().setValue(FACING, state.getValue(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		var facing = context.getHorizontalDirection().getOpposite();
		return super.getStateForPlacement(context).setValue(FACING, facing);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CodeControlBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide() ? null : createTickerHelper(type, Main.CODE_CONTROL_BLOCK_ENTITY, CodeControlBlockEntity::tick);
	}

	@Override
	protected MapCodec<? extends CodeControlBlock> codec() {
		return CODEC;
	}
}
