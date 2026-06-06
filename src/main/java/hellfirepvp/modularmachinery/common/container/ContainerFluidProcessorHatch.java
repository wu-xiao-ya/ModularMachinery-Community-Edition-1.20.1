package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.common.tiles.TileFluidProcessorHatch;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerFluidProcessorHatch extends ContainerBase<TileFluidProcessorHatch> {
    public ContainerFluidProcessorHatch(TileFluidProcessorHatch owner, EntityPlayer opening) {
        super(owner, opening);
    }
}
