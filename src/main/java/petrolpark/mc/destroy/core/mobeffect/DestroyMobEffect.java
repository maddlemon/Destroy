package petrolpark.mc.destroy.core.mobeffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

@MoveToPetrolparkLibrary
public class DestroyMobEffect extends MobEffect {
    
    public DestroyMobEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    };

    /**
     * 1.20.1's way of adding an Attribute Modifier. The UUID becomes the modifier's ID ({@code destroy:<uuid>}), so effects which shared a
     * UUID in 1.20.1 still share (and replace each other's) modifier.
     */
    protected DestroyMobEffect addAttributeModifier(Holder<Attribute> attribute, String uuid, double amount, AttributeModifier.Operation operation) {
        addAttributeModifier(attribute, Destroy.asResource(uuid), amount, operation);
        return this;
    };

    /**
     * 1.20.1's {@code MobEffect#addAttributeModifiers(LivingEntity, AttributeMap, int)}: called (server-side) when this effect is added to an
     * entity, or forcibly updated, after the Attribute Modifiers. 1.21 has no entity-aware version; it is called by {@code LivingEntityMixin}.
     */
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {};

    /**
     * 1.20.1's {@code MobEffect#removeAttributeModifiers(LivingEntity, AttributeMap, int)}: called (server-side) when this effect is removed
     * from an entity (including when it runs out), or forcibly updated, after the Attribute Modifiers are removed. See {@link #addAttributeModifiers(LivingEntity, AttributeMap, int)}.
     */
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {};

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new DestroyMobEffectExtensions());
    };

    public static class DestroyMobEffectExtensions implements IClientMobEffectExtensions {

        public void addToTooltip(List<Component> tooltip, MobEffectInstance instance) {
            String descriptionId = instance.getDescriptionId() + ".description";
            if (I18n.exists(descriptionId)) {
                tooltip.add(Component.literal(" "));
                tooltip.addAll(TooltipHelper.cutTextComponent(Component.translatable(descriptionId), Palette.GRAY));
            };
        };

        public void renderTooltip(EffectRenderingInventoryScreen<? extends AbstractContainerMenu> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, MobEffectInstance instance) {
            Minecraft minecraft = Minecraft.getInstance();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(screen.getEffectName(instance));
            tooltip.add(MobEffectUtil.formatDuration(instance, 1f, minecraft.level == null ? 20f : minecraft.level.tickRateManager().tickrate()));
            addToTooltip(tooltip, instance);
            guiGraphics.renderTooltip(minecraft.font, tooltip, Optional.empty(), mouseX, mouseY);
        };
    };
};
