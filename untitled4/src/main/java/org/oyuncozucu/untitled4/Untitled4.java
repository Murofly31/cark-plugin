package org.oyuncozucu.untitled4;
import org.oyuncozucu.untitled4.CarkManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Untitled4 extends JavaPlugin implements Listener {

    private CarkManager carkManager;

    @Override
    public void onEnable() {
        this.carkManager = new CarkManager(this);
        Bukkit.getPluginManager().registerEvents(carkManager, this);
        getLogger().info("Paper çark plugini aktif!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("carkcevir") && sender instanceof Player) {
            carkManager.openCarkGui((Player) sender);
            return true;
        }
        return false;
    }
}