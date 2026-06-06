package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.ControllerGUIRenderEvent")
public final class MmceControllerGUIRenderEvent extends MmceMachineEvent {
    private final List<String> extraInfo = new ArrayList<>();

    public MmceControllerGUIRenderEvent(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        super(controller, machineId, MmceMachineEventType.CONTROLLER_GUI_RENDER, MmceEventPhase.END);
    }

    @ZenCodeType.Getter("extraInfo")
    public String[] getExtraInfo() {
        return extraInfo.toArray(String[]::new);
    }

    public List<String> extraInfo() {
        return List.copyOf(extraInfo);
    }

    @ZenCodeType.Setter("extraInfo")
    public void setExtraInfo(String... info) {
        addExtraInfo(info);
    }

    @ZenCodeType.Method
    public void addExtraInfo(String line) {
        if (line != null) {
            extraInfo.add(line);
        }
    }

    @ZenCodeType.Method
    public void addExtraInfo(String... info) {
        if (info != null) {
            Arrays.stream(info)
                    .filter(line -> line != null)
                    .forEach(extraInfo::add);
        }
    }

    @ZenCodeType.Method
    public void clearExtraInfo() {
        extraInfo.clear();
    }
}
