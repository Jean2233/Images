package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.data.DataManager;
import com.andavin.images.data.FileDataManager;
import com.andavin.images.data.MySQLDataManager;
import com.andavin.images.data.SQLiteDataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @since July 05, 2020
 * @author Andavin
 */
public final class TransferCommand extends BaseCommand {

    private static final String KEY = "transfer.check";
    private static final String[] OPTIONS = { "MySQL", "SQLite", "File" };

    public TransferCommand() {
        super("transfer", "images.command.transfer");
        this.setAliases("datatransfer");
        this.setMinimumArgs(1);
        this.setDesc("Converta todos os dados para outro banco");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (TimeoutMetadata.isExpired(player, KEY)) {
            player.setMetadata(KEY, new TimeoutMetadata(20, TimeUnit.SECONDS));
            player.sendMessage(new String[] {
              "",
              "§a Tem certeza que deseja converter os dados?",
              "§7 Digite o comando novamente para confirmar.",
              ""
            });
            return;
        }

        player.removeMetadata(KEY, Images.getInstance());
        DataManager current = Images.getDataManager(), to;
        String type = args[0].toUpperCase(Locale.ENGLISH);
        switch (type) {
            case "MYSQL":

                if (current instanceof MySQLDataManager) {
                    player.sendMessage("§cO sistema já está utilizando o MySQL.");
                    return;
                }

                FileConfiguration config = Images.getInstance().getConfig();
                to = new MySQLDataManager(
                        config.getString("database.host"),
                        config.getInt("database.port"),
                        config.getString("database.schema"),
                        config.getString("database.user"),
                        config.getString("database.password")
                );

                break;
            case "SQLITE":

                if (current instanceof SQLiteDataManager) {
                    player.sendMessage("§cO sistema já está utilizando o SQLite.");
                    return;
                }

                to = new SQLiteDataManager(new File(Images.getImagesDirectory(), "images.db"));
                break;
            case "FILE":

                if (current instanceof FileDataManager) {
                    player.sendMessage("§cO sistema já está utilizando o Flat File.");
                    return;
                }

                to = new FileDataManager(new File(Images.getImagesDirectory(), "images.cimg"));
                break;
            default:
                player.sendMessage("§cTipo de banco de dados inválido.");
                return;
        }

        Scheduler.async(() -> {

            player.sendMessage("§aInicializando novo banco de dados...");
            to.initialize();
            List<CustomImage> images = Images.getMatchingImages(i -> true);
            images.forEach(image -> image.setId(-1)); // Reset the ID of the image so it can be reset
            player.sendMessage("§aSalvando " + images.size() + " imagens...");
            try {
                to.saveAll(images);
                player.sendMessage("§aTodas as imagens foram transferidas para o novo banco de dados!");
                Logger.info("Successfully transferred all images to {}", type);
                Logger.info("You may now change the database configuration to {}", type);
            } catch (Exception e) {
                player.sendMessage("§cOcorreu um erro ao transferir os dados.");
                Logger.severe(e);
            }
            // No matter what shutdown
            player.sendMessage("§cO servidor será fechado em 5 segundos...");
            Scheduler.laterAsync(Bukkit::shutdown, 100);
        });
    }

    @Override
    public void tabComplete(CommandSender sender, String[] args, List<String> completions) {

        String arg = args[0];
        for (String option : OPTIONS) {

            if (option.regionMatches(true, 0, arg, 0, arg.length())) {
                completions.add(option);
            }
        }
    }
}
