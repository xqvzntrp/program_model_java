package programmodel.config;

import programmodel.model.ModelRow;
import java.util.ArrayList;
import java.util.List;

/** USER-EDITABLE JOB. Add or remove seed domains here. All data is fictional. */
public final class SeedJob {
    private SeedJob() {}
    public static List<ModelRow> rows() {
        List<ModelRow> rows = new ArrayList<>();
        rows.addAll(PartnerOnboardingSeed.rows());
        rows.addAll(PolicyGovernanceSeed.rows());
        rows.addAll(ProgramReadinessSeed.rows());
        return List.copyOf(rows);
    }

    /** Shared user-facing row factory; exactly one element per release per row. */
    public static ModelRow row(String release, String element, String variant,
            String processId, String processName, String type, String subtype,
            String name, String next, String roleId, String roleName,
            String evidence, String review, String notes) {
        return new ModelRow(release, element, variant, processId, processName,
                type, subtype, name, next, roleId, roleName, evidence, review,
                true, notes);
    }
}
