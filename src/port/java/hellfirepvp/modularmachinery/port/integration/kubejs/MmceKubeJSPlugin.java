package hellfirepvp.modularmachinery.port.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;

public final class MmceKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(MmceKubeJSBindings.class);
        filter.allow(MmceKubeJSEvents.class);
        filter.allow(MmceKubeJSMachineBuilder.class);
        filter.allow(MmceKubeJSRecipeBuilder.class);
        filter.allow(MmceKubeJSBlockArrayBuilder.class);
        filter.allow(MmceKubeJSIngredientArrayPrimer.class);
        filter.allow(MmceMachineEvent.class);
        filter.allow(MmceRecipeEvent.class);
        filter.allow(MachineControllerBlockEntity.class);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("MMCE", MmceKubeJSBindings.class);
        bindings.add("ModularMachinery", MmceKubeJSBindings.class);
        bindings.add("MMCEEvents", MmceKubeJSEvents.class);
    }
}
