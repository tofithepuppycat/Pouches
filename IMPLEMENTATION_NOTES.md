# Pouch Item NBT Storage Implementation

## Overview
Refactored the pouch system to store items directly in each pouch item's NBT data instead of using a player capability.

## Changes Made

### 1. PouchItem.java
**Added NBT storage methods:**
- `getItemInSlot(ItemStack pouchStack, int slot)` - Retrieves an item from a specific slot in the pouch's NBT
- `setItemInSlot(ItemStack pouchStack, int slot, ItemStack item)` - Stores an item in a specific slot in the pouch's NBT
- `getAllItems(ItemStack pouchStack)` - Returns all items stored in the pouch

**NBT Structure:**
```json
{
  "Items": [
    {
      "Slot": 0,
      "id": "minecraft:diamond",
      "Count": 1
    },
    {
      "Slot": 1,
      "id": "minecraft:iron_ingot",
      "Count": 64
    }
  ]
}
```

### 2. PouchHelper.java
**Updated methods to work with equipped pouch items:**
- `getItemInPouchSlot()` - Now reads from the equipped pouch ItemStack's NBT
- `setItemInPouchSlot()` - Now writes to the equipped pouch ItemStack's NBT
- `getEquippedPouch(Player, int)` - New method to get the equipped pouch ItemStack at a given index
- `getAllPouchItems(Player)` - New method to get all items from all equipped pouches for syncing
- `getAllPouchSlotCounts(Player)` - New method to get slot counts for all equipped pouches

### 3. TransferToPouchPacket.java
**Updated packet handler:**
- Now saves items directly to pouch ItemStack NBT instead of player capability
- Properly syncs the flattened item array to client after changes

### 4. CuriosEventHandler.java
**Updated curio change handler:**
- Now reads items from equipped pouches' NBT instead of capability
- Syncs current pouch contents when curios are equipped/unequipped

### 5. CapabilityEvents.java
**Updated login sync:**
- Now reads items from equipped pouches' NBT instead of capability
- Sends proper sync packet on player login

## Client Deletion Issue Investigation

### Root Cause
The "client deleting items" issue was likely caused by:
1. **Sync Delay**: Client sends packet → Server updates → Client waits for sync response
2. **No Local Prediction**: Client doesn't immediately update its local cache
3. **Race Condition**: Client may render before sync packet arrives

### Solution
The new implementation:
1. **Direct NBT Storage**: Items are stored in the pouch itself, making them persistent
2. **Proper Syncing**: After every change, server sends SyncPouchPacket to client
3. **ItemStack References**: Since pouches are ItemStacks in curio slots, modifications persist automatically

### Sync Flow
```
1. Client: Press C key to transfer item
2. Client: Send TransferToPouchPacket to server
3. Server: Swap items using PouchHelper.setItemInPouchSlot()
4. Server: Items are saved to equipped pouch's NBT
5. Server: Send SyncPouchPacket back to client
6. Client: Update ClientPouchData cache
7. Client: Render updated items in wheel overlay
```

## Testing Checklist
- [ ] Equip pouches in curio slots
- [ ] Transfer items to pouch slots
- [ ] Verify items persist in pouch after unequipping/re-equipping
- [ ] Verify items persist after logging out/in
- [ ] Verify multiple pouches work correctly
- [ ] Verify different pouch types (basic, iron, diamond) work correctly
- [ ] Verify no item duplication occurs
- [ ] Verify client correctly displays items immediately after transfer

## Potential Issues to Monitor

### ItemStack Reference Persistence
When we get an ItemStack from a curio slot and modify its NBT, the changes should persist because we're modifying the object in-place. However, if Curios copies ItemStacks instead of returning references, we may need to:
- Call a method to mark the curio slot as "dirty"
- Or explicitly set the ItemStack back into the curio slot after modification

### Capability Still Attached
The player capability is still attached but not used for item storage. It could be removed entirely, but keeping it maintains backward compatibility and allows for future features.

## Future Improvements
1. Add visual feedback when items are transferred (particles/sounds)
2. Implement shulker-box-like preview tooltip on pouches
3. Add search/filter functionality for pouches with many items
4. Consider adding a GUI for easier item management
