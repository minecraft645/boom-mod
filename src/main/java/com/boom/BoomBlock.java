package com.boom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BoomBlock extends Block {
    public enum Kind { DINAMITA, TNT_5X }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final int FUSE_TICKS = 60; // 3 segundos

    private final Kind kind;

    public BoomBlock(Kind kind, Properties props) {
        super(props);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    // Se enciende con encendedor o bola de fuego
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (state.getValue(LIT)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setBlock(pos, state.setValue(LIT, true), 3);
            serverLevel.scheduleTick(pos, this, FUSE_TICKS);
            serverLevel.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    // Humo mientras arde la mecha
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0.05, 0);
        }
    }

    // Aqui explota
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        level.removeBlock(pos, false);
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;

        if (kind == Kind.TNT_5X) {
            // Explosion fuerte (~5 TNT juntos)
            level.explode(null, x, y, z, 8.0f, Level.ExplosionInteraction.TNT);
        } else {
            // Efecto visual/sonido sin romper bloques (para no destruir los items)
            level.explode(null, x, y, z, 1.5f, Level.ExplosionInteraction.NONE);
            // Rompe 2 de largo x 2 de ancho x 2 de alto, soltando los items
            Direction right = facing.getClockWise();
            for (int f = 1; f <= 2; f++) {
                for (int s = 0; s <= 1; s++) {
                    for (int h = 0; h <= 1; h++) {
                        BlockPos p = pos.relative(facing, f).relative(right, s).above(h);
                        BlockState bs = level.getBlockState(p);
                        if (bs.isAir() || !bs.getFluidState().isEmpty()) continue;
                        if (bs.getDestroySpeed(level, p) < 0) continue; // bedrock etc.
                        level.destroyBlock(p, true);
                    }
                }
            }
        }
    }
}
