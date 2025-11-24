package org.unipus.util;

/* (っ*´Д`)っ 小代码要被看光啦 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    /**
     * 将字符串内容作为一个条目写入 ZipOutputStream，而不在磁盘上创建中间文件。
     *
     * @param zipOut     Zip 输出流
     * @param entryName  在 zip 中的文件名（可包含路径，例如 "folder/file.txt"）
     * @param content    要写入该条目的字符串内容
     * @throws IOException 写入 zip 过程中发生 I/O 错误时抛出
     */
    public static void addStringAsZipEntry(ZipOutputStream zipOut,
                                            String entryName,
                                            String content) throws IOException {
        // 创建一个新的 zip 条目
        ZipEntry entry = new ZipEntry(entryName);
        zipOut.putNextEntry(entry);

        // 将字符串内容按 UTF-8 编码写入
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        zipOut.write(data);

        // 结束当前条目
        zipOut.closeEntry();
    }
}
