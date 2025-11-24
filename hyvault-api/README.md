# HyVault API

The HyVault API module provides interfaces and classes for interacting with economy systems in Hytale servers.

## Installation

### Gradle (Kotlin DSL)
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencies {
    implementation("com.github.sulku-studios:hyvault-api:v0.1.0-ALPHA")
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.sulku-studios</groupId>
    <artifactId>hyvault-api</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## 🎯 Usage

### For Plugin Developers

#### Getting an Economy

```java
// Get the default economy (configured in hyvault-plugin config.yml)
PlayerEconomy economy = HyVault.getEconomy();

// Get a specific economy by ID
PlayerEconomy economy = HyVault.getEconomy("gold");
```

#### Using Economy Methods

All economy operations return `CompletableFuture<EconomyResult>` for async operations:

```java
import fi.sulku.hytale.economy.api.PlayerEconomy;
import fi.sulku.hytale.economy.api.EconomyResult;
import fi.sulku.hytale.economy.api.ResultType;
import fi.sulku.hytale.economy.api.HyVault;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// Check if player has an account
CompletableFuture<Boolean> hasAccount = economy.hasAccount(playerUuid);
hasAccount.thenAccept(exists -> {
    if (exists) {
        System.out.println("Player has an account");
    }
});

// Create an account
economy.createAccount(playerUuid).thenAccept(success -> {
    if (success) {
        System.out.println("Account created successfully");
    }
});

// Get balance
economy.getBalance(playerUuid).thenAccept(result -> {
    if (result.getStatus() == ResultType.SUCCESS) {
        System.out.println("Balance: " + result.getBalanceAfter());
    }
});

// Deposit money
economy.deposit(playerUuid, new BigDecimal("100")).thenAccept(result -> {
    if (result.getStatus() == ResultType.SUCCESS) {
        System.out.println("Deposited " + result.getAmount() + 
                         ". New balance: " + result.getBalanceAfter());
    }
});

// Withdraw money
economy.withdraw(playerUuid, new BigDecimal("50")).thenAccept(result -> {
    if (result.getStatus() == ResultType.SUCCESS) {
        System.out.println("Withdrawn " + result.getAmount());
    } else {
        System.out.println("Withdrawal failed: " + result.getErrorMessage());
    }
});

// Check if player has enough money
economy.has(playerUuid, new BigDecimal("25")).thenAccept(hasEnough -> {
    if (hasEnough) {
        System.out.println("Player has enough money");
    }
});

// Set balance
economy.setBalance(playerUuid, new BigDecimal("1000")).thenAccept(result -> {
    if (result.getStatus() == ResultType.SUCCESS) {
        System.out.println("Balance set to: " + result.getBalanceAfter());
    }
});
```

#### Chaining Operations

You can chain CompletableFuture operations for complex workflows:

```java
// Example: Check if account exists, create if needed, then deposit
economy.hasAccount(playerUuid)
    .thenCompose(exists -> {
        if (!exists) {
            return economy.createAccount(playerUuid)
                .thenCompose(created -> economy.deposit(playerUuid, new BigDecimal("100")));
        } else {
            return economy.deposit(playerUuid, new BigDecimal("100"));
        }
    })
    .thenAccept(result -> {
        if (result.getStatus() == ResultType.SUCCESS) {
            System.out.println("Deposit completed! New balance: " + result.getBalanceAfter());
        }
    })
    .exceptionally(throwable -> {
        System.err.println("Error: " + throwable.getMessage());
        return null;
    });
```

#### Blocking Operations (Not Recommended)

If you need synchronous behavior, you can block on the future (use sparingly to avoid blocking the main thread):

```java
try {
    EconomyResult result = economy.getBalance(playerUuid).get();
    if (result.getStatus() == ResultType.SUCCESS) {
        System.out.println("Balance: " + result.getBalanceAfter());
    }
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}
```

#### Formatting Currency

```java
String formatted = economy.format(new BigDecimal("1234.56"));
// Output depends on implementation, e.g., "$1,234.56" or "1234.56 Gold"
```

### For Economy Provider Developers

Implement your own economy system by implementing the `PlayerEconomy` interface:

```java
import fi.sulku.hytale.economy.api.PlayerEconomy;
import fi.sulku.hytale.economy.api.EconomyResult;
import fi.sulku.hytale.economy.api.ResultType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MyCustomEconomy implements PlayerEconomy {
    
    @Override
    public boolean getIsEnabled() {
        return true;
    }
    
    @Override
    public String getId() {
        return "mycurrency";
    }
    
    @Override
    public String getName() {
        return "My Custom Currency";
    }
    
    @Override
    public String getCurrencyPlural() {
        return "Coins";
    }
    
    @Override
    public String getCurrencySingular() {
        return "Coin";
    }
    
    @Override
    public int getFractionalDigits() {
        return 2;
    }
    
    @Override
    public String format(BigDecimal amount) {
        return "$" + amount.setScale(getFractionalDigits(), RoundingMode.HALF_UP);
    }
    
    @Override
    public CompletableFuture<Boolean> createAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Your account creation logic
            // e.g., insert into database
            return true;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> hasAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if account exists in your storage
            return true;
        });
    }
    
    @Override
    public CompletableFuture<EconomyResult> getBalance(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Get balance from your storage (database, file, etc.)
            BigDecimal balance = new BigDecimal("1000");
            
            return new EconomyResult(
                ResultType.SUCCESS,
                null,
                null,
                balance,
                ""
            );
        });
    }
    
    @Override
    public CompletableFuture<EconomyResult> deposit(UUID uuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            // Get current balance
            BigDecimal oldBalance = new BigDecimal("1000"); // From your storage
            BigDecimal newBalance = oldBalance.add(amount);
            
            // Save new balance to your storage
            
            return new EconomyResult(
                ResultType.SUCCESS,
                amount,
                oldBalance,
                newBalance,
                ""
            );
        });
    }
    
    @Override
    public CompletableFuture<EconomyResult> withdraw(UUID uuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            // Get current balance
            BigDecimal oldBalance = new BigDecimal("1000"); // From your storage
            
            // Check if player has enough
            if (oldBalance.compareTo(amount) < 0) {
                return new EconomyResult(
                    ResultType.FAILURE,
                    amount,
                    oldBalance,
                    oldBalance,
                    "Insufficient funds"
                );
            }
            
            BigDecimal newBalance = oldBalance.subtract(amount);
            
            // Save new balance to your storage
            
            return new EconomyResult(
                ResultType.SUCCESS,
                amount,
                oldBalance,
                newBalance,
                ""
            );
        });
    }
    
    @Override
    public CompletableFuture<Boolean> has(UUID uuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            // Get balance from your storage
            BigDecimal balance = new BigDecimal("1000");
            return balance.compareTo(amount) >= 0;
        });
    }
    
    @Override
    public CompletableFuture<EconomyResult> setBalance(UUID uuid, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            // Get current balance
            BigDecimal oldBalance = new BigDecimal("1000"); // From your storage
            
            // Set new balance in your storage
            
            return new EconomyResult(
                ResultType.SUCCESS,
                null,
                oldBalance,
                amount,
                ""
            );
        });
    }
}
```

#### Registering Your Economy

In your plugin's main class:

```java
public class YourEconomyPlugin extends Plugin {
    private MyCustomEconomy economy;
    
    @Override
    public void onEnable() {
        economy = new MyCustomEconomy();
        HyVault.registerEconomy(economy);
        getLogger().info(economy.getName() + " registered with HyVault!");
    }
    
    @Override
    public void onDisable() {
        HyVault.unregisterEconomy(economy.getId());
        getLogger().info(economy.getName() + " unregistered from HyVault!");
    }
}
```

## 🔄 Adapters

### `EconomySuspendAdapter`
Converts `PlayerEconomy` to use Kotlin suspend functions for coroutine support.

### `EconomySyncAdapter`
Provides blocking/synchronous wrappers for economy methods. Useful when you need synchronous behavior but be careful not to block the main thread.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
