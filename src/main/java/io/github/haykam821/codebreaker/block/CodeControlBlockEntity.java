package io.github.haykam821.codebreaker.block;

import org.joml.Matrix4x3f;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import io.github.haykam821.codebreaker.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CodeControlBlockEntity extends BlockEntity {
	private static final BlockState DEFAULT_BLOCK = Blocks.AIR.getDefaultState();

	protected static final String BLOCK_KEY = "block";

	private BlockState block = Blocks.AIR.getDefaultState();

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
		this.markDirty();
	}

	@Override
	public void markDirty() {
		super.markDirty();

		if (this.element != null) {
			this.element.setBlockState(this.block);
		}
	}

	@Override
	public void markRemoved() {
		if (this.holder != null) {
			this.holder.destroy();
		}
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		this.block = view.read(BLOCK_KEY, BlockState.CODEC).orElse(DEFAULT_BLOCK);
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.put(BLOCK_KEY, BlockState.CODEC, this.block);
	}

	public static void tick(World world, BlockPos pos, BlockState state, CodeControlBlockEntity blockEntity) {
		if (blockEntity.holder == null) {
			Matrix4x3f matrix = new Matrix4x3f();

			Direction facing = state.get(CodeControlBlock.FACING);
			matrix.rotate(facing.getRotationQuaternion());

			matrix.rotateX(MathHelper.RADIANS_PER_DEGREE * 22.5f);
			matrix.scale(0.5f, 0.5f, 0.25f);
			matrix.translate(0f, -0.2f, -1.55f);
			matrix.rotateX((float) Math.PI);
			matrix.translate(-0.5f, -0.5f, -0.5f);

			blockEntity.element = new BlockDisplayElement(blockEntity.getBlock());
			blockEntity.element.setTransformation(matrix);

			blockEntity.holder = new ElementHolder();
			blockEntity.holder.addElement(blockEntity.element);

			BlockBoundAttachment.ofTicking(blockEntity.holder, (ServerWorld) world, pos);
		}
	}
}
