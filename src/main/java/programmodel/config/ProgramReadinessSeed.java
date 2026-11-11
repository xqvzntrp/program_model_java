package programmodel.config;

import programmodel.model.ModelRow;
import java.util.ArrayList;
import java.util.List;
import static programmodel.config.SeedJob.row;

/** USER-EDITABLE SEED DOMAIN 3: cross-team readiness and convergence. */
public final class ProgramReadinessSeed {
    private ProgramReadinessSeed() {}
    public static List<ModelRow> rows() {
        List<ModelRow> rows = new ArrayList<>();
        for (String release : List.of("2027-Q1", "2027-Q2")) {
            boolean q2 = release.endsWith("Q2");
            rows.add(row(release,"D-START","V1","PROC-D","Program Readiness","EVENT","START","Deployment requested","D-SPLIT","","","","REVIEWED",""));
            rows.add(row(release,"D-SPLIT","V1","PROC-D","Program Readiness","GATEWAY","PARALLEL","Readiness workstreams","D-TECH|D-OPS","","","","REVIEWED","Simultaneous checks"));
            rows.add(row(release,"D-TECH",q2?"V2":"V1","PROC-D","Program Readiness","ACTIVITY","TASK","Confirm technology readiness","D-DECIDE","R-TECH","Technology Lead","Integration checklist",q2?"APPROVED":"PROPOSED","Infrastructure dependency"));
            rows.add(row(release,"D-OPS","V1","PROC-D","Program Readiness","ACTIVITY","TASK","Confirm operations readiness","D-DECIDE","R-OPS","Operations Director","Training checklist","PROPOSED","Staffing dependency"));
            rows.add(row(release,"D-DECIDE","V1","PROC-D","Program Readiness","GATEWAY","EXCLUSIVE","Go-live ready?","D-END|D-TECH","","","Decision register","REVIEWED","Not a formal parallel gateway join"));
            rows.add(row(release,"D-END","V1","PROC-D","Program Readiness","EVENT","END","Readiness assessed","","","","","REVIEWED",""));
        }
        return rows;
    }
}
