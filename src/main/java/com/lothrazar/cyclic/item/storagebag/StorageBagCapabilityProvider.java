package com.lothrazar.cyclic.item.storagebag;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageBagCapabilityProvider implements ICapabilitySerializable<CompoundNBT> {

  private ItemStack bag;
  private int slots;

  private final LazyOptional<ItemStackHandler> inventory = LazyOptional.of(() -> new ItemStackHandler(slots));

  public StorageBagCapabilityProvider(ItemStack stack, int slots) {
    this.bag = stack;
    this.slots = slots;
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
      return inventory.cast();
    return LazyOptional.empty();
  }

  @Override
  public CompoundNBT serializeNBT() {
    if (inventory.isPresent()) {
      return inventory.resolve().get().serializeNBT();
    }
    return new CompoundNBT();
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    inventory.ifPresent(h -> h.deserializeNBT(nbt));
  }
}
