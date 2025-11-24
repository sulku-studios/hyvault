# HyVault Plugin

The HyVault Plugin is the core runtime component that manages economy providers and configuration for the HyVault economy system.

##  Installation

1. Download the latest `hyvault-plugin.jar` from releases
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Configure `plugins/hyvault/config.yml` as needed

## Configuration

The plugin generates a `config.yml` file in the `plugins/hyvault/` directory:

```yaml
allow-multiple-economies: false

# If multi economy is enabled it will default to this
# This is the economy ID that will be used when calling HyVault.getEconomy()
default-economy: hyconomy
```

### For Server Administrators

1. Install HyVault Plugin
2. Install at least one economy provider plugin (e.g., HyConomy, or any plugin that implements the HyVault API)
3. Configure which economy should be the default in `config.yml`
4. Restart the server

The plugin will automatically:
- Load the configuration
- Register economy providers as they load
- Set the default economy based on your configuration
- Manage economy provider lifecycle

### For Economy Provider Developers

Your plugin should depend on `hyvault-api` and register/unregister economies:

```kotlin
class YourEconomyPlugin : Plugin() {
    private lateinit var economy: YourEconomy
    
    override fun onEnable() {
        // Initialize your economy implementation
        economy = YourEconomy()
        // Register with HyVault
        HyVault.registerEconomy(economy)
        logger.info("Registered economy: ${economy.id}")
    }
    
    override fun onDisable() {
        // Unregister from HyVault
        HyVault.unregisterEconomy(economy.id)
        logger.info("Unregistered economy: ${economy.id}")
    }
}
```

### Multiple Economies

When `allow-multiple-economies: true`:
- All economy providers can coexist
- Each has a unique ID
- The `default-economy` config determines which is returned by `HyVault.getEconomy()`
- Plugins can access specific economies by name: `HyVault.getEconomy("youreconomyid")`

### Single Economy Mode

When `allow-multiple-economies: false`:
- Only one economy can be active
- Registering a new economy clears the previous one
- The most recently registered economy becomes the default
- Simpler for servers that only need one currency

##  Troubleshooting

### "No economy provider found"
- Ensure at least one economy plugin is installed and enabled
- Check that the economy plugin properly calls `HyVault.registerEconomy()`

### "Default economy not found"
- Check that `default-economy` in config.yml matches an economy provider's ID
- Economy IDs are case-sensitive?

### "Multiple economies disabled" warning
- This is normal when `allow-multiple-economies: false`
- Only the last registered economy will be active

##  License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
