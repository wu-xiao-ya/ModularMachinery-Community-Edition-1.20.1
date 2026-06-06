package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.SmartInterfaceBlock;
import hellfirepvp.modularmachinery.port.block.property.SmartInterfaceMode;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceSmartInterfaceUpdateEvent;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class SmartInterfaceBlockEntity extends BaseMachineBlockEntity {
    private final List<Binding> bindings = new ArrayList<>();

    public SmartInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(MmceBlockEntities.SMART_INTERFACE.get(), pos, state);
    }

    public SmartInterfaceMode getMode() {
        return getBlockState().hasProperty(SmartInterfaceBlock.TYPE)
                ? getBlockState().getValue(SmartInterfaceBlock.TYPE)
                : SmartInterfaceMode.NUMBER;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public Optional<Binding> getBinding(BlockPos controllerPos) {
        for (Binding binding : bindings) {
            if (binding.controllerPos().equals(controllerPos)) {
                return Optional.of(binding);
            }
        }
        return Optional.empty();
    }

    public Optional<Binding> getBinding(String type) {
        String normalizedType = type == null ? "" : type;
        for (Binding binding : bindings) {
            if (binding.type().equals(normalizedType)) {
                return Optional.of(binding);
            }
        }
        return Optional.empty();
    }

    public Optional<Float> value(String type) {
        return getBinding(type).map(Binding::value);
    }

    public void bind(BlockPos controllerPos, ResourceLocation machineId, String type, float defaultValue, boolean overrideValue) {
        String normalizedType = type == null ? "" : type;
        for (int index = 0; index < bindings.size(); index++) {
            Binding binding = bindings.get(index);
            if (!binding.controllerPos().equals(controllerPos)) {
                continue;
            }
            float value = !overrideValue && binding.type().equals(normalizedType) ? binding.value() : defaultValue;
            Binding updated = new Binding(controllerPos, machineId, normalizedType, value);
            if (!updated.equals(binding)) {
                bindings.set(index, updated);
                markForSync();
            }
            return;
        }
        bindings.add(new Binding(controllerPos, machineId, normalizedType, defaultValue));
        markForSync();
    }

    public boolean updateValue(BlockPos controllerPos, float value) {
        for (int index = 0; index < bindings.size(); index++) {
            Binding binding = bindings.get(index);
            if (binding.controllerPos().equals(controllerPos)) {
                updateBinding(index, binding, value);
                return true;
            }
        }
        return false;
    }

    public boolean updateValue(String type, float value) {
        String normalizedType = type == null ? "" : type;
        for (int index = 0; index < bindings.size(); index++) {
            Binding binding = bindings.get(index);
            if (binding.type().equals(normalizedType)) {
                updateBinding(index, binding, value);
                return true;
            }
        }
        return false;
    }

    private void updateBinding(int index, Binding binding, float value) {
        if (Float.compare(binding.value(), value) == 0) {
            return;
        }
        Binding updated = new Binding(binding.controllerPos(), binding.machineId(), binding.type(), value);
        bindings.set(index, updated);
        markForSync();
        postUpdateEvent(binding, value);
    }

    private void postUpdateEvent(Binding binding, float newValue) {
        if (level == null || level.isClientSide() || binding.machineId() == null) {
            return;
        }
        if (level.getBlockEntity(binding.controllerPos()) instanceof MachineControllerBlockEntity controller) {
            MmceEventRegistry.postMachine(new MmceSmartInterfaceUpdateEvent(
                    controller,
                    binding.machineId(),
                    this,
                    binding.type(),
                    binding.value(),
                    newValue
            ));
        }
    }

    public void removeBinding(BlockPos controllerPos) {
        if (bindings.removeIf(binding -> binding.controllerPos().equals(controllerPos))) {
            markForSync();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bindings.clear();
        ListTag list = tag.getList("boundData", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            bindings.add(Binding.load(list.getCompound(i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Binding binding : bindings) {
            list.add(binding.save());
        }
        tag.put("boundData", list);
    }

    public record Binding(BlockPos controllerPos, ResourceLocation machineId, String type, float value) {
        public Binding {
            type = type == null ? "" : type;
        }

        private static Binding load(CompoundTag tag) {
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            ResourceLocation machine = ResourceLocation.tryParse(tag.getString("machineId"));
            return new Binding(pos, machine, tag.getString("type"), tag.getFloat("value"));
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", controllerPos.getX());
            tag.putInt("y", controllerPos.getY());
            tag.putInt("z", controllerPos.getZ());
            if (machineId != null) {
                tag.putString("machineId", machineId.toString());
            }
            tag.putString("type", type);
            tag.putFloat("value", value);
            return tag;
        }
    }
}
