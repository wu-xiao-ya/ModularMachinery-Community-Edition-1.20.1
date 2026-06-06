package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.SmartInterfaceUpdateEvent")
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

    @ZenCodeType.Getter("smartInterface")
    public SmartInterfaceBlockEntity getSmartInterface() {
        return smartInterface;
    }

    @ZenCodeType.Getter("interfaceType")
    public String getInterfaceType() {
        return interfaceType;
    }

    @ZenCodeType.Getter("oldValue")
    public float getOldValue() {
        return oldValue;
    }

    @ZenCodeType.Getter("newValue")
    public float getNewValue() {
        return newValue;
    }

    @ZenCodeType.Getter("interfaceX")
    public int getInterfaceX() {
        return interfacePos.getX();
    }

    @ZenCodeType.Getter("interfaceY")
    public int getInterfaceY() {
        return interfacePos.getY();
    }

    @ZenCodeType.Getter("interfaceZ")
    public int getInterfaceZ() {
        return interfacePos.getZ();
    }
}
