package plugin.kicksystem;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class KickSystem extends JavaPlugin implements Listener {

    private final Map<UUID, MathChallenge> activeChallenges = new HashMap<>();
    private Essentials essentials;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Essentials") instanceof Essentials) {
            essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        scheduleMathChallenges();
    }

    private void scheduleMathChallenges() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!essentials.getUser(player).isAfk()) {
                        int a = random.nextInt(1000) + 1;
//                      int b = random.nextInt(10) + 1;
//                      int result = a + b;
                        String title = (ChatColor.RED + "Anti AFK Farming");
                        String subtitle = (ChatColor.YELLOW + "Type in chat the following number:" + a);
                        int fadeIn = 10;
                        int fadeOut = 20;
                        int stay = 240;
                        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                        UUID playerId = player.getUniqueId();
                        activeChallenges.put(playerId, new MathChallenge(player, a));
                        player.sendMessage("\n\n\n" + ChatColor.YELLOW + "===============[" + ChatColor.DARK_RED + "!" + ChatColor.YELLOW + "]===============" + "\n\n" + ChatColor.RED + "This is a message from anti AFK farming system, please write in the chat the following number: " + a + "\n\n" + ChatColor.YELLOW + "===============[" + ChatColor.DARK_RED + "!" + ChatColor.YELLOW + "]===============" + "\n\n\n");

                        // Schedule a task to kick the player if they don't answer within 2 minutes
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (activeChallenges.containsKey(playerId)) {
                                    player.kickPlayer("\n" + ChatColor.YELLOW + "===============[!]===============" + "\n" + ChatColor.RED + "You did not answer the math question in time! You have been kicked from the anti AFK farm system." + "\n" + ChatColor.YELLOW + "===============[!]===============" + "\n");
                                    System.out.println(ChatColor.BLUE + "Player: " + player + " has been kicked due to inactivity. (Didn't answer)");
                                    activeChallenges.remove(playerId);
                                }
                            }
                        }.runTaskLater(KickSystem.this, 2400L); // 4800 ticks = 4 minutes
                    }
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
                    Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(ChatColor.RED + "Wrong answer! You have been kicked from the AFK farm system."));
                    System.out.println(ChatColor.BLUE + "Player: " + player + " has been kicked due to inactivity. (Wrong answer)" );
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
