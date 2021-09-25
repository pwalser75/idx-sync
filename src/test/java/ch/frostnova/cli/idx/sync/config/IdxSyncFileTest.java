package ch.frostnova.cli.idx.sync.config;

import ch.frostnova.cli.idx.sync.console.Console;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class IdxSyncFileTest {

    @Test
    void shouldReadWriteSyncFileJSON() throws IOException {
        shouldReadWriteSyncFile(ObjectMappers.json());
    }

    @Test
    void shouldReadWriteSyncFileYAML() throws IOException {
        shouldReadWriteSyncFile(ObjectMappers.yaml());
    }

    private static void shouldReadWriteSyncFile(ObjectMapper objectMapper) throws IOException {
        IdxSyncFile testData = testData();

        String serialized = objectMapper.writeValueAsString(testData);
        Console.println(serialized);

        IdxSyncFile deserialized = objectMapper.readValue(serialized, IdxSyncFile.class);

        assertThat(deserialized).usingRecursiveComparison().isEqualTo(testData);
    }

    private static IdxSyncFile testData() {
        IdxSyncFile idxSyncFile = new IdxSyncFile();
        idxSyncFile.setFolderId(randomUUID().toString());
        idxSyncFile.setFolderName("BACKUP Dev");
        idxSyncFile.setExcludePatterns(Stream.of("**/.git", "**/node-modules").collect(toSet()));
        idxSyncFile.setIncludeHidden(true);
        idxSyncFile.setSourceFolderId(randomUUID().toString());
        idxSyncFile.setTags(Stream.of("DEV", "BACKUP", "DAILY").collect(toSet()));
        return idxSyncFile;
    }
}
