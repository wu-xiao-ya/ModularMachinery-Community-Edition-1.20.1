package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

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
    public MachineControllerBlockEntity getController() {
        return controller;
    }
    public String getMachineId() {
        return machineId == null ? "" : machineId.toString();
    }

    public ResourceLocation getMachineResourceLocation() {
        return machineId;
    }
    public String getType() {
        return type.name().toLowerCase(java.util.Locale.ROOT);
    }

    public MmceMachineEventType getEventType() {
        return type;
    }
    public MmceEventPhase getPhase() {
        return phase;
    }
    public boolean isCanceled() {
        return canceled;
    }
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
    public void cancel() {
        setCanceled(true);
    }
    public void addModifier(String key, MmceRecipeModifier modifier) {
        if (controller != null) {
            controller.addModifier(key, modifier);
        }
    }
    public void removeModifier(String key) {
        if (controller != null) {
            controller.removeModifier(key);
        }
    }
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        if (controller != null) {
            controller.addPermanentModifier(key, modifier);
        }
    }
    public void removePermanentModifier(String key) {
        if (controller != null) {
            controller.removePermanentModifier(key);
        }
    }
    public boolean hasModifier(String key) {
        return controller != null && controller.hasModifier(key);
    }
    public boolean hasPermanentModifier(String key) {
        return controller != null && controller.hasPermanentModifier(key);
    }
    public int getControllerX() {
        return controllerPos().getX();
    }
    public int getControllerY() {
        return controllerPos().getY();
    }
    public int getControllerZ() {
        return controllerPos().getZ();
    }

    private BlockPos controllerPos() {
        return controller == null ? BlockPos.ZERO : controller.getBlockPos();
    }
}
