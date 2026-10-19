package petrolpark.mc.destroy.core.chemistry.vat.observation;

import petrolpark.mc.destroy.DestroyMessages;
import petrolpark.mc.destroy.client.DestroyGuiTextures;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity;

public class RedstoneMonitorVatSideScreen extends AbstractQuantityObservingScreen {

    private final VatSideBlockEntity vatSide;

    public RedstoneMonitorVatSideScreen(VatSideBlockEntity vatSide) {
        super(vatSide.redstoneMonitor, DestroyLang.translate("tooltip.vat.menu.quantity_observed.title").component(), DestroyGuiTextures.VAT_QUANTITY_OBSERVER);
        this.vatSide = vatSide;
    };
    
    @Override
    protected int getEditBoxY() {
        return 35;
    };

    @Override
    protected void updateThresholds(float lower, float upper) {
        DestroyMessages.sendToServer(new RedstoneQuantityMonitorThresholdChangeC2SPacket(lower, upper, vatSide.getBlockPos()));
    }

};
