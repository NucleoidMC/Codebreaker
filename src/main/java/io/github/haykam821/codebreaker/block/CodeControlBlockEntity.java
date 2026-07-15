package io.github.haykam821.codebreaker.block;

import org.joml.Matrix4x3f;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import io.github.haykam821.codebreaker.Main;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class CodeControlBlockEntity extends BlockEntity {
	private static final BlockState DEFAULT_BLOCK = Blocks.AIR.defaultBlockState();

	protected static final String BLOCK_KEY = "block";

	private BlockState block = Blocks.AIR.defaultBlockState();

	private BlockDisplayElement element;
	private ElementHolder holder;

	public CodeControlBlockEntity(BlockPos pos, BlockState state) {
		super(Main.CODE_CONTROL_BLOCK_ENTITY, pos, state);
	}

	public BlockState getBlock() {
		return this.block;
	}

	public void setBlock(BlockState block) {
		this.block = block;
		this.setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (this.element != null) {
			this.element.setBlockState(this.block);
		}
	}

	@Override
	public void setRemoved() {
		if (this.holder != null) {
			this.holder.destroy();
		}
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		this.block = view.read(BLOCK_KEY, BlockState.CODEC).orElse(DEFAULT_BLOCK);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		view.store(BLOCK_KEY, BlockState.CODEC, this.block);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CodeControlBlockEntity blockEntity) {
		if (blockEntity.holder == null) {
			Matrix4x3f matrix = new Matrix4x3f();

			Direction facing = state.getValue(CodeControlBlock.FACING);
			matrix.rotate(facing.getRotation());

			matrix.rotateX(Mth.DEG_TO_RAD * 22.5f);
			matrix.scale(0.5f, 0.5f, 0.25f);
			matrix.translate(0f, -0.2f, -1.55f);
			matrix.rotateX((float) Math.PI);
			matrix.translate(-0.5f, -0.5f, -0.5f);

			blockEntity.element = new BlockDisplayElement(blockEntity.getBlock());
			blockEntity.element.setTransformation(matrix);

			blockEntity.holder = new ElementHolder();
			blockEntity.holder.addElement(blockEntity.element);

			BlockBoundAttachment.ofTicking(blockEntity.holder, (ServerLevel) level, pos);
		}
	}
}
