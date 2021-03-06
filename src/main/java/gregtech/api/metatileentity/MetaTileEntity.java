package gregtech.api.metatileentity;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.ITurnable;
import gregtech.api.util.GTUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class MetaTileEntity implements IMetaTileEntity {

    public final IMetaTileEntityFactory factory;

    protected final ItemStack[] itemInventory;
    protected final FluidStack[] fluidInventory;
    protected EnumFacing frontFacing = EnumFacing.NORTH;

    protected int[] sidedRedstoneOutput = new int[6];

    GregtechTileEntity holder;

    public MetaTileEntity(IMetaTileEntityFactory factory) {
        this.factory = factory;
        this.itemInventory = new ItemStack[getSlotsCount()];
        this.fluidInventory = new FluidStack[getTanksCount()];
    }

    @Override
    public IMetaTileEntityFactory getFactory() {
        return factory;
    }

    @Override
    public <T> boolean hasCapability(Capability<T> capability, EnumFacing side) {
        return capability == ITurnable.CAPABILITY_TURNABLE;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability == ITurnable.CAPABILITY_TURNABLE) {
            return ITurnable.CAPABILITY_TURNABLE.cast(this);
        }
        return null;
    }

    @Override
    public IBlockState getModelState() {
        return factory.getDefaultRenderState();
    }

    @Override
    public int getMaxStackSize(int index) {
        return 64;
    }

    @Override
    public int getTankCapacity(int tankIndex) {
        return 16000;
    }

    @Override
    public String getMetaName() {
        return GregTechAPI.METATILEENTITY_REGISTRY.getNameForObject(factory);
    }

