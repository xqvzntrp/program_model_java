package programmodel.model;

import java.util.List;

/** One element in one explicitly identified quarterly release. */
public record ModelRow(String releaseId, String elementId, String variantId,
        String processId, String processName, String elementType, String elementSubtype,
        String elementName, String nextElementIds, String roleId, String roleName,
        String evidenceReference, String reviewStatus, boolean selectedForRelease,
        String notes) {

    public static final List<String> HEADERS = List.of(
        "release_id", "element_id", "variant_id", "process_id", "process_name",
        "element_type", "element_subtype", "element_name", "next_element_ids",
        "role_id", "role_name", "evidence_reference", "review_status",
        "selected_for_release", "notes");

    public List<String> cells() {
        return List.of(releaseId, elementId, variantId, processId, processName,
                elementType, elementSubtype, elementName, nextElementIds, roleId,
                roleName, evidenceReference, reviewStatus,
                Boolean.toString(selectedForRelease), notes);
    }

    public ModelRow forRelease(String newRelease) {
        return new ModelRow(newRelease, elementId, variantId, processId, processName,
            elementType, elementSubtype, elementName, nextElementIds, roleId, roleName,
            evidenceReference, reviewStatus, false, notes);
    }

    public static ModelRow fromCells(List<String> c) {
        if (c.size() != HEADERS.size()) throw new IllegalArgumentException("Expected " + HEADERS.size() + " columns, got " + c.size());
        if (!c.get(13).equalsIgnoreCase("true") && !c.get(13).equalsIgnoreCase("false"))
            throw new IllegalArgumentException("selected_for_release must be true or false");
        return new ModelRow(c.get(0),c.get(1),c.get(2),c.get(3),c.get(4),
                c.get(5),c.get(6),c.get(7),c.get(8),c.get(9),c.get(10),
                c.get(11),c.get(12),Boolean.parseBoolean(c.get(13)),c.get(14));
    }
}
