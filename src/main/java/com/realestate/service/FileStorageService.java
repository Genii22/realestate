package com.realestate.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Сохранение загруженных фото в локальную папку (по умолчанию ./uploads).
 * Возвращает публичный URL вида /uploads/<имя-файла>.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT =
            Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(root);
    }

    /**
     * Сохраняет файл и возвращает публичный URL ("/uploads/<filename>").
     * Возвращает null для пустого файла.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        String ext = extractExtension(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException(
                    "Допустимы только изображения (jpg/jpeg/png/webp/gif): " + original);
        }

        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Недопустимый путь файла");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл: " + original, e);
        }
        return "/uploads/" + filename;
    }

    /**
     * Удаляет файл по публичному URL (например, "/uploads/abc.jpg"). Если файл уже
     * отсутствует — игнорируется. URL-ы вне нашей папки uploads тоже игнорируются.
     */
    public void deleteByUrl(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        String filename = url.substring("/uploads/".length());
        // Защита от path-traversal: ".." или "/" недопустимы.
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) return;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) return;
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Файл не удалён — это не критично для удаления объявления.
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
