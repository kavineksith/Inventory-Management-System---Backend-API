package com.example.inventory.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${backup.mysql.bin.path:}")
    private String mysqlBinPath;

    @Value("${backup.mysql.database:}")
    private String mysqlDatabase;

    @Value("${backup.mysql.user:}")
    private String mysqlUser;

    @Value("${backup.mysql.password:}")
    private String mysqlPassword;

    @Value("${backup.h2.file.path:}")
    private String h2FilePath;

    @Value("${backup.dir}")
    private String backupDir;

    @Value("${backup.retention.days:7}")
    private int retentionDays;

    @Scheduled(cron = "0 0 2 * * ?") // every day at 2 AM
    public void performBackup() throws IOException {
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String backupFilePath = null;

        if (datasourceUrl.contains("mysql")) {
            backupFilePath = backupMySQL();
        } else if (datasourceUrl.contains("h2")) {
            backupFilePath = backupH2();
        } else {
            System.out.println("No backup method implemented for DB URL: " + datasourceUrl);
            return;
        }

        if (backupFilePath != null) {
            String zipFilePath = backupFilePath + ".zip";
            zipFile(backupFilePath, zipFilePath);

            Files.deleteIfExists(Paths.get(backupFilePath));

            System.out.println("Backup compressed: " + zipFilePath);
        }

        cleanupOldBackups();
    }

    private String backupMySQL() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = backupDir + mysqlDatabase + "_" + timestamp + ".sql";

        ProcessBuilder pb = new ProcessBuilder(
                mysqlBinPath,
                "-u" + mysqlUser,
                "-p" + mysqlPassword,
                mysqlDatabase
        );
        pb.redirectOutput(new File(backupFile));
        pb.start();

        System.out.println("MySQL backup created: " + backupFile);
        return backupFile;
    }

    private String backupH2() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path source = Paths.get(h2FilePath);
        Path target = Paths.get(backupDir + "db_name_" + timestamp + ".mv.db");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("H2 backup created: " + target);
        return target.toString();
    }

    private void zipFile(String sourceFilePath, String zipFilePath) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zos = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(sourceFilePath)
        ) {
            ZipEntry zipEntry = new ZipEntry(new File(sourceFilePath).getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    private void cleanupOldBackups() {
        File dir = new File(backupDir);
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                Instant fileTime = Instant.ofEpochMilli(file.lastModified());
                if (fileTime.isBefore(cutoff)) {
                    if (file.delete()) {
                        System.out.println("Deleted old backup: " + file.getName());
                    }
                }
            }
        }
    }
}
