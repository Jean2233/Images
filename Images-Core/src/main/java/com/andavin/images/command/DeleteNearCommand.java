package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.image.CustomImage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @since July 05, 2020
 * @author Andavin
 */
final class DeleteNearCommand extends BaseCommand {

    DeleteNearCommand() {
        super("near", "images.command.delete.near");
        this.setAliases("n");
        this.setMinimumArgs(1);
        this.setUsage("/image delete near <range>");
        this.setDesc("Remova todas as imagens em um raio");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        int range;
        try {
            range = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cO raio '" + args[0] + "' é inválido.");
            return;
        }

        if (range < 1) {
            player.sendMessage("§cO raio deve ser maior ou igual a 1.");
            return;
        }

        Location location = player.getLocation();
        List<CustomImage> images = Images.getMatchingImages(image -> image.isInRange(location, range));
        if (images.isEmpty()) {
            player.sendMessage("§cNenhuma imagem encontrada neste raio.");
            return;
        }

        int success = 0;
        for (CustomImage image : images) {

            if (Images.removeImage(image)) {
                image.destroy();
                success++;
            }
        }

        if (success == images.size()) {
            player.sendMessage("§aForam excluídas " + success + " imagens em " + range + " blocos.");
        } else {

            player.sendMessage("§cForam encontradas " + images.size() + " imagens.");
            if (success > 0) {
                player.sendMessage("§cPorém, apenas " + success + " foram removidas.");
            } else {
                player.sendMessage("§cPorém, ocorreu um erro ao removê-las.");
            }
        }
    }
}
