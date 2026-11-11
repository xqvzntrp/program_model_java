package programmodel.service;

import programmodel.io.CsvStore;
import programmodel.model.ModelRow;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** Lossless relational projection for the current 1-element/1-role/1-evidence grain. */
public final class RelationalProjection {
    private RelationalProjection() {}

    private static final List<String> ELEMENTS = List.of("release_id","element_id","variant_id","process_id","process_name",
            "element_type","element_subtype","element_name","review_status","selected_for_release","notes");
    private static final List<String> FLOWS = List.of("release_id","source_element_id","flow_position","target_element_id");
    private static final List<String> ROLES = List.of("release_id","element_id","role_id","role_name");
    private static final List<String> EVIDENCE = List.of("release_id","element_id","evidence_reference");
    private static final List<String> PROCESSES = List.of("release_id","process_id","process_name");

    public static void project(List<ModelRow> rows, Path target) throws IOException {
        Validator.validate(rows);
        List<List<String>> elements = new ArrayList<>(), flows = new ArrayList<>(), roles = new ArrayList<>(), evidence = new ArrayList<>(), processes = new ArrayList<>();
        Set<String> processKeys = new HashSet<>();
        for (ModelRow r : rows) {
            if (processKeys.add(r.releaseId() + "\u0000" + r.processId()))
                processes.add(List.of(r.releaseId(), r.processId(), r.processName()));
            elements.add(List.of(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                    r.elementType(),r.elementSubtype(),r.elementName(),r.reviewStatus(),
                    Boolean.toString(r.selectedForRelease()),r.notes()));
            if (!r.nextElementIds().isBlank()) {
                String[] targets = r.nextElementIds().split("\\|",-1);
                for (int i=0; i<targets.length; i++) flows.add(List.of(r.releaseId(),r.elementId(),Integer.toString(i+1),targets[i]));
            }
            if (!r.roleId().isBlank()) roles.add(List.of(r.releaseId(),r.elementId(),r.roleId(),r.roleName()));
            if (!r.evidenceReference().isBlank()) evidence.add(List.of(r.releaseId(),r.elementId(),r.evidenceReference()));
        }
        CsvStore.writeTable(target.resolve("processes.csv"),PROCESSES,processes);
        CsvStore.writeTable(target.resolve("elements.csv"),ELEMENTS,elements);
        CsvStore.writeTable(target.resolve("flows.csv"),FLOWS,flows);
        CsvStore.writeTable(target.resolve("responsibilities.csv"),ROLES,roles);
        CsvStore.writeTable(target.resolve("evidence.csv"),EVIDENCE,evidence);
    }

    /** Rebuilds every canonical field from freshly read projection files (not in-memory rows). */
    public static List<ModelRow> reconstruct(Path source) throws IOException {
        List<List<String>> elements = CsvStore.readTable(source.resolve("elements.csv"), ELEMENTS);
        List<List<String>> flows = CsvStore.readTable(source.resolve("flows.csv"), FLOWS);
        List<List<String>> roles = CsvStore.readTable(source.resolve("responsibilities.csv"), ROLES);
        List<List<String>> evidence = CsvStore.readTable(source.resolve("evidence.csv"), EVIDENCE);
        List<List<String>> processes = CsvStore.readTable(source.resolve("processes.csv"), PROCESSES);
        Map<String,List<String>> processMap = new HashMap<>(), roleMap = new HashMap<>(), evidenceMap = new HashMap<>();
        Map<String,TreeMap<Integer,String>> flowMap = new HashMap<>();
        for (List<String> p : processes) {
            if (processMap.putIfAbsent(key(p.get(0),p.get(1)),p)!=null) throw new IllegalArgumentException("Duplicate projected process");
        }
        for (List<String> f : flows) {
            String k=key(f.get(0),f.get(1));
            TreeMap<Integer,String> targets=flowMap.computeIfAbsent(k, ignored -> new TreeMap<>());
            if (targets.putIfAbsent(Integer.parseInt(f.get(2)),f.get(3))!=null) throw new IllegalArgumentException("Duplicate flow position");
        }
        for (List<String> r : roles) if (roleMap.putIfAbsent(key(r.get(0),r.get(1)),r)!=null)
            throw new IllegalArgumentException("Multiple roles not supported by canonical grain");
        for (List<String> e : evidence) if (evidenceMap.putIfAbsent(key(e.get(0),e.get(1)),e)!=null)
            throw new IllegalArgumentException("Multiple evidence references not supported by canonical grain");
        List<ModelRow> output = new ArrayList<>();
        for (List<String> r : elements) {
            String k=key(r.get(0),r.get(1));
            List<String> process=processMap.get(key(r.get(0),r.get(3)));
            if (process==null || !process.get(2).equals(r.get(4))) throw new IllegalArgumentException("Missing or inconsistent projected process: " + k);
            List<String> role=roleMap.remove(k), ev=evidenceMap.remove(k);
            TreeMap<Integer,String> targetMap=flowMap.remove(k);
            String next="";
            if (targetMap!=null) {
                List<String> targets=new ArrayList<>();
                int expected=1;
                for (Map.Entry<Integer,String> e : targetMap.entrySet()) {
                    if (e.getKey()!=expected++) throw new IllegalArgumentException("Noncontiguous flow positions: " + k);
                    targets.add(e.getValue());
                }
                next=String.join("|",targets);
            }
            output.add(new ModelRow(r.get(0),r.get(1),r.get(2),r.get(3),r.get(4),r.get(5),r.get(6),r.get(7),
                    next,role==null?"":role.get(2),role==null?"":role.get(3),ev==null?"":ev.get(2),
                    r.get(8),Boolean.parseBoolean(r.get(9)),r.get(10)));
        }
        if (!flowMap.isEmpty() || !roleMap.isEmpty() || !evidenceMap.isEmpty())
            throw new IllegalArgumentException("Orphan projection relationships");
        Validator.validate(output);
        return output;
    }

    public static void assertLossless(List<ModelRow> originals, Path directory) throws IOException {
        project(originals,directory);
        List<ModelRow> roundtrip = reconstruct(directory);
        if (!originals.equals(roundtrip)) throw new IllegalStateException("Canonical-to-relational round trip changed records");
    }

    private static String key(String release, String id) { return release + "\u0000" + id; }
}
