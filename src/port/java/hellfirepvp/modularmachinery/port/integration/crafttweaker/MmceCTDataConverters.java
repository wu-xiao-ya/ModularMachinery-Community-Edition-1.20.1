package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.google.gson.JsonParser;
import hellfirepvp.modularmachinery.port.data.MmceNbtCompat;
import net.minecraft.nbt.CompoundTag;

final class MmceCTDataConverters {
    static CompoundTag toCompound(IData data) {
        if (data == null) {
            return null;
        }
        return MmceNbtCompat.toTag(JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject());
    }

    private MmceCTDataConverters() {
    }
}
