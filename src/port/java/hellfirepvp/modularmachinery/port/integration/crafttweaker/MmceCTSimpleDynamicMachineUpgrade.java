package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.SimpleDynamicMachineUpgrade")
public final class MmceCTSimpleDynamicMachineUpgrade {
    private final MmceMachineUpgrade upgrade;

    private MmceCTSimpleDynamicMachineUpgrade(MmceMachineUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    public static MmceCTSimpleDynamicMachineUpgrade of(MmceMachineUpgrade upgrade) {
        return upgrade == null ? null : new MmceCTSimpleDynamicMachineUpgrade(upgrade);
    }

    @ZenCodeType.Getter("name")
    public String getName() {
        return upgrade.getName();
    }

    @ZenCodeType.Getter("localizedName")
    public String getLocalizedName() {
        return upgrade.getLocalizedName();
    }

    @ZenCodeType.Getter("level")
    public float getLevel() {
        return upgrade.getLevel();
    }

    @ZenCodeType.Getter("maxStack")
    public int getMaxStack() {
        return upgrade.getMaxStack();
    }

    @ZenCodeType.Getter("stackSize")
    public int getStackSize() {
        return upgrade.getStackSize();
    }

    @ZenCodeType.Getter("itemData")
    public String getItemData() {
        return upgrade.getItemData();
    }

    @ZenCodeType.Setter("itemData")
    public void setItemData(IData itemData) {
        upgrade.setItemData(itemData);
    }

    @ZenCodeType.Getter("customData")
    public String getCustomData() {
        return upgrade.getCustomData();
    }

    @ZenCodeType.Setter("customData")
    public void setCustomData(IData customData) {
        upgrade.setCustomData(customData);
    }

    @ZenCodeType.Getter("parentStack")
    public IItemStack getParentStack() {
        return IItemStack.of(upgrade.getParentStack());
    }

    @ZenCodeType.Getter("descriptions")
    public String[] getDescriptions() {
        return upgrade.getDescriptions();
    }

    @ZenCodeType.Getter("busGUIDescriptions")
    public String[] getBusGUIDescriptions() {
        return upgrade.getBusGUIDescriptions();
    }

    @ZenCodeType.Method
    public void decrementItemDurability(int durability) {
        upgrade.decrementItemDurability(durability);
        upgrade.writeBack();
    }

    @ZenCodeType.Method
    public MmceMachineUpgrade unwrap() {
        return upgrade;
    }
}
