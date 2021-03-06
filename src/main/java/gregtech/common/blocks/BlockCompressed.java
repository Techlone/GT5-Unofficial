package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.type.*;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public final class BlockCompressed extends DelayedStateBlock {

    public final PropertyMaterial variantProperty;

    public BlockCompressed(Collection<? extends Material> materials) {
        super(net.minecraft.block.material.Material.IRON);
        setHardness(5.0f);
        setResistance(10.0f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    public void registerBlock(String blockName) {
        setUnlocalizedName("unnamed");
        setRegistryName(blockName);
        GameRegistry.register(this);
        CompressedItemBlock itemBlock = new CompressedItemBlock(this);
        itemBlock.setRegistryName(blockName);
        GameRegistry.register(itemBlock);
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if(material instanceof SolidMaterial) {
            return "pickaxe";
        } else if(material instanceof DustMaterial) {
            return "shovel";
        }
        return "pickaxe";
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if(material instanceof SolidMaterial) {
            return ((SolidMaterial) material).harvestLevel;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        Material material = variantProperty.getAllowedValues().get(meta);
        return getDefaultState().withProperty(variantProperty, material);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        Material material = state.getValue(variantProperty);
        return variantProperty.getAllowedValues().indexOf(material);
    }

    @Override
    protected BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, variantProperty);
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItem(IBlockState blockState) {
        return new ItemStack(this, 1, getMetaFromState(blockState));
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for(IBlockState blockState : blockState.getValidStates()) {
            list.add(getItem(blockState));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        Material material = state.getValue(variantProperty);
        if(material instanceof GemMaterial) {
            return net.minecraft.block.material.Material.ROCK;
        } else if(material instanceof MetalMaterial) {
            return net.minecraft.block.material.Material.IRON;
        } else if(material instanceof DustMaterial) {
            return net.minecraft.block.material.Material.SAND;
        }
        return net.minecraft.block.material.Material.ROCK;
    }

    @Override
    @SuppressWarnings("deprecation")
    public MapColor getMapColor(IBlockState state) {
        return getMaterial(state).getMaterialMapColor();
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        Material material =state.getValue(variantProperty);
        if(material instanceof GemMaterial) {
            return SoundType.STONE;
        } else if(material instanceof MetalMaterial) {
            return SoundType.METAL;
        } else if(material instanceof DustMaterial) {
            return SoundType.SAND;
        }
        return SoundType.STONE;
    }

}
