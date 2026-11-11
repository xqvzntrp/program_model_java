package programmodel.config;

import programmodel.model.ModelRow;
import java.util.List;
import static programmodel.config.SeedJob.row;

/** USER-EDITABLE SEED DOMAIN 1: ordinary process flow and a changed owner. */
public final class PartnerOnboardingSeed {
    private PartnerOnboardingSeed() {}
    public static List<ModelRow> rows() {
        return List.of(
            row("2027-Q1","P-START","V1","PROC-P","Partner Onboarding","EVENT","START","Application received","P-REVIEW","","","","REVIEWED","Intake begins"),
            row("2027-Q1","P-REVIEW","V1","PROC-P","Partner Onboarding","ACTIVITY","TASK","Review application","P-DECIDE","R-REG","Regional Manager","Draft procedure","PROPOSED","Owner not confirmed"),
            row("2027-Q1","P-DECIDE","V1","PROC-P","Partner Onboarding","GATEWAY","EXCLUSIVE","Application complete?","P-APPROVE|P-REQUEST","","","","REVIEWED","Branch labels not yet modeled"),
            row("2027-Q1","P-APPROVE","V1","PROC-P","Partner Onboarding","ACTIVITY","TASK","Approve partner","P-END","","","","PROPOSED","Ownership unresolved"),
            row("2027-Q1","P-REQUEST","V1","PROC-P","Partner Onboarding","ACTIVITY","TASK","Request clarification","P-REVIEW","R-REG","Regional Manager","Draft procedure","PROPOSED","Loop back"),
            row("2027-Q1","P-END","V1","PROC-P","Partner Onboarding","EVENT","END","Onboarding ends","","","","","REVIEWED",""),
            row("2027-Q2","P-START","V1","PROC-P","Partner Onboarding","EVENT","START","Application received","P-REVIEW","","","","REVIEWED","Intake begins"),
            row("2027-Q2","P-REVIEW","V2","PROC-P","Partner Onboarding","ACTIVITY","TASK","Review application","P-DECIDE","R-OPS","Operations Director","Revised procedure","APPROVED","Owner updated"),
            row("2027-Q2","P-DECIDE","V1","PROC-P","Partner Onboarding","GATEWAY","EXCLUSIVE","Application complete?","P-APPROVE|P-REQUEST","","","","REVIEWED","Branch labels not yet modeled"),
            row("2027-Q2","P-APPROVE","V2","PROC-P","Partner Onboarding","ACTIVITY","TASK","Approve partner","P-END","R-COMP","Compliance Officer","Approval matrix","APPROVED","Ownership confirmed"),
            row("2027-Q2","P-REQUEST","V1","PROC-P","Partner Onboarding","ACTIVITY","TASK","Request clarification","P-REVIEW","R-REG","Regional Manager","Draft procedure","PROPOSED","Loop back"),
            row("2027-Q2","P-END","V1","PROC-P","Partner Onboarding","EVENT","END","Onboarding ends","","","","","REVIEWED","")
        );
    }
}
