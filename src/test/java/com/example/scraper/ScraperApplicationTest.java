package com.example.scraper;

import com.example.scraper.config.ScraperProperties;
import com.example.scraper.service.ScraperService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ScraperApplicationTest {

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private ScraperProperties config;

    @Test
    void testScrapingProducesOutputFile() throws Exception {
        Files.deleteIfExists(Path.of(config.getOutputFile()));
        // Run only once — explicitly
        scraperService.runScraper();

        Path outputPath = Path.of(config.getOutputFile());
        assertTrue(Files.exists(outputPath), "Output file should be created");
        assertTrue(Files.size(outputPath) > 0, "Output file should not be empty");
    }
}
