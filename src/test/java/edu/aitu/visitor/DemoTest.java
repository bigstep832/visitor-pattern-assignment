package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoTest {
    @Test
    void defaultDemoHasReproducibleOutput() {
        assertEquals("""
                Root: campus
                Total size: 3568 bytes
                Search: suffix=.txt, minimum=300
                  docs/architecture.txt
                  docs/archive/old-notes.txt
                Matches: 2
                Files: 6
                Directories: 6
                Maximum depth: 3
                """, Demo.run(".txt", 300));
    }

    @Test
    void changingTheSearchConfigurationChangesOnlyMatches() {
        assertEquals("""
                Root: campus
                Total size: 3568 bytes
                Search: suffix=.png, minimum=1000
                  assets/logo.png
                Matches: 1
                Files: 6
                Directories: 6
                Maximum depth: 3
                """, Demo.run(".png", 1000));
    }
}
