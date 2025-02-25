package org.jreleaser.engine.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class InitTest {
    @Test
    void jsonSyntaxIsCorrect(@TempDir Path outputDirectory) {
        // given:
        String format = "json";

        // when:
        Init.execute(null, format, false, outputDirectory);
        Path outputFile = outputDirectory.resolve("jreleaser." + format);

        // then:
        assertDoesNotThrow(() -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(Files.readString(outputFile));
        });
    }
}
