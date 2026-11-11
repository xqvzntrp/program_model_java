package programmodel.service;

import programmodel.model.ModelRow;
import programmodel.io.CsvStore;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Append-only quarterly publication using replace-by-atomic-file-swap, not a database transaction. */
public final class Publisher {
    private Publisher() {}

    public static int publish(Path draftFile, Path publishedFile, String release) throws IOException {
        if (release==null || release.isBlank()) throw new IllegalArgumentException("Target release cannot be blank");
        List<ModelRow> draft = CsvStore.read(draftFile);
        List<ModelRow> selected = draft.stream()
                .filter(r -> r.releaseId().equals(release) && r.selectedForRelease()).toList();
        if (selected.isEmpty()) throw new IllegalArgumentException("No selected rows for release " + release);
        // Selected rows must be an internally consistent COMPLETE release; no carry-forward.
        Validator.validate(selected);
        List<ModelRow> current = Files.exists(publishedFile) ? CsvStore.read(publishedFile) : List.of();
        if (current.stream().anyMatch(r->r.releaseId().equals(release)))
            throw new IllegalStateException("Release " + release + " already published; immutable; cannot publish twice");
        Validator.validate(current);
        List<ModelRow> combined = new ArrayList<>(current);
        combined.addAll(selected);
        // Refuse to change existing published data. In a single-process local demo, atomic move
        // avoids partial writes; this is NOT a substitute for concurrency/DB permissions.
        Path parent = publishedFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent,".program-model-published-",".tmp");
        try {
            CsvStore.write(temp,combined);
            try { Files.move(temp,publishedFile,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temp,publishedFile,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
        return selected.size();
    }
}
