package org.source.spring.doc.infrastructure.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;

/**
 * 扫描路径工具类
 * <p>
 * 提供路径类型判断和包名转换功能
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@UtilityClass
public class ScanPathUtils {

    /**
     * 判断是否为绝对路径
     * <p>
     * 判断规则：
     * <ul>
     *     <li>以 / 开头 -> Unix/Mac 绝对路径</li>
     *     <li>以盘符开头（如 C:\）-> Windows 绝对路径</li>
     *     <li>其他情况视为包名格式</li>
     * </ul>
     * </p>
     *
     * @param path 路径字符串
     * @return true 如果是绝对路径
     */
    public boolean isAbsolutePath(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        // Unix/Mac 绝对路径
        if (path.startsWith("/")) {
            return true;
        }
        // Windows 绝对路径 (C:\, D:\ 等)
        if (path.matches("^\\w:[/\\\\].*")) {
            return true;
        }
        return false;
    }

    /**
     * 将包名转换为 src/main/java 下的路径
     * <p>
     * 例如：org.source.spring.doc.controller -> /project/src/main/java/org/source/spring/doc/controller
     * </p>
     *
     * @param packageName    包名
     * @param projectRootPath 项目根路径
     * @return 转换后的完整路径
     */
    public String convertPackageToPath(String packageName, String projectRootPath) {
        if (StringUtils.isBlank(packageName)) {
            return projectRootPath;
        }
        String packagePath = packageName.replace('.', '/');
        return Paths.get(projectRootPath, "src", "main", "java", packagePath).toString();
    }
}