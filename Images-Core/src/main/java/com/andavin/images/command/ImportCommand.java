/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.LegacyImportManager;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @since September 23, 2019
 * @author Andavin
 */
public class ImportCommand extends BaseCommand {

    private static final String CONFIRM_META = "import-confirm";
    private static final long COOLDOWN = TimeUnit.MINUTES.toMillis(5);

    private long lastRun;

    ImportCommand() {
        super("import", "images.command.import");
        this.setAliases("legacyImport");
        this.setDesc("Importe e destrua imagens e as re-crie em um novo formato.");
        this.setUsage("/image import");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (TimeoutMetadata.isExpired(player, CONFIRM_META)) {
            player.sendMessage(new String[] {
              "",
              "§a Tem certeza que deseja importar as imagens?",
              "§7 Digite o comando novamente para confirmar.",
              ""
            });

            player.setMetadata(CONFIRM_META, new TimeoutMetadata(15, TimeUnit.SECONDS));
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRun < COOLDOWN) {
            player.sendMessage("§cAguarde 5 minutos para executar este comando novamente.");
            return;
        }

        lastRun = now;
        player.sendMessage("§aImportando imagens...");
        List<CustomImage> importedImages = LegacyImportManager.importImages(
                Images.getImagesDirectory(), Images.getDataManager());
        Scheduler.async(() -> {
            Images.addImages(importedImages);
            player.sendMessage("§aForam importadas " + importedImages.size() + " imagens!");
        });
    }
}
