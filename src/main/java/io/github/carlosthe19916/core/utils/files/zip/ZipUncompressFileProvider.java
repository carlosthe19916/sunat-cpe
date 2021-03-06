package io.github.carlosthe19916.core.utils.files.zip;

import io.github.carlosthe19916.core.utils.files.UncompressFileProvider;
import io.github.carlosthe19916.core.utils.files.exceptions.NotReadableCompressFileException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUncompressFileProvider implements UncompressFileProvider {

    @Override
    public String getSupportedExtension() {
        return "zip";
    }

    @Override
    public Map<String, byte[]> uncompress(byte[] bytes) throws NotReadableCompressFileException {
        Map<String, byte[]> entries = new HashMap<>();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ZipInputStream zis = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryFileName = entry.getName();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte buffer[] = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, count);
                }

                entries.put(entryFileName, bos.toByteArray());
            }
        } catch (Exception e) {
            throw new NotReadableCompressFileException("Could not unzip");
        }

        return entries;
    }

}
