package programmodel.service;

import programmodel.model.ModelRow;
import java.util.*;

/** Checks relationally meaningful keys and intra-release node references. */
public final class Validator {
    private Validator() {}

    public static void validate(List<ModelRow> rows) {
        List<String> problems = new ArrayList<>();
        Map<String,ModelRow> byReleaseElement = new HashMap<>();
        Map<String,String> processNames = new HashMap<>();
        Map<String,String> roleNames = new HashMap<>();
        Map<String,Set<String>> releaseProcesses = new HashMap<>();
        for (ModelRow r: rows) {
            for (String required: List.of(r.releaseId(),r.elementId(),r.variantId(),r.processId(),
                    r.processName(),r.elementType(),r.elementSubtype(),r.elementName(),r.reviewStatus())) {
                if (required.isBlank()) problems.add("Required column is blank for element " + r.elementId());
            }
            String key = r.releaseId() + "\u0000" + r.elementId();
            if (byReleaseElement.putIfAbsent(key,r)!=null) problems.add("Duplicate (release_id, element_id): " + r.releaseId() + "/" + r.elementId());
            String prevProcess = processNames.putIfAbsent(r.processId(),r.processName());
            if (prevProcess!=null && !prevProcess.equals(r.processName())) problems.add("Inconsistent name for process " + r.processId());
            releaseProcesses.computeIfAbsent(r.releaseId(),k->new HashSet<>()).add(r.processId());
            if (r.roleId().isBlank() != r.roleName().isBlank()) problems.add("Role ID/name must both be present or absent: " + r.elementId());
            if (!r.roleId().isBlank()) {
                String roleKey=r.releaseId()+"\u0000"+r.roleId();
                String prevRole=roleNames.putIfAbsent(roleKey,r.roleName());
                if (prevRole!=null && !prevRole.equals(r.roleName())) problems.add("Inconsistent role ID/name: " + r.roleId());
            }
            if (!validSubtype(r.elementType(),r.elementSubtype())) problems.add("Invalid node type/subtype: " + r.elementId());
            if (!Set.of("PROPOSED","REVIEWED","APPROVED","REJECTED").contains(r.reviewStatus()))
                problems.add("Invalid review status: " + r.elementId());
            if (!r.elementType().equals("ACTIVITY") && !r.roleId().isBlank()) problems.add("Only activities may have roles: " + r.elementId());
        }
        for (ModelRow r: rows) {
            Set<String> targets = new HashSet<>();
            if (!r.nextElementIds().isBlank()) {
                for (String t: r.nextElementIds().split("\\|",-1)) {
                    if (t.isBlank()) { problems.add("Empty flow target: " + r.elementId()); continue; }
                    if (!targets.add(t)) problems.add("Duplicate flow target: " + r.elementId() + " -> " + t);
                    if (t.equals(r.elementId())) problems.add("Self-referencing flow: " + r.elementId());
                    ModelRow dest=byReleaseElement.get(r.releaseId()+"\u0000"+t);
                    if (dest==null) problems.add("Missing target in same release: " + r.elementId()+" -> "+t);
                    else if (!dest.processId().equals(r.processId())) problems.add("Cross-process flow: " + r.elementId()+" -> "+t);
                }
            }
            if (r.elementType().equals("EVENT") && r.elementSubtype().equals("END") && !targets.isEmpty()) problems.add("END event has outgoing flow: " + r.elementId());
            if (r.elementType().equals("GATEWAY") && targets.size()<2) problems.add("Gateway must branch to 2+ targets in this demo: " + r.elementId());
        }
        // Release-scoped process completeness: each included process needs a start and end.
        for (String rp : releaseProcesses.keySet()) {
            for (String pid : releaseProcesses.get(rp)) {
                boolean start = rows.stream().anyMatch(r -> r.releaseId().equals(rp) && r.processId().equals(pid)
                        && r.elementType().equals("EVENT") && r.elementSubtype().equals("START"));
                boolean end = rows.stream().anyMatch(r -> r.releaseId().equals(rp) && r.processId().equals(pid)
                        && r.elementType().equals("EVENT") && r.elementSubtype().equals("END"));
                if (!start || !end) problems.add("Process needs START and END: " + rp + "/" + pid);
            }
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("Validation failed:\n - " + String.join("\n - ",problems));
    }

    private static boolean validSubtype(String type, String subtype) {
        return switch (type) {
            case "ACTIVITY" -> Set.of("TASK","SUB_PROCESS").contains(subtype);
            case "EVENT" -> Set.of("START","END").contains(subtype);
            case "GATEWAY" -> Set.of("EXCLUSIVE","PARALLEL").contains(subtype);
            default -> false;
        };
    }
}
