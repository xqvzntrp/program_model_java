package programmodel.config;

import programmodel.model.ModelRow;
import java.util.ArrayList;
import java.util.List;
import static programmodel.config.SeedJob.row;

/** USER-EDITABLE SEED DOMAIN 2: policy-to-program coordination. */
public final class PolicyGovernanceSeed {
    private PolicyGovernanceSeed() {}
    public static List<ModelRow> rows() {
        List<ModelRow> rows = new ArrayList<>();
        for (String release : List.of("2027-Q1", "2027-Q2")) {
            boolean q2 = release.endsWith("Q2");
            rows.add(row(release,"G-START","V1","PROC-G","Policy Governance","EVENT","START","Policy issued","G-INTERPRET","","","Policy register","REVIEWED","External authority originates policy"));
            rows.add(row(release,"G-INTERPRET",q2?"V2":"V1","PROC-G","Policy Governance","ACTIVITY","TASK","Interpret policy for program","G-DECIDE","R-PMO","Program Management","Policy assessment",q2?"APPROVED":"PROPOSED","Interpretation is not itself policy"));
            rows.add(row(release,"G-DECIDE","V1","PROC-G","Policy Governance","GATEWAY","EXCLUSIVE","Requirements accepted?","G-COORD|G-INTERPRET","","","","REVIEWED","Return for revision if not accepted"));
            rows.add(row(release,"G-COORD","V1","PROC-G","Policy Governance","ACTIVITY","TASK","Coordinate implementation owners","G-VERIFY","R-PMO","Program Management","Requirements register","REVIEWED","Not operating the controls"));
            rows.add(row(release,"G-VERIFY","V1","PROC-G","Policy Governance","ACTIVITY","TASK","Check reported readiness","G-END","R-GOV","Governance Reviewer","Owner attestations","PROPOSED","Evidence is reported rather than directly observed"));
            rows.add(row(release,"G-END","V1","PROC-G","Policy Governance","EVENT","END","Assessment recorded","","","","","REVIEWED",""));
        }
        return rows;
    }
}
