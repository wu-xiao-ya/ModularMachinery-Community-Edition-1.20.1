package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.ControllerButtonClickEvent")
public final class MmceControllerButtonClickEvent extends MmceMachineEvent {
    private final int buttonId;

    public MmceControllerButtonClickEvent(MachineControllerBlockEntity controller, ResourceLocation machineId, int buttonId) {
        super(controller, machineId, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, MmceEventPhase.END);
        this.buttonId = buttonId;
    }

    @ZenCodeType.Getter("buttonId")
    public int getButtonId() {
        return buttonId;
    }
}
