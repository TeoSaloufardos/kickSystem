package plugin.kicksystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class KickSystem extends JavaPlugin implements Listener {

    private final Map<UUID, MathChallenge> activeChallenges = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        scheduleMathChallenges();
    }

    private void scheduleMathChallenges() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int a = random.nextInt(10) + 1;
                    int b = random.nextInt(10) + 1;
                    int result = a + b;
                    UUID playerId = player.getUniqueId();
                    activeChallenges.put(playerId, new MathChallenge(player, result));
                    player.sendMessage(ChatColor.GREEN + "This is a message from anti AFK farm system, please solve this math problem within 2 mins (Write your answer in the chat): " + a + " + " + b + " = ?");

                    // Schedule a task to kick the player if they don't answer within 2 minutes
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (activeChallenges.containsKey(playerId)) {
                                player.kickPlayer("You did not answer the math question in time! You have been kicked from the anti AFK farm system.");
                                activeChallenges.remove(playerId);
                            }
                        }
                    }.runTaskLater(KickSystem.this, 2400L); // 2400 ticks = 2 minutes
                }
            }
        }.runTaskTimer(this, 0L, 72000L); // 72000 ticks = 1 hour
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (activeChallenges.containsKey(playerId)) {
            try {
                int answer = Integer.parseInt(event.getMessage());
                MathChallenge challenge = activeChallenges.get(playerId);
                if (answer == challenge.getResult()) {
                    player.sendMessage(ChatColor.GREEN + "Correct answer! You can keep playing.");
                } else {
                    Bukkit.getScheduler().runTask(this, () -> player.kickPlayer("Wrong answer! You have been kicked from the AFK farm system."));
                }
                activeChallenges.remove(playerId);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number.");
            }
            event.setCancelled(true); // Prevent the message from being shown in the public chat
        }
    }

    private static class MathChallenge {
        private final Player player;
        private final int result;

        public MathChallenge(Player player, int result) {
            this.player = player;
            this.result = result;
        }

        public Player getPlayer() {
            return player;
        }

        public int getResult() {
            return result;
        }
    }
}
