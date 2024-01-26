package pro.cloudnode.smp.bankaccounts.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.bankaccounts.Account;
import pro.cloudnode.smp.bankaccounts.BankAccounts;
import pro.cloudnode.smp.bankaccounts.Invoice;
import pro.cloudnode.smp.bankaccounts.Permissions;

import java.math.BigDecimal;
import java.util.Optional;

public final class Join implements Listener {
    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        BankAccounts.getInstance().getServer().getScheduler().runTaskAsynchronously(BankAccounts.getInstance(), () -> {
            final Player player = event.getPlayer();
            final @NotNull Optional<@NotNull Double> startingBalance = BankAccounts.getInstance().config()
                    .startingBalance();
            startingBalance.ifPresent(aDouble -> BankAccounts.getInstance().getServer().getScheduler()
                    .runTaskAsynchronously(BankAccounts.getInstance(), () -> {
                        final @NotNull Account[] accounts = Account.get(player, Account.Type.PERSONAL);
                        if (accounts.length == 0) {
                            new Account(player, Account.Type.PERSONAL, null, BigDecimal.valueOf(aDouble), false).insert();
                        }
                    }));
            if (player.hasPermission(Permissions.NOTIFY_UPDATE)) {
                BankAccounts.getInstance().getServer().getScheduler().runTaskLater(BankAccounts.getInstance(), () -> BankAccounts.checkForUpdates().ifPresent(latestVersion -> {
                    player.sendMessage(BankAccounts.getInstance().config().messagesUpdateAvailable(latestVersion));
                }), 20L);
            }

            if (player.hasPermission(Permissions.INVOICE_NOTIFY) && BankAccounts.getInstance().config().invoiceNotifyJoin()) {
                BankAccounts.getInstance().getServer().getScheduler().runTaskLater(BankAccounts.getInstance(), () -> {
                    final @NotNull Optional<@NotNull Component> message = BankAccounts.getInstance().config().messagesInvoiceNotify(Invoice.countUnpaid(player));
                    message.ifPresent(player::sendMessage);
                }, 20L);
            }
        });
    }
}
