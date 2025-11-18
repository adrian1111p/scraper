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

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @PostConstruct
    public void runOnStartup() throws IOException {
        runScraper(properties.getInputFolder(), properties.getOutputFile());
    }

    public void runScraper() throws IOException {
        runScraper(properties.getInputFolder(), properties.getOutputFile());
    }

    public void runScraper(String inputFolder, String outputFile) throws IOException {
        System.out.println("➡️ Running scraper on " + (IS_WINDOWS ? "Windows" : "Linux/Mac") + "...");

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
            writer.write("📂 Input folder: " + inputPath.toAbsolutePath() + "\n");
            writer.write("📄 Output file: " + outputPath.toAbsolutePath() + "\n");
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
        String filename = path.getFileName().toString().toLowerCase();
        boolean matches = properties.getIncludeExtensions().stream()
                .anyMatch(ext -> filename.endsWith("." + ext.toLowerCase()));

        boolean notExcludedFolder = properties.getExcludeFolders().stream()
                .noneMatch(folder -> path.toString().toLowerCase().contains(FileSystems.getDefault().getSeparator() + folder.toLowerCase()));

        boolean notExcludedFile = properties.getExcludeFilePatterns().stream().noneMatch(p -> {
            String regex = p.replace(".", "\\.").replace("*", ".*").replace("?", ".");
            return filename.matches(properties.isExcludePatternCaseSensitive() ? regex : regex.toLowerCase());
        });

        return matches && notExcludedFolder && notExcludedFile;
    }

    private boolean shouldExclude(Path dir) {
        String dirStr = dir.toString().toLowerCase();
        return properties.getExcludeFolders().stream()
                .anyMatch(folder -> dirStr.contains(FileSystems.getDefault().getSeparator() + folder.toLowerCase()));
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
            writer.write(treeLine);
            if (node.isDirectory()) {
                prefixStack.push(last ? "    " : "│   ");
                walkTree(writer, map, current.resolve(node.getName()), depth + 1, last, prefixStack);
                prefixStack.pop();
            }
        }
    }
}
