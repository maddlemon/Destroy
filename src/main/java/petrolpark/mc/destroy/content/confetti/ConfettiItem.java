package petrolpark.mc.destroy.content.confetti;

import net.createmod.catnip.platform.CatnipServices;
import petrolpark.mc.destroy.core.explosion.SmartExplosion;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ISpecialEffectExplosiveItem;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class ConfettiItem extends Item implements ISpecialEffectExplosiveItem {

    public final Supplier<ParticleOptions> particleFactory;

    public ConfettiItem(Properties properties, Supplier<ParticleOptions> particleFactory) {
        super(properties);
        DispenserBlock.registerBehavior(this, new ConfettiDispenserBehaviour());
        this.particleFactory = particleFactory;
    };

    @Override
    public void explode(SmartExplosion explosion, Level level, ObjectArrayList<BlockPos> toBlow, ItemStack specialItemStack) {
        for (int i = 0; i < 512; i++) {
            level.addParticle(particleFactory.get(), explosion.center().x, explosion.center().y(), explosion.center().z, -0.25d + level.random.nextDouble() * 0.5d, -0.25d + level.random.nextDouble() * 0.5d, -0.25d + level.random.nextDouble() * 0.5d);
        };
    };

    public class ConfettiDispenserBehaviour extends OptionalDispenseItemBehavior {

        @Override
        @SuppressWarnings("resource")
        protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
            Direction facing = blockSource.state().getValue(DispenserBlock.FACING);
            CatnipServices.NETWORK.sendToClientsAround(blockSource.level(), blockSource.center(), 32d, new ConfettiBurstS2CPacket(stack, Vec3.atCenterOf(blockSource.pos()).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5f)), Vec3.atLowerCornerOf(facing.getNormal()).scale(0.25f)));
            stack.shrink(1);
            return stack;
        };
    };
    
};
