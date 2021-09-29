package ch.frostnova.cli.idx.sync.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class IdxSyncFile {

    public static final Path FILENAME = Paths.get(".idxsync");

    @JsonProperty("folder-id")
    private String folderId;

    @JsonProperty("folder-name")
    private String folderName;

    @JsonProperty("exclude-patterns")
    private Set<String> excludePatterns = new HashSet<>();

    @JsonProperty("include-hidden")
    private boolean includeHidden = false;

    @JsonProperty("source-folder-id")
    private String sourceFolderId;

    @JsonProperty("tags")
    private Set<String> tags = new HashSet<>();

    public static Path resolve(Path parent) {
        return parent.resolve(FILENAME);
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Set<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(Set<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public boolean isIncludeHidden() {
        return includeHidden;
    }

    public void setIncludeHidden(boolean includeHidden) {
        this.includeHidden = includeHidden;
    }

    public String getSourceFolderId() {
        return sourceFolderId;
    }

    public void setSourceFolderId(String sourceFolderId) {
        this.sourceFolderId = sourceFolderId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdxSyncFile that = (IdxSyncFile) o;
        return Objects.equals(folderId, that.folderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderId);
    }

    @Override
    public String toString() {
        return "IdxSyncFile{" +
                "folderId='" + folderId + '\'' +
                '}';
    }
}
