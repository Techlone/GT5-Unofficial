package gregtech.api.metatileentity;

import com.google.common.collect.ImmutableBiMap;
import gregtech.api.capability.ITurnable;
import gregtech.api.capability.internal.*;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public interface IMetaTileEntity extends ITurnable, IRedstoneReceiver, IRedstoneEmitter, IUIHolder, ISimpleFluidInventory, ISimpleSlotInventory {

    IMetaTileEntityFactory getFactory();

    String getMetaName();

    IGregTechTileEntity getHolder();

    @SideOnly(Side.CLIENT)
    IBlockState getModelState();

    /**
     * when placing a Machine in World, to initialize default Modes. data can be null!
     */
    void initFromItemStackData(NBTTagCompound data);

    /**
     * Adds the NBT-Information to the ItemStack, when being dismanteled properly
     * Used to store Machine specific Upgrade Data.
     */
    void writeItemStackData(NBTTagCompound data);

    /**
     * writeToNBT
     */
    void saveNBTData(NBTTagCompound data);

    /**
     * readFromNBT
     */
    void loadNBTData(NBTTagCompound data);

    void receiveCustomData(int dataId, PacketBuffer buf);

    void writeInitialData(PacketBuffer buf);
    void receiveInitialData(PacketBuffer buf);

    <T> boolean hasCapability(Capability<T> capability, EnumFacing side);
    <T> T getCapability(Capability<T> capability, EnumFacing side);

    /**
     * Called when a player right clicks the facing with a screwdriver.
     */
    boolean onScrewdriverRightClick(EnumFacing side, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, float clickX, float clickY, float clickZ);

    /**
     * Called when a player right clicks the facing with a wrench.
     */
    boolean onWrenchRightClick(EnumFacing side, EnumFacing wrenchingSide, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, float clickX, float clickY, float clickZ);

    /**
     * The First processed Tick which was passed to this MetaTileEntity
     */
    void onFirstTick();

    /**
     * The Tick before all the generic handling happens, what gives a slightly faster reaction speed.
     * Don't use this if you really don't need to. @onPostTick is better suited for ticks.
     * This happens still after the Cover handling.
     */
    void onPreTick(long tickTimer);

    /**
     * The Tick after all the generic handling happened.
     * Recommended to use this like updateEntity.
     */
    void onPostTick(long tickTimer);

    /**
     * Called when this MetaTileEntity gets (intentionally) disconnected from the BaseMetaTileEntity.
     * Doesn't get called when this thing is moved by Frames or similar hacks.
     */
    void inValidate();

    /**
     * Called when the BaseMetaTileEntity gets invalidated, what happens right before the @inValidate above gets called
     */
    void onRemoval();

    int getSlotsCount();

    /**
     * Creates instance of ModularUI to be opened for specified player
     * Note that set of widgets in GUI returned in this method should be SAME ON BOTH SIDES
     * @param player player who opens GUI
     * @return instance of ModularUI for this MetaTileEntity
     */
    ModularUI<? extends IMetaTileEntity> createUI(EntityPlayer player);

    int[] getSlotsForFace(EnumFacing face);
    //side == null - internal inventory change
    boolean allowPullStack(int index, EnumFacing side, ItemStack stack);
    boolean allowPutStack(int index, EnumFacing side, ItemStack stack);

    /**
     * @return a COPY of stack in slot. Actual stack won't change.
     */
    ItemStack getStackInSlot(int index);
    int getMaxStackSize(int index);

    void setStackInSlot(int index, ItemStack stack);
    boolean isValidSlot(int index);

    int getTanksCount();
    int getTankCapacity(int tankIndex);

    int[] getTanksForFace(EnumFacing face);
    //side == null - internal tank change
    boolean allowPullFluid(int tankIndex, EnumFacing side, Fluid fluid);
    boolean allowPutFluid(int tankIndex, EnumFacing side, Fluid fluid);

    /**
     * @return a COPY of stack in slot. Actual stack won't change.
     */
    FluidStack getFluidInTank(int tankIndex);
    void setFluidInTank(int index, FluidStack fluidStack);
    boolean isValidFluidTank(int tankIndex);

    /**
     * @return true if the Machine can be accessed
     */
    boolean isAccessAllowed(EntityPlayer player);

    /**
     * Called when a player rightclicks the machine
     * Sneaky rightclicks are not getting passed to this!
     */
    boolean onRightClick(EnumFacing side, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, float clickX, float clickY, float clickZ);

    /**
     * Called when a player leftclicks the machine
     * Sneaky leftclicks are getting passed to this unlike with the rightclicks.
     */
    void onLeftClick(EntityPlayer player);

    /**
     * Called when the Machine explodes, override Explosion Code here.
     */
    void doExplosion(long explosionPower);

    void onEntityCollidedWithBlock(Entity collider);

}