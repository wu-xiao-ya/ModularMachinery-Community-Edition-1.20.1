package hellfirepvp.modularmachinery.port.integration.kubejs;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class MmceKubeJSMachineController {
    private final MachineControllerBlockEntity controller;

    private MmceKubeJSMachineController(MachineControllerBlockEntity controller) {
        this.controller = controller;
    }

    public static MmceKubeJSMachineController of(MachineControllerBlockEntity controller) {
        return controller == null ? null : new MmceKubeJSMachineController(controller);
    }

    public String getMachineId() {
        return controller.getMachineId().map(Object::toString).orElse("");
    }

    public String getActiveRecipeId() {
        return controller.getActiveRecipeId().map(Object::toString).orElse("");
    }

    public String getStatus() {
        return controller.getRecipeStatus().serializedName();
    }

    public String getStatusDetail() {
        String detail = controller.getRecipeStatusDetail();
        return detail == null ? "" : detail;
    }

    public int getProgress() {
        return controller.getRecipeProgress();
    }

    public int getRecipeTime() {
        return controller.getActiveRecipeId()
                .map(MmceDataRegistry.snapshot().recipes()::get)
                .map(MmceRecipeDefinition::recipeTime)
                .orElse(0);
    }

    public int getParallelism() {
        return controller.getActiveRecipeParallelism();
    }

    public boolean isWorking() {
        return controller.isWorking();
    }

    public boolean isStructureFormed() {
        return controller.isStructureFormed();
    }

    public BlockPos getBlockPos() {
        return controller.getBlockPos();
    }

    public Level getLevel() {
        return controller.getLevel();
    }

    public void addModifier(String key, MmceRecipeModifier modifier) {
        controller.addModifier(key, modifier);
    }

    public void removeModifier(String key) {
        controller.removeModifier(key);
    }

    public boolean hasModifier(String key) {
        return controller.hasModifier(key);
    }

    MachineControllerBlockEntity unwrap() {
        return controller;
    }
}
