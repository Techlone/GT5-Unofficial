package gregtech.api.metatileentity.factory;

import com.google.common.base.Throwables;
import gregtech.api.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.IMetaTileEntityFactory;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.common.blocks.BlockMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class TieredMetaTileEntityFactory<T extends TieredMetaTileEntity> extends MetaTileEntityFactory<T> {

    protected int tier;

    public TieredMetaTileEntityFactory(BlockMachine.ToolClass toolClass, int harvestLevel, String[] description, Class<T> metaTileEntityClass, ResourceLocation modelLocation, IBlockState defaultState, int tier) {
        super(toolClass, harvestLevel, description, metaTileEntityClass, modelLocation, defaultState);
        this.tier = tier;
    }

    @Override
    public IMetaTileEntity constructMetaTileEntity() {
        try {
            return metaTileEntityClass.getConstructor(IMetaTileEntityFactory.class, int.class).newInstance(this, tier);
        } catch (Throwable exception) {
            throw Throwables.propagate(exception);
        }
    }

}
