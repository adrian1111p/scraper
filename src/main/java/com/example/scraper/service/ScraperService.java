package com.example.scraper.service;

import com.example.scraper.config.ScraperProperties;
import com.example.scraper.model.FileNode;
import com.example.scraper.util.TreeGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScraperService {

    private final ScraperProperties properties;

    @PostConstruct
    public void runOnStartup() throws IOException {
        runScraper(properties.getInputFolder(), properties.getOutputFile());
    }

    /**
     * Public method for testing or external usage using configured paths.
     */
    public void runScraper() throws IOException {
        runScraper(properties.getInputFolder(), properties.getOutputFile());
    }

    public void runScraper(String inputFolder, String outputFile) throws IOException {
        System.out.println("➡️ Running scraper...");
        Path inputPath = Paths.get(inputFolder);
        Path outputPath = Paths.get(outputFile);

        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            System.err.println("❌ Invalid input folder: " + inputFolder);
            return;
        }

        Map<Path, List<FileNode>> treeMap = new TreeMap<>();
        List<Path> collectedFiles = new ArrayList<>();

        Files.walkFileTree(inputPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (shouldInclude(file)) {
                    collectedFiles.add(file);
                    addToTree(treeMap, file, false, file.toFile().length());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!shouldExclude(dir)) {
                    addToTree(treeMap, dir, true, 0);
                    return FileVisitResult.CONTINUE;
                } else {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile(), StandardCharsets.UTF_8))) {
            writer.write("📂 Input folder: " + inputFolder + "\n");
            writer.write("📄 Output file: " + outputFile + "\n");
            writer.write("🧾 Found " + collectedFiles.size() + " file(s).\n\n");
            writer.write("📁 Tree Structure:\n");

            generateTreeView(writer, treeMap, inputPath);

            writer.write("\n📦 Merged Content:\n\n");

            for (Path file : collectedFiles) {
                writer.write("/* ==== " + file.toAbsolutePath() + " ==== */\n");
                Files.lines(file, StandardCharsets.UTF_8).forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("⚠️ Failed to write line: " + e.getMessage());
                    }
                });
                writer.write("\n------ next text file. ------\n\n");
            }
        }

        System.out.println("✅ Scraper completed.");
    }

    private void addToTree(Map<Path, List<FileNode>> map, Path path, boolean isDir, long size) {
        Path parent = path.getParent();
        FileNode node = new FileNode(path.getFileName().toString(), isDir, size);
        map.computeIfAbsent(parent, k -> new ArrayList<>()).add(node);
    }

    private boolean shouldInclude(Path path) {
        String filename = path.getFileName().toString();
        boolean matches = properties.getIncludeExtensions().stream().anyMatch(filename::endsWith);
        boolean notExcludedFolder = properties.getExcludeFolders().stream().noneMatch(path.toString()::contains);
        boolean notExcludedFile = properties.getExcludeFilePatterns().stream().noneMatch(p ->
                filename.toLowerCase().matches(p.replace("*", ".*").replace("?", "."))
        );

        if (!matches) {
            System.out.println("❌ Skipped (extension): " + path);
        }
        if (!notExcludedFolder) {
            System.out.println("❌ Skipped (excluded folder): " + path);
        }
        if (!notExcludedFile) {
            System.out.println("❌ Skipped (excluded file): " + path);
        }

        return matches && notExcludedFolder && notExcludedFile;
    }

    private boolean shouldExclude(Path dir) {
        return properties.getExcludeFolders().stream().anyMatch(dir.toString()::contains);
    }

    private void generateTreeView(BufferedWriter writer, Map<Path, List<FileNode>> treeMap, Path root) throws IOException {
        Deque<String> prefixStack = new ArrayDeque<>();
        walkTree(writer, treeMap, root, 0, true, prefixStack);
    }

    private void walkTree(BufferedWriter writer, Map<Path, List<FileNode>> map, Path current, int depth, boolean isLast, Deque<String> prefixStack) throws IOException {
        List<FileNode> children = map.get(current);
        if (children == null) return;

        children.sort(Comparator.comparing(FileNode::getName));

        for (int i = 0; i < children.size(); i++) {
            FileNode node = children.get(i);
            boolean last = (i == children.size() - 1);
            String treeLine = TreeGenerator.generateTree(List.of(), node, depth + 1, last);
            writer.write(treeLine); // ❌ Don't add extra line
            if (node.isDirectory()) {
                prefixStack.push(last ? "    " : "│   ");
                walkTree(writer, map, current.resolve(node.getName()), depth + 1, last, prefixStack);
                prefixStack.pop();
            }
        }
    }
}
