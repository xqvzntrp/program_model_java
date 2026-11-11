package programmodel;

import programmodel.config.*;
import programmodel.io.CsvStore;
import programmodel.model.ModelRow;
import programmodel.service.*;
import java.nio.file.*;
import java.util.List;

/** Command line interface; configuration is plain Java under programmodel.config. */
public final class ProgramModel {
    private ProgramModel() {}

    public static void main(String[] args) throws Exception {
        String command = args.length == 0 ? "help" : args[0];
        Path root = Path.of(System.getProperty("programmodel.output", "output"));
        Path draft=root.resolve("PROGRAM_MODEL_DRAFT.csv");
        Path published=root.resolve("PROGRAM_MODEL_PUBLISHED.csv");
        switch (command) {
            case "seed" -> {
                List<ModelRow> rows=SeedJob.rows();
                Validator.validate(rows);
                // Seed establishes canonical CSV files; never clears an existing published history.
                if (!Files.exists(published)) CsvStore.write(published,List.of());
                CsvStore.write(draft,rows);
                System.out.println("Seeded draft: " + rows.size() + " fictional elements; published history retained");
            }
            case "validate" -> {
                Validator.validate(CsvStore.read(draft));
                System.out.println("Draft validation passed");
            }
            case "publish" -> {
                String release=args.length>1?args[1]:PublishJob.RELEASE_ID;
                System.out.println("Published " + Publisher.publish(draft,published,release) + " rows for " + release);
            }
            case "load" -> {
                String from=args.length>1?args[1]:LoadJob.FROM_RELEASE;
                String to=args.length>2?args[2]:LoadJob.TO_RELEASE;
                System.out.println("Loaded " + ReleaseLoader.load(published,draft,from,to) +
                        " UNSELECTED rows into draft release " + to);
            }
            case "project" -> {
                String source=args.length>1?args[1]:"published";
                Path file=switch (source) {
                    case "draft" -> draft;
                    case "published" -> published;
                    default -> throw new IllegalArgumentException("project source must be draft or published");
                };
                List<ModelRow> rows=CsvStore.read(file);
                if (rows.isEmpty()) throw new IllegalArgumentException("No rows to project");
                RelationalProjection.assertLossless(rows, root.resolve("prepared"));
                System.out.println("Projected and round-trip verified " + rows.size() + " records");
            }
            case "status" -> {
                System.out.println("Draft rows: " + (Files.exists(draft)?CsvStore.read(draft).size():0));
                System.out.println("Published rows: " + (Files.exists(published)?CsvStore.read(published).size():0));
            }
            case "help" -> System.out.println("Commands: seed | validate | publish [RELEASE] | load [FROM_RELEASE] [TO_RELEASE] | project [draft|published] | status");
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
}
