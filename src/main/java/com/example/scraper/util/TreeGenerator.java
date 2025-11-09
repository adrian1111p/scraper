package com.example.scraper.util;

import com.example.scraper.model.FileNode;

import java.util.List;

public class TreeGenerator {

    public static String generateTree(List<String> pathSegments, FileNode node, int depth, boolean isLast) {
        StringBuilder tree = new StringBuilder();
        String prefix = getPrefix(depth, isLast);

        String typeEmoji = node.isDirectory() ? "📁" : "📄";
        String sizeInfo = node.isDirectory() ? "" : " (" + readableSize(node.getSize()) + ")";
        tree.append(prefix).append(typeEmoji).append(" ").append(node.getName()).append(sizeInfo).append("\n");

        return tree.toString();
    }

    private static String getPrefix(int depth, boolean isLast) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < depth - 1; i++) {
            prefix.append("│   ");
        }
        if (depth > 0) {
            prefix.append(isLast ? "└── " : "├── ");
        }
        return prefix.toString();
    }

    private static String readableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), unit);
    }
}
