package petrolpark.mc.destroy.client;

import java.util.function.Supplier;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.content.confetti.ConfettoParticleData;
import petrolpark.mc.destroy.core.chemistry.hazard.mobeffect.TearParticle;
import petrolpark.mc.destroy.core.fluid.RainParticle;
import petrolpark.mc.destroy.core.fluid.TintedSplashParticle;
import petrolpark.mc.destroy.core.fluid.gasparticle.BoilingFluidBubbleParticleData;
import petrolpark.mc.destroy.core.fluid.gasparticle.GasParticleData;
import com.simibubi.create.foundation.particle.ICustomParticleData;
import net.createmod.catnip.lang.Lang;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public enum DestroyParticleTypes {

    BOILING_FLUID_BUBBLE(BoilingFluidBubbleParticleData::new),
    CONFETTO(ConfettoParticleData::new),
    WHITE_CONFETTO(ConfettoParticleData.White::new),
    DISTILLATION(GasParticleData::new),
    EVAPORATION(GasParticleData::new),
    RAIN(RainParticle.Data::new),
    TEAR(TearParticle.Data::new),
    TINTED_SPLASH(TintedSplashParticle.Data::new);

    private final ParticleEntry<? extends ParticleOptions> particleEntry;

    <T extends ParticleOptions> DestroyParticleTypes(Supplier<? extends ICustomParticleData<T>> typeProvider) {
        particleEntry = new ParticleEntry<T>(Lang.asId(name()), typeProvider); // Create an entry for this Particle
    };

    public static void register(IEventBus eventBus) {
        ParticleEntry.PARTICLE_TYPES.register(eventBus);
    };

    /**
     * Assign the right provider to every Particle Type.
     * For some unfathomable reason these are assigned in an event and not when the Particle Types themselves are registered.
     * @param event Event for registering Particle providers
     */
    @OnlyIn(Dist.CLIENT)
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        for (DestroyParticleTypes particleType : values()) {
            particleType.particleEntry.registerProvider(event);
        };
    };

    /**
     * Get this Particle Type.
     * @return
     */
    public ParticleType<?> get() {
		return particleEntry.object.get();
	};

    /**
     * Convinience class to register a Particle and also its provider when initialized.
     * Copied from the {@link com.simibubi.create.AllParticleTypes.ParticleEntry Create source code}. 
     */
    private static class ParticleEntry<T extends ParticleOptions> {
        private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(NeoForgeRegistries.PARTICLE_TYPES, Destroy.MOD_ID);

        private final String name; // ID of this particle
        private final Supplier<? extends ICustomParticleData<T>> typeProvider; // The class initializer for this Particle
        private final RegistryObject<ParticleType<T>> object; // This Particle's Registry entry

        public ParticleEntry(String name, Supplier<? extends ICustomParticleData<T>> typeProvider) {
            this.name = name;
            this.typeProvider = typeProvider;
            object = PARTICLE_TYPES.register(this.name, () -> typeProvider.get().createType()); // Register this Particle
        };

        @OnlyIn(Dist.CLIENT)
        public void registerProvider(RegisterParticleProvidersEvent event) {
            typeProvider.get().register(object.get(), event); // Assign the Particle provider
        };

    };
};
