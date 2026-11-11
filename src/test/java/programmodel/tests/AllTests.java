package programmodel.tests;

import programmodel.config.SeedJob;
import programmodel.io.CsvStore;
import programmodel.model.ModelRow;
import programmodel.service.*;
import java.nio.file.*;
import java.util.*;

/** Dependency-free unit tests. Throws on failure, exits nonzero. */
public final class AllTests {
    private AllTests() {}
    private static int passed;

    public static void main(String[] ignored) throws Exception {
        test("three seed domains across two releases", AllTests::seedCoverage);
        test("same canonical CSV contract", AllTests::canonicalCsv);
        test("quoted punctuation and multiline CSV", AllTests::quotedCsv);
        test("reject duplicate element key", AllTests::duplicateKey);
        test("reject missing flow target", AllTests::missingTarget);
        test("reject cross-process edge", AllTests::crossProcess);
        test("reject invalid node subtype", AllTests::invalidSubtype);
        test("reject role name inconsistency", AllTests::inconsistentRole);
        test("two independent quarterly releases", AllTests::publishBoth);
        test("reject duplicate publication without mutation", AllTests::immutableRelease);
        test("reject incomplete selection without mutation", AllTests::incompleteSelection);
        test("publish only selected domains", AllTests::selection);
        test("explicit load resets publication selection", AllTests::loadDraft);
        test("reject load into a published release", AllTests::loadPublished);
        test("full relational round trip", AllTests::roundTrip);
        test("reject orphan relational projection", AllTests::orphanProjection);
        System.out.println("ALL TESTS PASSED: " + passed);
    }

    private static void seedCoverage() {
        List<ModelRow> rows=SeedJob.rows();
        eq(36,rows.size());
        Validator.validate(rows);
        eq(3L,rows.stream().map(ModelRow::processId).distinct().count());
        eq(2L,rows.stream().map(ModelRow::releaseId).distinct().count());
        eq(Set.of("ACTIVITY","EVENT","GATEWAY"),new HashSet<>(rows.stream().map(ModelRow::elementType).toList()));
    }

    private static void canonicalCsv() throws Exception {
        Path folder=temp(); Path draft=folder.resolve("PROGRAM_MODEL_DRAFT.csv"),pub=folder.resolve("PROGRAM_MODEL_PUBLISHED.csv");
        CsvStore.write(draft,SeedJob.rows()); CsvStore.write(pub,List.of());
        eq(Files.readString(draft).split("\\R",2)[0],Files.readString(pub).split("\\R",2)[0]);
        eq(SeedJob.rows(),CsvStore.read(draft));
        eq(0,CsvStore.read(pub).size());
    }

    private static void quotedCsv() throws Exception {
        Path path=temp().resolve("special.csv");
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow r=rows.get(0);
        rows.set(0,new ModelRow(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                r.elementType(),r.elementSubtype(),"Start, \"review\"\nnext line",r.nextElementIds(),
                r.roleId(),r.roleName(),r.evidenceReference(),r.reviewStatus(),r.selectedForRelease(),r.notes()));
        CsvStore.write(path,rows); eq(rows,CsvStore.read(path));
    }

