package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineUpgrade")
public final class MmceCTMachineUpgrade {
    private final MmceMachineUpgrade upgrade;

    private MmceCTMachineUpgrade(MmceMachineUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    public static MmceCTMachineUpgrade of(MmceMachineUpgrade upgrade) {
        return upgrade == null ? null : new MmceCTMachineUpgrade(upgrade);
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

    @ZenCodeType.Getter("stack")
    public ItemStack getStack() {
        return upgrade.getStack();
    }

    @ZenCodeType.Getter("itemData")
    public String getItemData() {
        return upgrade.getItemData();
    }

    @ZenCodeType.Setter("itemData")
    public void setItemData(IData itemData) {
        upgrade.setItemData(MmceCTDataConverters.toCompound(itemData));
    }

    @ZenCodeType.Getter("customData")
    public String getCustomData() {
        return upgrade.getCustomData();
    }

    @ZenCodeType.Setter("customData")
    public void setCustomData(IData customData) {
        upgrade.setCustomData(MmceCTDataConverters.toCompound(customData));
    }

    @ZenCodeType.Getter("parentStack")
    public ItemStack getParentStack() {
        return upgrade.getParentStack();
    }

    @ZenCodeType.Getter("descriptions")
    public String[] getDescriptions() {
        return upgrade.getDescriptions();
    }

    @ZenCodeType.Getter("busGUIDescriptions")
    public String[] getBusGUIDescriptions() {
        return upgrade.getBusGUIDescriptions();
    }

    @ZenCodeType.Getter("busGuiDescriptions")
    public String[] getBusGuiDescriptions() {
        return upgrade.getBusGuiDescriptions();
    }

    @ZenCodeType.Method
    public void decrementItemDurability(int durability) {
        upgrade.decrementItemDurability(durability);
        upgrade.writeBack();
    }

    @ZenCodeType.Method
    public MmceCTSimpleMachineUpgrade asSimpleMachineUpgrade() {
        return MmceCTSimpleMachineUpgrade.of(upgrade);
    }

    @ZenCodeType.Method
    public MmceCTSimpleDynamicMachineUpgrade asSimpleDynamicMachineUpgrade() {
        return MmceCTSimpleDynamicMachineUpgrade.of(upgrade);
    }

    public MmceMachineUpgrade unwrap() {
        return upgrade;
    }
}
