# NPC Currency Exchange & Diamond Bank Features

## Overview
This plugin features a unified NPC-based currency exchange system and diamond bank management.

## Features Added

### 1. **Unified Exchange NPC** (NEW!)
- **ONE NPC handles both directions automatically:**
  - Hold **diamonds** in hand → Converts to coins
  - Hold **coins** in hand → Converts to diamonds
- **Right-click** = Convert 1 item
- **Shift + Right-click** = Convert entire stack at once!
- Diamonds from exchanges go into the central bank
- Coins can only be converted back if bank has diamonds available
- All transactions are logged

### 2. Diamond Bank NPC
- Bank managers can access the diamond bank through an NPC
- Shows total diamonds stored in the bank
- Only accessible to players with `smpcurrency.bank.manage` permission

### 3. Transaction Logging
- All diamond-to-coin exchanges are logged in `plugins/smpCurrency/transactions.log`
- All coin-to-diamond sales are logged
- Bank withdrawals are logged
- Logs include timestamp, player name, UUID, and transaction details

### 4. Coin Protection
- Coins cannot be stored in ender chests
- Coins cannot be stored in shulker boxes
- Includes protection against drag-clicking
- Players are notified when they attempt to store coins in restricted inventories

## Requirements

- **Citizens plugin** must be installed for NPC features to work
- The plugin will still function without Citizens, but NPC features will be disabled

## Setup Instructions

### Step 1: Install Citizens
1. Download Citizens from: https://www.spigotmc.org/resources/citizens.13811/
2. Place Citizens.jar in your server's plugins folder
3. Restart your server

### Step 2: Create NPCs
Use Citizens commands to create NPCs:
```
/npc create ExchangeNPC
/npc create BankNPC
```

### Step 3: Register NPCs
Use the plugin commands to register the NPCs:

**For Exchange NPC (handles both diamonds ↔ coins):**
1. Look at the exchange NPC
2. Run: `/currency npc setexchange`

**For Bank NPC:**
1. Look at the bank NPC
2. Run: `/currency npc setbank`

## Commands

### NPC Management (`/currency npc`)
**Note:** This plugin uses `/currency npc` to avoid conflicts with Citizens' `/npc` command.

- `/currency npc setexchange` - Register NPC as exchange NPC (handles both directions)
- `/currency npc setbank` - Register NPC as bank NPC
- `/currency npc remove` - Unregister the NPC you're looking at
- `/currency npc list` - List all registered NPCs

**Permission:** `smpcurrency.npc.manage` (default: op)

### Bank Management (`/bank`)
- `/bank balance` - Check total diamonds in the bank
- `/bank withdraw <amount>` - Withdraw diamonds from the bank

**Permission:** `smpcurrency.bank.manage` (default: op)

## Permissions

```yaml
smpcurrency.npc.manage - Manage NPCs (default: op)
smpcurrency.bank.manage - Access and manage diamond bank (default: op)
```

## How It Works

### Exchange NPC (Unified - Both Directions)

**Diamond → Coin:**
1. Hold diamonds in your **main hand**
2. Right-click the exchange NPC:
   - **Normal click** = Convert 1 diamond → 1 coin
   - **Shift + click** = Convert entire stack → full stack of coins
3. Diamonds are added to the bank vault
4. Transaction is logged

**Coin → Diamond:**
1. Hold coins in your **main hand**
2. Right-click the exchange NPC:
   - **Normal click** = Convert 1 coin → 1 diamond
   - **Shift + click** = Convert entire stack → full stack of diamonds
3. Diamonds are deducted from the bank vault
4. **Only works if bank has enough diamonds!**
5. Transaction is logged

### Bank Access
1. Bank manager right-clicks the bank NPC
2. Plugin checks for `smpcurrency.bank.manage` permission
3. If authorized, displays:
   - Total diamonds in bank
   - Instructions for withdrawing diamonds
4. Manager can use `/bank withdraw <amount>` to retrieve diamonds

### Coin Protection
- When a player tries to place coins in an ender chest or shulker box:
  - Action is cancelled
  - Player receives an error message
- This includes direct placement, shift-clicking, and drag-clicking

## Files Created

- `plugins/smpCurrency/transactions.log` - Transaction log file
- `plugins/smpCurrency/diamond_bank.yml` - Diamond bank storage

## Configuration

NPC registrations are automatically saved to `config.yml`:
```yaml
npcs:
  exchange:
    123: true  # NPC ID 123 is an exchange NPC (handles both directions)
  bank:
    124: true  # NPC ID 124 is a bank NPC
```

## Economy Balance

The system maintains perfect economic balance:
- **Players exchange diamonds** → Bank accumulates diamonds
- **Players convert coins back** → Bank releases diamonds
- **If bank runs out of diamonds** → No more coin-to-diamond conversions until more diamonds are exchanged!

This prevents infinite diamond duplication and creates a balanced economy.

## Important Notes

1. **Command Conflict Resolution:** This plugin uses `/currency npc` instead of `/npc` to avoid conflicts with Citizens. Citizens' native `/npc` command remains fully functional.

2. **Shift-Click Feature:** Players can shift-click NPCs while holding full stacks to exchange all items at once (up to 64 diamonds/coins per click).

3. **Bank Dependency:** Coin-to-diamond conversion is LIMITED by the bank's diamond reserves. This creates scarcity and value!

## Troubleshooting

**Citizens not detected:**
- Ensure Citizens is installed and enabled
- Check console for "Citizens plugin not found" message
- Restart server after installing Citizens

**Exchange NPC not working:**
- Make sure you're **holding diamonds or coins in your main hand**
- Check if NPC is registered: `/currency npc list`
- Verify you're right-clicking (not left-clicking)
- Check server console for errors

**Cannot convert coins to diamonds:**
- Check bank balance: `/bank balance`
- The bank might be empty! More players need to exchange diamonds first
- Bank managers can deposit diamonds using `/bank withdraw` (in reverse, but check code)

**Coins still going into ender chest:**
- Verify CoinProtectionListener is registered (check console on startup)
- Test with both drag-clicking and shift-clicking
- Report issue with debug information

**Shift-click not working:**
- Make sure you're holding shift while right-clicking the NPC
- Verify you have a full stack or multiple items in hand
- Check server logs for any errors
