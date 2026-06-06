package github.kasuminova.mmce.common.event.client;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenRegister
@ZenClass("mods.modularmachinery.ControllerButtonClickEvent")
public class ControllerButtonClickEvent extends MachineEvent {
    private final String buttonId;

    public ControllerButtonClickEvent(TileMultiblockMachineController controller, String buttonId) {
        super(controller);
        this.buttonId = buttonId == null ? "" : buttonId;
    }

    @ZenGetter("buttonId")
    public String getButtonId() {
        return buttonId;
    }
}
