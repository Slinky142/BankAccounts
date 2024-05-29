package pro.cloudnode.smp.bankaccounts.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.util.Optional;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final @NotNull Optional<@NotNull Double> startingBalance = BankAccounts.getInstance().config()
                .startingBalance();
        startingBalance.ifPresent(aDouble -> BankAccounts.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(BankAccounts.getInstance(), () -> {
                    if (Account.getVaultAccount(player).isEmpty()) {
                        // if the player already has a personal account, they will not be given starting balance
                        final @NotNull BigDecimal balance = Account.get(player, Account.Type.PERSONAL).length > 0 ? BigDecimal.ZERO : BigDecimal.valueOf(aDouble);
                        new Account(player, Account.Type.VAULT, null, balance, false).insert();
                    }
                }));
        if (player.hasPermission(Permissions.NOTIFY_UPDATE)) {
            BankAccounts.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(BankAccounts.getInstance(), () -> BankAccounts.checkForUpdates().ifPresent(latestVersion -> {
                player.sendMessage(BankAccounts.getInstance().config().messagesUpdateAvailable(latestVersion));
            }), 20L);
        }
    }
}
