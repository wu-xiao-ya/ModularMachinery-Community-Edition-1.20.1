package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class MmceControllerGUIRenderEvent extends MmceMachineEvent {
    private final List<String> extraInfo = new ArrayList<>();

    public MmceControllerGUIRenderEvent(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        super(controller, machineId, MmceMachineEventType.CONTROLLER_GUI_RENDER, MmceEventPhase.END);
    }
    public String[] getExtraInfo() {
        return extraInfo.toArray(String[]::new);
    }

    public List<String> extraInfo() {
        return List.copyOf(extraInfo);
    }
    public void setExtraInfo(String... info) {
        addExtraInfo(info);
    }
    public void addExtraInfo(String line) {
        if (line != null) {
            extraInfo.add(line);
        }
    }
    public void addExtraInfo(String... info) {
        if (info != null) {
            Arrays.stream(info)
                    .filter(line -> line != null)
                    .forEach(extraInfo::add);
        }
    }
    public void clearExtraInfo() {
        extraInfo.clear();
    }
}
