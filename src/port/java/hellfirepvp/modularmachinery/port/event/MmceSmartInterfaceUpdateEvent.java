package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class MmceSmartInterfaceUpdateEvent extends MmceMachineEvent {
    private final SmartInterfaceBlockEntity smartInterface;
    private final BlockPos interfacePos;
    private final String interfaceType;
    private final float oldValue;
    private final float newValue;

    public MmceSmartInterfaceUpdateEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            SmartInterfaceBlockEntity smartInterface,
            String interfaceType,
            float oldValue,
            float newValue
    ) {
        super(controller, machineId, MmceMachineEventType.SMART_INTERFACE_UPDATE, MmceEventPhase.END);
        this.smartInterface = smartInterface;
        this.interfacePos = smartInterface == null ? BlockPos.ZERO : smartInterface.getBlockPos();
        this.interfaceType = interfaceType == null ? "" : interfaceType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    public SmartInterfaceBlockEntity getSmartInterface() {
        return smartInterface;
    }
    public String getInterfaceType() {
        return interfaceType;
    }
    public float getOldValue() {
        return oldValue;
    }
    public float getNewValue() {
        return newValue;
    }
    public int getInterfaceX() {
        return interfacePos.getX();
    }
    public int getInterfaceY() {
        return interfacePos.getY();
    }
    public int getInterfaceZ() {
        return interfacePos.getZ();
    }
}
