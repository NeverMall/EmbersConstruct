package com.peatral.embersconstruct.common.tileentity;

import com.peatral.embersconstruct.common.inventory.ContainerKiln;
import com.peatral.embersconstruct.common.block.BlockKiln;
import com.peatral.embersconstruct.common.registry.KilnRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityKiln extends TileEntityLockable implements ITickable, ISidedInventory {
    private static final int[] SLOTS_TOP = new int[] {0};
    private static final int[] SLOTS_BOTTOM = new int[] {2, 1};
    private static final int[] SLOTS_SIDES = new int[] {1};
    /** The ItemStacks that hold the items currently being used in the kiln */
    private NonNullList<ItemStack> kilnItemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
    /** The number of ticks that the kiln will keep burning */
    private int kilnBurnTime;
    /** The number of ticks that a fresh copy of the currently-burning item would keep the kiln burning for */
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime;
    private String kilnCustomName;


    public int getSizeInventory() {
        return this.kilnItemStacks.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.kilnItemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack getStackInSlot(int index) {
        return this.kilnItemStacks.get(index);
    }


    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.kilnItemStacks, index, count);
    }

    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.kilnItemStacks, index);
    }


    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemstack = this.kilnItemStacks.get(index);
        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
        this.kilnItemStacks.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        if (index == 0 && !flag) {
            this.totalCookTime = this.getCookTime(stack);
            this.cookTime = 0;
            this.markDirty();
        }
    }

    public String getName() {
        return this.hasCustomName() ? this.kilnCustomName : "container.embersconstruct.kiln";
    }

    public boolean hasCustomName() {
        return this.kilnCustomName != null && !this.kilnCustomName.isEmpty();
    }

    public void setCustomInventoryName(String customName) {
        this.kilnCustomName = customName;
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.kilnItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.kilnItemStacks);
        this.kilnBurnTime = compound.getInteger("BurnTime");
        this.cookTime = compound.getInteger("CookTime");
        this.totalCookTime = compound.getInteger("CookTimeTotal");
        this.currentItemBurnTime = compound.getInteger("BurnTimeTotal");

        if (compound.hasKey("CustomName", 8)) {
            this.kilnCustomName = compound.getString("CustomName");
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("BurnTime", (short)this.kilnBurnTime);
        compound.setInteger("CookTime", (short)this.cookTime);
        compound.setInteger("CookTimeTotal", (short)this.totalCookTime);
        compound.setInteger("BurnTimeTotal", (short)this.currentItemBurnTime);
        ItemStackHelper.saveAllItems(compound, this.kilnItemStacks);

        if (this.hasCustomName()) {
            compound.setString("CustomName", this.kilnCustomName);
        }

        return compound;
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isBurning() {
        return this.kilnBurnTime > 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isBurning(IInventory inventory) {
        return inventory.getField(0) > 0;
    }

    public void update() {
        boolean flag = this.isBurning();
        boolean flag1 = false;


        if (this.isBurning()) {
            this.kilnBurnTime--;
            BlockKiln.setState(true, this.world, this.pos);
        }

        if (!this.world.isRemote) {
            ItemStack itemstack = this.kilnItemStacks.get(1);

            if (this.isBurning() || !itemstack.isEmpty() && !this.kilnItemStacks.get(0).isEmpty()) {
                if (!this.isBurning() && this.canSmelt()) {
                    this.kilnBurnTime = getItemBurnTime(itemstack);
                    this.currentItemBurnTime = this.kilnBurnTime;

                    if (this.isBurning()) {
                        flag1 = true;

                        if (!itemstack.isEmpty()) {
                            Item item = itemstack.getItem();
                            itemstack.shrink(1);

                            if (itemstack.isEmpty()) {
                                ItemStack item1 = item.getContainerItem(itemstack);
                                this.kilnItemStacks.set(1, item1);
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canSmelt()) {
                    ++this.cookTime;

                    if (this.cookTime == this.totalCookTime) {
                        this.cookTime = 0;
                        this.totalCookTime = this.getCookTime(this.kilnItemStacks.get(0));
                        this.smeltItem();
                        flag1 = true;
                    }
                } else {
                    this.cookTime = 0;
                }
            } else if (!this.isBurning() && this.cookTime > 0) {
                this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
            }

            if (flag != this.isBurning()) {
                flag1 = true;
                BlockKiln.setState(this.isBurning(), this.world, this.pos);
            }
        }

        if (flag1) {
            this.markDirty();
        }

    }

    public int getCookTime(ItemStack stack) {
        return 200;
    }

    private boolean canSmelt() {
        if (this.kilnItemStacks.get(0).isEmpty()) {
            return false;
        }
        else {
            ItemStack itemstack = KilnRecipes.instance().getResult(this.kilnItemStacks.get(0));

            if (itemstack.isEmpty()) {
                return false;
            }
            else {
                ItemStack itemstack1 = this.kilnItemStacks.get(2);

                if (itemstack1.isEmpty()) {
                    return true;
                } else if (!itemstack1.isItemEqual(itemstack)) {
                    return false;
                } else if (itemstack1.getCount() + itemstack.getCount() <= this.getInventoryStackLimit() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                    return true;
                } else {
                    return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize(); // Forge fix: make kiln respect stack sizes in kiln recipes
                }
            }
        }
    }

    public void smeltItem() {
        if (this.canSmelt()) {
            ItemStack itemstack = this.kilnItemStacks.get(0);
            ItemStack itemstack1 = KilnRecipes.instance().getResult(itemstack);
            ItemStack itemstack2 = this.kilnItemStacks.get(2);

            if (itemstack2.isEmpty()) {
                this.kilnItemStacks.set(2, itemstack1.copy());
            } else if (itemstack2.getItem() == itemstack1.getItem()) {
                itemstack2.grow(itemstack1.getCount());
            }

            if (itemstack.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && itemstack.getMetadata() == 1 && !((ItemStack)this.kilnItemStacks.get(1)).isEmpty() && ((ItemStack)this.kilnItemStacks.get(1)).getItem() == Items.BUCKET) {
                this.kilnItemStacks.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemstack.shrink(1);
        }
    }

    public static int getItemBurnTime(ItemStack stack) {
        return TileEntityFurnace.getItemBurnTime(stack);
    }

    public static boolean isItemFuel(ItemStack stack) {
        return getItemBurnTime(stack) > 0;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 2) {
            return false;
        } else if (index != 1) {
            return true;
        } else {
            ItemStack itemstack = this.kilnItemStacks.get(1);
            return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && itemstack.getItem() != Items.BUCKET;
        }
    }

    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return SLOTS_BOTTOM;
        } else {
            return side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES;
        }
    }

    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (direction == EnumFacing.DOWN && index == 1) {
            Item item = stack.getItem();

            if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
                return false;
            }
        }

        return true;
    }

    public String getGuiID() {
        return "minecraft:kiln";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerKiln(playerInventory, this);
    }

    public int getField(int id) {
        switch (id) {
            case 0:
                return this.kilnBurnTime;
            case 1:
                return this.currentItemBurnTime;
            case 2:
                return this.cookTime;
            case 3:
                return this.totalCookTime;
            default:
                return 0;
        }
    }

    public void setField(int id, int value) {
        switch (id) {
            case 0:
                this.kilnBurnTime = value;
                break;
            case 1:
                this.currentItemBurnTime = value;
                break;
            case 2:
                this.cookTime = value;
                break;
            case 3:
                this.totalCookTime = value;
        }
    }

    public int getFieldCount() {
        return 4;
    }

    public void clear() {
        this.kilnItemStacks.clear();
    }

    net.minecraftforge.items.IItemHandler handlerTop = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.UP);
    net.minecraftforge.items.IItemHandler handlerBottom = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.DOWN);
    net.minecraftforge.items.IItemHandler handlerSide = new net.minecraftforge.items.wrapper.SidedInvWrapper(this, net.minecraft.util.EnumFacing.WEST);

    @SuppressWarnings("unchecked")
    @Override
    @javax.annotation.Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
        if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            if (facing == EnumFacing.DOWN)
                return (T) handlerBottom;
            else if (facing == EnumFacing.UP)
                return (T) handlerTop;
            else
                return (T) handlerSide;
        return super.getCapability(capability, facing);
    }
}