    private static void duplicateKey() {
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());rows.add(rows.get(0));
        throwsError(()->Validator.validate(rows),"Duplicate");
    }

    private static void missingTarget() {
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow r=rows.get(0);rows.set(0,withNext(r,"UNKNOWN"));
        throwsError(()->Validator.validate(rows),"Missing target");
    }

    private static void crossProcess() {
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow r=rows.get(0);rows.set(0,withNext(r,"G-START"));
        throwsError(()->Validator.validate(rows),"Cross-process");
    }

    private static void invalidSubtype() {
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow r=rows.get(0);
        rows.set(0,new ModelRow(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                "EVENT","MIDDLE",r.elementName(),r.nextElementIds(),r.roleId(),r.roleName(),
                r.evidenceReference(),r.reviewStatus(),r.selectedForRelease(),r.notes()));
        throwsError(()->Validator.validate(rows),"Invalid node");
    }

    private static void inconsistentRole() {
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow r=rows.stream().filter(x->x.releaseId().equals("2027-Q1") && x.elementId().equals("P-REQUEST")).findFirst().orElseThrow();
        int i=rows.indexOf(r);
        rows.set(i,new ModelRow(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                r.elementType(),r.elementSubtype(),r.elementName(),r.nextElementIds(),r.roleId(),"Different Name",
                r.evidenceReference(),r.reviewStatus(),r.selectedForRelease(),r.notes()));
        throwsError(()->Validator.validate(rows),"Inconsistent role");
    }

    private static void publishBoth() throws Exception {
        Path dir=temp(), draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        CsvStore.write(draft,SeedJob.rows());
        eq(18,Publisher.publish(draft,pub,"2027-Q1"));
        eq(18,Publisher.publish(draft,pub,"2027-Q2"));
        eq(36,CsvStore.read(pub).size());
        eq(18L,CsvStore.read(pub).stream().filter(x->x.releaseId().equals("2027-Q1")).count());
    }

    private static void immutableRelease() throws Exception {
        Path dir=temp(), draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        CsvStore.write(draft,SeedJob.rows());Publisher.publish(draft,pub,"2027-Q1");
        byte[] before=Files.readAllBytes(pub);
        throwsError(()->Publisher.publish(draft,pub,"2027-Q1"),"already published");
        bytesEq(before,Files.readAllBytes(pub));
    }

    private static void incompleteSelection() throws Exception {
        Path dir=temp(), draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        List<ModelRow> rows=new ArrayList<>(SeedJob.rows());
        ModelRow victim=rows.stream().filter(r->r.releaseId().equals("2027-Q1") && r.elementId().equals("P-REVIEW")).findFirst().orElseThrow();
        rows.set(rows.indexOf(victim),withSelected(victim,false));
        CsvStore.write(draft,rows);
        throwsError(()->Publisher.publish(draft,pub,"2027-Q1"),"Missing target");
        isTrue(!Files.exists(pub),"Failure must not create published file");
    }

    private static void selection() throws Exception {
        Path dir=temp(),draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        List<ModelRow> rows=new ArrayList<>();
        for (ModelRow r:SeedJob.rows()) rows.add(r.releaseId().equals("2027-Q1") && r.processId().equals("PROC-G") ? withSelected(r,false):r);
        CsvStore.write(draft,rows);
        eq(12,Publisher.publish(draft,pub,"2027-Q1"));
        isTrue(CsvStore.read(pub).stream().noneMatch(r->r.processId().equals("PROC-G")),"Unselected domain must not publish");
    }

    private static void loadDraft() throws Exception {
        Path dir=temp(),draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        CsvStore.write(draft,SeedJob.rows());Publisher.publish(draft,pub,"2027-Q1");
        eq(18,ReleaseLoader.load(pub,draft,"2027-Q1","2027-Q3"));
        List<ModelRow> loaded=CsvStore.read(draft);
        isTrue(loaded.stream().allMatch(r->r.releaseId().equals("2027-Q3") && !r.selectedForRelease()),"Loaded rows must require explicit selection");
        throwsError(()->Publisher.publish(draft,pub,"2027-Q3"),"No selected rows");
    }

    private static void loadPublished() throws Exception {
        Path dir=temp(),draft=dir.resolve("draft.csv"),pub=dir.resolve("published.csv");
        CsvStore.write(draft,SeedJob.rows());Publisher.publish(draft,pub,"2027-Q1");
        throwsError(()->ReleaseLoader.load(pub,draft,"2027-Q1","2027-Q1"),"distinct");
    }

    private static void roundTrip() throws Exception {
        List<ModelRow> rows=SeedJob.rows();Path dir=temp();
        RelationalProjection.assertLossless(rows,dir);
        eq(rows,RelationalProjection.reconstruct(dir));
        eq(3*2,CsvStore.readTable(dir.resolve("processes.csv"),List.of("release_id","process_id","process_name")).size());
    }

    private static void orphanProjection() throws Exception {
        Path dir=temp();RelationalProjection.project(SeedJob.rows(),dir);
        Path roles=dir.resolve("responsibilities.csv");
        Files.writeString(roles,Files.readString(roles)+"2027-Q1,NONEXISTENT,R-X,Orphan Role\r\n");
        throwsError(()->RelationalProjection.reconstruct(dir),"Orphan");
    }

    private static ModelRow withNext(ModelRow r,String next) {
        return new ModelRow(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                r.elementType(),r.elementSubtype(),r.elementName(),next,r.roleId(),r.roleName(),
                r.evidenceReference(),r.reviewStatus(),r.selectedForRelease(),r.notes());
    }
    private static ModelRow withSelected(ModelRow r,boolean selected) {
        return new ModelRow(r.releaseId(),r.elementId(),r.variantId(),r.processId(),r.processName(),
                r.elementType(),r.elementSubtype(),r.elementName(),r.nextElementIds(),r.roleId(),r.roleName(),
                r.evidenceReference(),r.reviewStatus(),selected,r.notes());
    }
    private static Path temp() throws Exception { return Files.createTempDirectory("program-model-unit-"); }
    private interface TestAction { void run() throws Exception; }
    private static void test(String name,TestAction action) throws Exception {
        try {action.run();passed++;System.out.println("PASS " + name);}
        catch(Exception|AssertionError e) {System.err.println("FAIL " + name);throw e;}
    }
    private static void throwsError(TestAction action,String contains) {
        try {action.run();throw new AssertionError("Expected an error containing: " + contains);}
        catch(AssertionError e) {throw e;}
        catch(Exception e) {isTrue(e.getMessage()!=null && e.getMessage().contains(contains),"Expected ["+contains+"] got "+e);}
    }
    private static void eq(Object expected,Object actual) {if(!Objects.equals(expected,actual))throw new AssertionError("Expected "+expected+", got "+actual);}
    private static void bytesEq(byte[] a,byte[] b) {if(!Arrays.equals(a,b))throw new AssertionError("Published bytes changed");}
    private static void isTrue(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
}
