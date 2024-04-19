package ch.frostnova.cli.idx.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class IdxSyncFileTest {

    private static void shouldReadWriteSyncFile(ObjectMapper objectMapper) throws IOException {
        var testData = testData();

        var serialized = objectMapper.writeValueAsString(testData);
        System.out.println(serialized);

        var deserialized = objectMapper.readValue(serialized, IdxSyncFile.class);

        assertThat(deserialized).usingRecursiveComparison().isEqualTo(testData);
    }

    private static IdxSyncFile testData() {
        var idxSyncFile = new IdxSyncFile();
        idxSyncFile.setFolderId(randomUUID().toString());
        idxSyncFile.setFolderName("BACKUP Dev");
        idxSyncFile.setExcludePatterns(Set.of(
                "**/node-modules",
                "**/.git",
                "$RECYCLE.BIN",
                "System Volume Information"));
        idxSyncFile.setIncludeHidden(true);
        idxSyncFile.setSourceFolderId(randomUUID().toString());
        idxSyncFile.setTags(Stream.of("DEV", "BACKUP", "DAILY").collect(toSet()));
        return idxSyncFile;
    }

    @Test
    void shouldReadWriteSyncFileJSON() throws IOException {
        shouldReadWriteSyncFile(ObjectMappers.json());
    }

    @Test
    void shouldReadWriteSyncFileYAML() throws IOException {
        shouldReadWriteSyncFile(ObjectMappers.yaml());
    }
}
