package io.github.jxch.platform.frp.client.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileUtil {

    public static void ensureDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory: " + dir, e);
        }
    }

    public static void writeAtomic(Path target, String content) {
        ensureDirectory(target.getParent());

        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Path bak = target.resolveSibling(target.getFileName() + ".bak");

        try {
            Files.writeString(tmp, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            if (Files.exists(target)) {
                Files.copy(target, bak, StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write atomic file: " + target, e);
        }
    }

    public static void restoreBackup(Path target) {
        Path bak = target.resolveSibling(target.getFileName() + ".bak");
        if (!Files.exists(bak)) {
            return;
        }
        try {
            Files.copy(bak, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to restore backup: " + bak, e);
        }
    }

}
