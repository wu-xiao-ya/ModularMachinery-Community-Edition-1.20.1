package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineEvent")
public class MmceMachineEvent {
    private final MachineControllerBlockEntity controller;
    private final ResourceLocation machineId;
    private final MmceMachineEventType type;
    private final MmceEventPhase phase;
    private boolean canceled;

    public MmceMachineEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            MmceMachineEventType type,
            MmceEventPhase phase
    ) {
        this.controller = controller;
        this.machineId = machineId;
        this.type = type;
        this.phase = phase;
    }

    @ZenCodeType.Getter("controller")
    public MachineControllerBlockEntity getController() {
        return controller;
    }

    @ZenCodeType.Getter("machineId")
    public String getMachineId() {
        return machineId == null ? "" : machineId.toString();
    }

    public ResourceLocation getMachineResourceLocation() {
        return machineId;
    }

    @ZenCodeType.Getter("type")
    public String getType() {
        return type.name().toLowerCase(java.util.Locale.ROOT);
    }

    public MmceMachineEventType getEventType() {
        return type;
    }

    @ZenCodeType.Getter("phase")
    public MmceEventPhase getPhase() {
        return phase;
    }

    @ZenCodeType.Getter("canceled")
    public boolean isCanceled() {
        return canceled;
    }

    @ZenCodeType.Setter("canceled")
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @ZenCodeType.Method
    public void cancel() {
        setCanceled(true);
    }

    @ZenCodeType.Method
    public void addModifier(String key, MmceRecipeModifier modifier) {
        if (controller != null) {
            controller.addModifier(key, modifier);
        }
    }

    @ZenCodeType.Method
    public void removeModifier(String key) {
        if (controller != null) {
            controller.removeModifier(key);
        }
    }

    @ZenCodeType.Method
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        if (controller != null) {
            controller.addPermanentModifier(key, modifier);
        }
    }

    @ZenCodeType.Method
    public void removePermanentModifier(String key) {
        if (controller != null) {
            controller.removePermanentModifier(key);
        }
    }

    @ZenCodeType.Method
    public boolean hasModifier(String key) {
        return controller != null && controller.hasModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String key) {
        return controller != null && controller.hasPermanentModifier(key);
    }

    @ZenCodeType.Getter("controllerX")
    public int getControllerX() {
        return controllerPos().getX();
    }

    @ZenCodeType.Getter("controllerY")
    public int getControllerY() {
        return controllerPos().getY();
    }

    @ZenCodeType.Getter("controllerZ")
    public int getControllerZ() {
        return controllerPos().getZ();
    }

    private BlockPos controllerPos() {
        return controller == null ? BlockPos.ZERO : controller.getBlockPos();
    }
}
