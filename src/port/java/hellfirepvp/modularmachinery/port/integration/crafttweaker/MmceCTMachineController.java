package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineController")
public final class MmceCTMachineController {
    private final MachineControllerBlockEntity controller;

    private MmceCTMachineController(MachineControllerBlockEntity controller) {
        this.controller = controller;
    }

    static MmceCTMachineController of(MachineControllerBlockEntity controller) {
        return controller == null ? null : new MmceCTMachineController(controller);
    }

    @ZenCodeType.Getter("machineId")
    public String getMachineId() {
        return controller.getMachineId().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("blueprintMachineId")
    public String getBlueprintMachineId() {
        return controller.getBlueprintMachineId().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("owner")
    public String getOwner() {
        return controller.getOwner().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("x")
    public int getX() {
        return controller.getBlockPos().getX();
    }

    @ZenCodeType.Getter("y")
    public int getY() {
        return controller.getBlockPos().getY();
    }

    @ZenCodeType.Getter("z")
    public int getZ() {
        return controller.getBlockPos().getZ();
    }

    @ZenCodeType.Getter("structureFormed")
    public boolean isStructureFormed() {
        return controller.isStructureFormed();
    }

    @ZenCodeType.Setter("structureFormed")
    public void setStructureFormed(boolean structureFormed) {
        controller.setStructureFormed(structureFormed);
    }

    @ZenCodeType.Getter("working")
    public boolean isWorking() {
        return controller.isWorking();
    }

    @ZenCodeType.Setter("working")
    public void setWorking(boolean working) {
        controller.setWorking(working);
    }

    @ZenCodeType.Getter("activeRecipe")
    public String getActiveRecipe() {
        return getActiveRecipeId();
    }

    @ZenCodeType.Getter("activeRecipeId")
    public String getActiveRecipeId() {
        return controller.getActiveRecipeId().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("progress")
    public int getProgress() {
        return controller.getRecipeProgress();
    }

    @ZenCodeType.Setter("progress")
    public void setProgress(int progress) {
        controller.setRecipeProgress(progress);
    }

    @ZenCodeType.Getter("parallelism")
    public int getParallelism() {
        return controller.getActiveRecipeParallelism();
    }

    @ZenCodeType.Setter("parallelism")
    public void setParallelism(int parallelism) {
        controller.setActiveRecipeParallelism(parallelism);
    }

    @ZenCodeType.Getter("activeInputGroupId")
    public int getActiveInputGroupId() {
        return controller.getActiveInputGroupId();
    }

    @ZenCodeType.Setter("activeInputGroupId")
    public void setActiveInputGroupId(int activeInputGroupId) {
        controller.setActiveInputGroupId(activeInputGroupId);
    }

    @ZenCodeType.Getter("status")
    public String getStatus() {
        return controller.getRecipeStatus().serializedName();
    }

    @ZenCodeType.Getter("statusInfo")
    public String getStatusInfo() {
        return controller.getRecipeStatusDetail();
    }

    @ZenCodeType.Method
    public void setStatus(String status) {
        controller.setRecipeStatus(MmceRecipeStatus.bySerializedName(status), controller.getRecipeStatusDetail());
    }

    @ZenCodeType.Method
    public void setStatusInfo(String info) {
        controller.setRecipeStatus(controller.getRecipeStatus(), info == null ? "" : info);
    }

    @ZenCodeType.Method
    public void setStatus(String status, String info) {
        controller.setRecipeStatus(MmceRecipeStatus.bySerializedName(status), info == null ? "" : info);
    }

    @ZenCodeType.Method
    public void startRecipe(String recipeId) {
        startRecipe(recipeId, 1);
    }

    @ZenCodeType.Method
    public void startRecipe(String recipeId, int parallelism) {
        if (recipeId != null && !recipeId.isBlank()) {
            controller.startRecipe(MmceEventIds.recipeId(recipeId), parallelism);
        }
    }

    @ZenCodeType.Method
    public int advanceRecipeProgress() {
        return controller.advanceRecipeProgress();
    }

    @ZenCodeType.Method
    public void clearActiveRecipe() {
        controller.clearActiveRecipe();
    }

    @ZenCodeType.Method
    public void addModifier(String key, MmceRecipeModifier modifier) {
        controller.addModifier(key, modifier);
    }

    @ZenCodeType.Method
    public void removeModifier(String key) {
        controller.removeModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasModifier(String key) {
        return controller.hasModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasTemporaryModifier(String key) {
        return controller.hasTemporaryModifier(key);
    }

    @ZenCodeType.Method
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        controller.addPermanentModifier(key, modifier);
    }

    @ZenCodeType.Method
    public void removePermanentModifier(String key) {
        controller.removePermanentModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String key) {
        return controller.hasPermanentModifier(key);
    }

    @ZenCodeType.Method
    public void clearTemporaryModifiers() {
        controller.clearTemporaryModifiers();
    }

    @ZenCodeType.Method
    public boolean hasMachineUpgrade(String upgradeName) {
        return controller.hasMachineUpgrade(upgradeName);
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgrade getMachineUpgrade(String upgradeName) {
        return MmceCTMachineUpgrade.of(controller.getMachineUpgrade(upgradeName));
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgrade[] getMachineUpgrades() {
        return java.util.Arrays.stream(controller.getMachineUpgrades())
                .map(MmceCTMachineUpgrade::of)
                .toArray(MmceCTMachineUpgrade[]::new);
    }

    @ZenCodeType.Method
    public String[] getDynamicPatternNames() {
        return controller.getDynamicPatternNames();
    }

    @ZenCodeType.Method
    public int getDynamicPatternSize(String patternName) {
        return controller.getDynamicPatternSize(patternName);
    }

    @ZenCodeType.Method
    public String getDynamicPatternFacing(String patternName) {
        return controller.getDynamicPatternFacing(patternName);
    }

    @ZenCodeType.Method
    public float getSmartInterfaceValue(String type) {
        return controller.getSmartInterfaceData(type).map(binding -> binding.value()).orElse(0.0F);
    }

    @ZenCodeType.Method
    public boolean hasSmartInterfaceData(String type) {
        return controller.getSmartInterfaceData(type).isPresent();
    }

    @ZenCodeType.Method
    public boolean updateSmartInterfaceValue(String type, float value) {
        return controller.updateSmartInterfaceValue(type, value);
    }

    MachineControllerBlockEntity unwrap() {
        return controller;
    }

    private static final class MmceEventIds {
        private static ResourceLocation recipeId(String value) {
            String id = value.trim();
            return id.indexOf(':') >= 0
                    ? ResourceLocation.parse(id)
                    : ResourceLocation.fromNamespaceAndPath(hellfirepvp.modularmachinery.port.ModularMachineryNeoForge.MODID, id);
        }
    }
}
