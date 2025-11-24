# HyVault

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-orange.svg)](build.gradle.kts)

**HyVault** is a modern economy API for Hytale servers, It provides a clean, extensible interface for economy plugins with support for async operations and Kotlin coroutines.
It uses BigRecimal for accurate economy.

##  Modules

This project consists of two modules:

### [hyvault-api](hyvault-api/)
The core API that economy providers implement and other plugins depend on.
- Clean interfaces for economy operations
- Async/await support with CompletableFuture
- Kotlin coroutines support via adapters
- Multiple economy provider support

### [hyvault-plugin](hyvault-plugin/)
The runtime plugin that manages economy providers and configuration.
- Economy provider registry
- Configuration management
- Default economy selection
- Multi-economy support toggle

##  Quick Start

### For Plugin Developers (Using the Economy API)

Add HyVault API to your dependencies:

```kotlin
dependencies {
    compileOnly("fi.sulku.hytale:hyvault-api:0.1.0-SNAPSHOT")
}
```

Get the economy and there is 3 ways to use it:

2 ways for java:

```java
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

//----Way 1 recommended: using completable futures----
// Get the default economy
PlayerEconomy economy = HyVault.getEconomy();
// Or get a specific economy by name
PlayerEconomy economy = HyVault.getEconomy("myeconomyid");
// Use the economy, returns completable future
CompletableFuture<BigRecimal> balanceFuture = economy.getBalance(playerUUID);
CompletableFuture<EconomyResult> result = economy.setBalance(playerUUID, BigDecimal.valueOf(200));

//----Way 2: using sync wrapper----
// Get the default economy
EconomySyncAdapter economy = HyVault.getEconomy().asSync(); // asSync is a wrapper so you don't have to use completablefutures.
// Or get a specific economy by name
EconomySyncAdapter economy = HyVault.getEconomy("myeconomyid").asSync();
// Use the economy, returns completable future
BigRecimal balance = economy.getBalance(playerUuid);
EconomyResult result = economy.setBalance(playerUUID, BigDecimal.valueOf(200));
```

Kotlin:
```kotlin

//----Way 1 recommended: using completable futures----
// Get the default economy
val economy = HyVault.getEconomy()
// Or get a specific economy by name
val economy = HyVault.getEconomy("myeconomyid")
// Use the economy, returns completable future
val balanceFuture : CompletableFuture<BigRecimal>  = economy.getBalance(playerUUID)
val result : CompletableFuture<EconomyResult> = economy.setBalance(playerUUID, BigRecimaleger.valueOf(200))

//----Way 2: using sync wrapper----
// Get the default economy
val economy : EconomySyncAdapter = HyVault.getEconomy().asSuspend() // wraps economy with suspend functions
// Or get a specific economy by name
val economy : EconomySyncAdapter = HyVault.getEconomy("myeconomyid").asSync()
// Use the economy, returns completable future
val balance : BigRecimal = economy.getBalance(playerUuid)
val result : EconomyResult = economy.setBalance(playerUUID, 500.toBigDecimal())
```
## Documentation

- [API Documentation](hyvault-api/README.md) - For developers using the economy API
- [Plugin Documentation](hyvault-plugin/README.md) - For server administrators and economy providers


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