//    @Override
    public String getUnlocalizedName() {
        return "gt.machine." + getMetaName();
    }

    @Override
    public boolean canConnectRedstone(@Nullable EnumFacing side) {
        return true;
    }

    @Override
    public int getInputRedstoneSignal(EnumFacing side) {
        return getWorldObj().getStrongPower(getWorldPos().offset(side));
    }

    @Override
    public World getWorldObj() {
        return holder.getWorldObj();
    }

    @Override
    public BlockPos getWorldPos() {
        return holder.getWorldPos();
    }

    @Override
    public long getTimer() {
        return holder.getTimer();
    }

    @Override
    public void markDirty() {
        holder.markDirty();
    }

    public void markBlockForRenderUpdate() {
        BlockPos pos = getWorldPos();
        getWorldObj().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        switch (dataId) {
            case 1:
                this.frontFacing = EnumFacing.VALUES[buf.readByte()];
                markBlockForRenderUpdate();
                break;
            case 2:
                this.sidedRedstoneOutput[buf.readByte()] = buf.readInt();
                markBlockForRenderUpdate();
                break;
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buf) {
        buf.writeByte(frontFacing.getIndex());
        for(EnumFacing side : EnumFacing.VALUES) {
            buf.writeInt(sidedRedstoneOutput[side.getIndex()]);
        }
    }

    @Override
    public void receiveInitialData(PacketBuffer buf) {
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
        for(EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneOutput[side.getIndex()] = buf.readInt();
        }
    }

    @Override
    public int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        return side == null ? Arrays.stream(sidedRedstoneOutput).sorted().findFirst().orElse(0) : sidedRedstoneOutput[side.getIndex()];
    }

    @Override
    public void setOutputRedstoneSignal(EnumFacing side, int strength) {
        Preconditions.checkNotNull(side);
        this.sidedRedstoneOutput[side.getIndex()] = strength;
        if(!getWorldObj().isRemote) {
            markDirty();
            holder.writeCustomData(2, buf -> {
                buf.writeByte(side.getIndex());
                buf.writeInt(strength);
            });
        }
    }

    @Override
    public boolean isValidFacing(EnumFacing side) {
        return true;
    }

    @Override
    public GregtechTileEntity getHolder() {
        return holder;
    }

    @Override
    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    @Override
    public void setFrontFacing(EnumFacing facing) {
        this.frontFacing = facing;
        if(!getWorldObj().isRemote) {
            markDirty();
            holder.writeCustomData(1, buf -> buf.writeByte(facing.getIndex()));
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing face) {
        int[] slots = new int[getSlotsCount()];
        int lastIndex = 0;
        for(int i = 0; i < getSlotsCount(); i++) {
            if(allowPullStack(i, face, null) || allowPutStack(i, face, null)) {
                slots[lastIndex++] = i;
            }
        }
        return Arrays.copyOf(slots, lastIndex);
    }

    @Override
    public boolean allowPullStack(int index, EnumFacing side, ItemStack stack) {
        return isValidSlot(index);
    }

    @Override
    public boolean allowPutStack(int index, EnumFacing side, ItemStack stack) {
        return isValidSlot(index);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.itemInventory[index];
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        this.itemInventory[index] = stack == null || stack.stackSize <= 0 ? null : stack;
        holder.markDirty();
    }

    @Override
    public boolean isValidSlot(int index) {
        return true;
    }

    @Override
    public int[] getTanksForFace(EnumFacing face) {
        int[] slots = new int[getTanksCount()];
        int lastIndex = 0;
        for(int i = 0; i < getTanksCount(); i++) {
            if(allowPullFluid(i, face, null) || allowPutFluid(i, face, null)) {
                slots[lastIndex++] = i;
            }
        }
        return Arrays.copyOf(slots, lastIndex);
    }

    @Override
    public boolean allowPullFluid(int tankIndex, @Nullable EnumFacing side, @Nullable Fluid fluid) {
        return isValidFluidTank(tankIndex);
    }

    @Override
    public boolean allowPutFluid(int tankIndex, @Nullable EnumFacing side, @Nullable Fluid fluid) {
        //one tank for one fluid type
        for(int i = 0; i < getTanksCount(); i++) {
            if(i != tankIndex) {
                FluidStack fluidStack = getFluidInTank(i);
                if(fluidStack != null && fluidStack.getFluid() == fluid) {
                    return false;
                }
            }
        }
        return isValidFluidTank(tankIndex);
    }

    @Override
    public @Nullable FluidStack getFluidInTank(int tankIndex) {
        return this.fluidInventory[tankIndex];
    }

    public boolean addItemsToMachine(boolean simulate, List<ItemStack> items) {
        items = new ArrayList<>(items); //copy collection
        for(int i = 0; i < getSlotsCount(); i++) {
            Iterator<ItemStack> iter = items.iterator();
            while(iter.hasNext()) {
                ItemStack itemStack = iter.next();
                if(allowPutStack(i, null, itemStack)) {
                    ItemStack stackInSlot = getStackInSlot(i);
                    if(stackInSlot == null) {
                        ItemStack toPut = itemStack.splitStack(getMaxStackSize(i));
                        if(!simulate) setStackInSlot(i, toPut);
                    } else if(ItemStack.areItemsEqual(itemStack, stackInSlot) &&
                            ItemStack.areItemStackTagsEqual(itemStack, stackInSlot) &&
                            stackInSlot.stackSize < getMaxStackSize(i)) {
                        ItemStack toPut = itemStack.splitStack(getMaxStackSize(i) - stackInSlot.stackSize);
                        if(!simulate) setStackInSlot(i, toPut);
                    }
                    if(itemStack.stackSize == 0) {
                        iter.remove();
                    }
                }
            }
        }
        return items.isEmpty();
    }

    public boolean addFluidsToMachine(boolean simulate, List<FluidStack> items) {
        items = new ArrayList<>(items); //copy collection
        for(int i = 0; i < getTanksCount(); i++) {
            Iterator<FluidStack> iter = items.iterator();
            while(iter.hasNext()) {
                FluidStack fluidStack = iter.next();
                if(allowPutFluid(i, null, fluidStack.getFluid())) {
                    FluidStack stackInSlot = getFluidInTank(i);
                    if(stackInSlot == null) {
                        FluidStack toPut = fluidStack.copy();
                        toPut.amount = Math.min(fluidStack.amount, getTankCapacity(i));
                        fluidStack.amount -= toPut.amount;
                        if(!simulate) setFluidInTank(i, toPut);
                    } else if(fluidStack.isFluidEqual(stackInSlot) && stackInSlot.amount < getTankCapacity(i)) {
                        FluidStack toPut = fluidStack.copy();
                        toPut.amount = Math.min(fluidStack.amount, getTankCapacity(i) - stackInSlot.amount);
                        fluidStack.amount -= toPut.amount;
                        if(!simulate) setFluidInTank(i, toPut);
                    }
                    if(fluidStack.amount == 0) {
                        iter.remove();
                    }
                }
            }
        }
        return items.isEmpty();
    }

    @Override
    public void setFluidInTank(int index, @Nullable FluidStack fluidStack) {
        this.fluidInventory[index] = fluidStack == null || fluidStack.amount <= 0 ? null : fluidStack;
        holder.markDirty();
    }

    @Override
    public void onLeftClick(EntityPlayer player) {

    }

    @Override
    public void onFirstTick() {
    }

    @Override
    public void onPreTick(long tickTimer) {
    }

    @Override
    public void onPostTick(long tickTimer) {
    }

    @Override
    public void inValidate() {
    }

    @Override
    public void onRemoval() {
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer player) {
        return true; //default - to be overridden
    }

    @Override
    public void onEntityCollidedWithBlock(Entity collider) {
    }

    @Override
    public boolean isValidFluidTank(int tankIndex) {
        return true;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound data) {
    }

    @Override
    public void writeItemStackData(NBTTagCompound data) {
    }

    @Override
    public void saveNBTData(NBTTagCompound data) {
        data.setInteger("Facing", frontFacing.getIndex());
        NBTTagList itemInventory = new NBTTagList();
        for(int i = 0; i < getSlotsCount(); i++) {
            ItemStack stack = getStackInSlot(i);
            if(stack != null) {
                itemInventory.appendTag(stack.writeToNBT(new NBTTagCompound()));
            } else itemInventory.appendTag(new NBTTagCompound());
        }
        NBTTagList fluidInventory = new NBTTagList();
        for(int i = 0; i < getTanksCount(); i++) {
            FluidStack stack = getFluidInTank(i);
            if(stack != null) {
                fluidInventory.appendTag(stack.writeToNBT(new NBTTagCompound()));
            } else fluidInventory.appendTag(new NBTTagCompound());
        }
        if(!itemInventory.hasNoTags()) {
            data.setTag("ItemInventory", itemInventory);
        }
        if(!fluidInventory.hasNoTags()) {
            data.setTag("FluidInventory", fluidInventory);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound data) {
        if(data.hasKey("Facing", Constants.NBT.TAG_INT)) {
            this.frontFacing = EnumFacing.VALUES[data.getInteger("Facing")];
        }
        if(data.hasKey("ItemInventory", Constants.NBT.TAG_LIST)) {
            NBTTagList itemInventory = data.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
            for(int i = 0; i < itemInventory.tagCount(); i++) {
                NBTTagCompound stackTag = itemInventory.getCompoundTagAt(i);
                if(!stackTag.hasNoTags()) {
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(stackTag);
                    if(i < getSlotsCount()) setStackInSlot(i, itemStack);
                }
            }
        }
        if(data.hasKey("FluidInventory", Constants.NBT.TAG_LIST)) {
            NBTTagList fluidInventory = data.getTagList("FluidInventory", Constants.NBT.TAG_COMPOUND);
            for(int i = 0; i < fluidInventory.tagCount(); i++) {
                NBTTagCompound stackTag = fluidInventory.getCompoundTagAt(i);
                if(!stackTag.hasNoTags()) {
                    FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
                    if(i < getTanksCount()) setFluidInTank(i, fluidStack);
                }
            }
        }
    }

    @Override
    public void doExplosion(long strength) {
        BlockPos pos = holder.getWorldPos();
        World world = holder.getWorldObj();
        GTUtility.playSound(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, GregTechAPI.soundList.get(209), SoundCategory.BLOCKS, 1.0f, 1.0f);
//        if (GregTechAPI.sMachineExplosions) { // TODO CONFIG
            world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, strength, true);
//        }
    }

}