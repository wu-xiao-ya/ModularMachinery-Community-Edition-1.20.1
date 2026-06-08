package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.SimpleMachineUpgrade")
public final class MmceCTSimpleMachineUpgrade {
    private final MmceMachineUpgrade upgrade;

    private MmceCTSimpleMachineUpgrade(MmceMachineUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    public static MmceCTSimpleMachineUpgrade of(MmceMachineUpgrade upgrade) {
        return upgrade == null ? null : new MmceCTSimpleMachineUpgrade(upgrade);
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

    @ZenCodeType.Getter("customData")
    public String getCustomData() {
        return upgrade.getCustomData();
    }

    @ZenCodeType.Setter("customData")
    public void setCustomData(IData customData) {
        upgrade.setCustomData(MmceCTDataConverters.toCompound(customData));
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
    public MmceMachineUpgrade unwrap() {
        return upgrade;
    }
}
