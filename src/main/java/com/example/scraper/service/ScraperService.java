package com.example.scraper.service;

import com.example.scraper.config.ScraperProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ScraperService {

    private final ScraperProperties config;

    public ScraperService(ScraperProperties config) {
        this.config = config;
    }

    @PostConstruct
    public void runOnStartup() throws IOException {
        runScraper();
    }

    public void runScraper() throws IOException {
        Path inputFolder = Paths.get(config.getInputFolder());
        Path outputFile = Paths.get(config.getOutputFile());

        System.out.println("\u2b6e\ufe0f Running scraper...");
        System.out.println("\ud83d\udcc2 Input folder: " + inputFolder.toAbsolutePath());
        System.out.println("\ud83d\udcc4 Output file: " + outputFile.toAbsolutePath());

        if (!Files.exists(inputFolder)) {
            System.err.println("\u274c Input folder does not exist.");
            return;
        }

        List<Path> filesToMerge = findMatchingFiles(inputFolder);
        System.out.println("\ud83e\uddfE Found " + filesToMerge.size() + " files.");

        if (filesToMerge.isEmpty()) {
            System.out.println("\u26a0\ufe0f No files to merge.");
            return;
        }

        writeMergedContent(filesToMerge, outputFile);
        System.out.println("\u2705 Scraper completed.");
    }

    private List<Path> findMatchingFiles(Path folder) throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        boolean matches = matchesExtension(path);
                        boolean notExcludedFolder = notInExcludedFolder(path);
                        boolean notExcludedFile = notInExcludedFiles(path);

                        if (!matches) {
                            System.out.println("\u274c Skipped (extension): " + path);
                        }
                        if (!notExcludedFolder) {
                            System.out.println("\u274c Skipped (excluded folder): " + path);
                        }
                        if (!notExcludedFile) {
                            System.out.println("\u274c Skipped (excluded file): " + path);
                        }

                        return matches && notExcludedFolder && notExcludedFile;
                    })
                    .collect(Collectors.toList());
        }
    }

    private boolean matchesExtension(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.equals("dockerfile")) {
            return true;
        }
        return config.getIncludeExtensions().stream()
                .anyMatch(ext -> filename.endsWith("." + ext.toLowerCase()));
    }

    private boolean notInExcludedFolder(Path path) {
        String normalizedPath = path.toAbsolutePath().toString().replace("\\", "/");
        return config.getExcludeFolders().stream()
                .noneMatch(excluded -> normalizedPath.contains("/" + excluded + "/"));
    }

    private boolean notInExcludedFiles(Path path) {
        String filename = path.getFileName().toString();
        if (config.getExcludeFilePatterns() == null) return true;

        return config.getExcludeFilePatterns().stream()
                .noneMatch(pattern -> globMatches(pattern, filename));
    }

    private boolean globMatches(String pattern, String filename) {
        if (!config.isExcludePatternCaseSensitive()) {
            pattern = pattern.toLowerCase();
            filename = filename.toLowerCase();
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        return matcher.matches(Paths.get(filename));
    }

    private void writeMergedContent(List<Path> files, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            for (Path file : files) {
                writer.write("/* ==== " + file.toAbsolutePath() + " ==== */\n");
                Files.lines(file, StandardCharsets.UTF_8).forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("\u274c Failed writing line: " + e.getMessage());
                    }
                });
                writer.write("\n");
                writer.write("------ next text file. ------\n");
                writer.write("\n");
            }
        }
    }
}
