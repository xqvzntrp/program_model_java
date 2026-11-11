package programmodel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import programmodel.io.CsvStore;
import programmodel.model.ModelRow;

/** Explicit copying from a published release into a new, UNSELECTED draft. */
public final class ReleaseLoader {
    private ReleaseLoader() {}

    public static int load(Path published, Path draft, String sourceRelease, String targetRelease) throws IOException {
        if (sourceRelease.isBlank() || targetRelease.isBlank() || sourceRelease.equals(targetRelease))
            throw new IllegalArgumentException("Source and target must be distinct, nonblank releases");
        if (!Files.exists(published)) throw new IllegalArgumentException("No published dataset yet");
        List<ModelRow> source = CsvStore.read(published);
        if (source.stream().anyMatch(r -> r.releaseId().equals(targetRelease)))
            throw new IllegalArgumentException("Target release has already been published: " + targetRelease);
        List<ModelRow> cloned = source.stream().filter(r -> r.releaseId().equals(sourceRelease))
                .map(r -> r.forRelease(targetRelease)).toList();
        if (cloned.isEmpty()) throw new IllegalArgumentException("Source release not found: " + sourceRelease);
        // Deliberate full draft replacement, never an implicit carry-forward.
        CsvStore.write(draft, cloned);
        return cloned.size();
    }
}
