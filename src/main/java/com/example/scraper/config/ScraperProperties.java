package com.example.scraper.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {

    private String inputFolder;
    private String outputFile;
    private List<String> includeExtensions;
    private List<String> excludeFolders;
    private List<String> excludeFilePatterns;
    private boolean excludePatternCaseSensitive = false;

    // NEW FIELD
    private boolean runOnStartup = true;

    public String getInputFolder() { return inputFolder; }
    public void setInputFolder(String inputFolder) { this.inputFolder = inputFolder; }

    public String getOutputFile() { return outputFile; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }

    public List<String> getIncludeExtensions() { return includeExtensions; }
    public void setIncludeExtensions(List<String> includeExtensions) { this.includeExtensions = includeExtensions; }

    public List<String> getExcludeFolders() { return excludeFolders; }
    public void setExcludeFolders(List<String> excludeFolders) { this.excludeFolders = excludeFolders; }

    public List<String> getExcludeFilePatterns() { return excludeFilePatterns; }
    public void setExcludeFilePatterns(List<String> excludeFilePatterns) { this.excludeFilePatterns = excludeFilePatterns; }

    public boolean isExcludePatternCaseSensitive() { return excludePatternCaseSensitive; }
    public void setExcludePatternCaseSensitive(boolean excludePatternCaseSensitive) { this.excludePatternCaseSensitive = excludePatternCaseSensitive; }

    public boolean isRunOnStartup() { return runOnStartup; }
    public void setRunOnStartup(boolean runOnStartup) { this.runOnStartup = runOnStartup; }

    @PostConstruct
    public void debug() {
        System.out.println("🔧 Properties loaded:");
        System.out.println("  input: " + inputFolder);
        System.out.println("  output: " + outputFile);
        System.out.println("  extensions: " + includeExtensions);
        System.out.println("  exclude folders: " + excludeFolders);
        System.out.println("  exclude file patterns: " + excludeFilePatterns);
        System.out.println("  run on startup: " + runOnStartup);
        System.out.println("  case-sensitive patterns: " + excludePatternCaseSensitive);
    }
}
