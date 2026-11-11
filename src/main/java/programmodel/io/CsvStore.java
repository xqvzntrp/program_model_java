package programmodel.io;

import programmodel.model.ModelRow;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** RFC4180-style CSV with quoted newlines, quotes and commas; no third-party dependencies. */
public final class CsvStore {
    private CsvStore() {}

    public static void write(Path target, List<ModelRow> rows) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append(String.join(",", ModelRow.HEADERS)).append("\r\n");
        for (ModelRow row : rows) {
            StringJoiner join = new StringJoiner(",");
            for (String cell : row.cells()) join.add(escape(cell));
            out.append(join).append("\r\n");
        }
        Files.createDirectories(target.toAbsolutePath().getParent());
        Files.writeString(target, out.toString(), StandardCharsets.UTF_8);
    }

    public static List<ModelRow> read(Path source) throws IOException {
        String raw = Files.readString(source, StandardCharsets.UTF_8);
        List<List<String>> records = parse(raw);
        if (records.isEmpty() || !records.get(0).equals(ModelRow.HEADERS))
            throw new IllegalArgumentException("CSV header does not match canonical Program Model specification: " + source);
        List<ModelRow> rows = new ArrayList<>();
        for (int i=1; i<records.size(); i++) {
            try { rows.add(ModelRow.fromCells(records.get(i))); }
            catch (RuntimeException e) { throw new IllegalArgumentException("CSV row " + (i+1) + ": " + e.getMessage(), e); }
        }
        return rows;
    }

    private static String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    static List<List<String>> parse(String raw) {
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false, closedQuote = false, started = false;
        for (int i=0; i<raw.length(); i++) {
            char c = raw.charAt(i);
            if (quoted) {
                if (c=='\"') {
                    if (i+1<raw.length() && raw.charAt(i+1)=='\"') { field.append('\"'); i++; }
                    else { quoted=false; closedQuote=true; }
                } else field.append(c);
            } else if (c=='\"') {
                if (field.length()>0 || closedQuote) throw new IllegalArgumentException("Unexpected quote in CSV");
                quoted=true; started=true;
            } else if (c==',') {
                fields.add(field.toString()); field.setLength(0); closedQuote=false; started=true;
            } else if (c=='\r' || c=='\n') {
                if (c=='\r' && i+1<raw.length() && raw.charAt(i+1)=='\n') i++;
                if (started || field.length()>0 || !fields.isEmpty()) {
                    fields.add(field.toString()); records.add(fields);
                    fields=new ArrayList<>(); field.setLength(0); started=false; closedQuote=false;
                }
            } else {
                if (closedQuote) throw new IllegalArgumentException("Characters after closing CSV quote");
                field.append(c); started=true;
            }
        }
        if (quoted) throw new IllegalArgumentException("Unclosed CSV quote");
        if (started || field.length()>0 || !fields.isEmpty()) {
            fields.add(field.toString()); records.add(fields);
        }
        return records;
    }
}
