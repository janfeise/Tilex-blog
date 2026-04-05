package blog.tilex.backend.Tilex_blog_backend.utils.gallery;

import blog.tilex.backend.Tilex_blog_backend.config.GalleryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件验证
 * 验证上传文件的大小、类型等
 */
@Component
@Slf4j
public class FileValidator {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico"
    );

    private final GalleryConfig galleryConfig;

    public FileValidator(GalleryConfig galleryConfig) {
        this.galleryConfig = galleryConfig;
    }

    /**
     * 验证文件
     * 
     * @param file 要验证的文件
     * @throws IllegalArgumentException 如果文件无效
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 检查文件大小
        long maxSizeBytes = (long) galleryConfig.getMaxUploadSize() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                String.format("File size exceeds limit of %dMB",
                    galleryConfig.getMaxUploadSize()));
        }

        // 检查文件类型
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                "Unsupported file type: " + extension +
                ". Allowed types: " + ALLOWED_EXTENSIONS);
        }

        log.debug("File validation passed: {}", file.getOriginalFilename());
    }

    /**
     * 提取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
}
