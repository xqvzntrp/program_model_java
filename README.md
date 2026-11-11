# Program Model — plain Java / CSV proof of concept

A **standalone, dependency-free Java 17 project**, rebuilt from an empty Git repository in deliberate, reviewable commits. The Git repository and its commit history are included in the downloadable ZIP.

## The two canonical files

Running the jobs generates `output/PROGRAM_MODEL_DRAFT.csv` and `output/PROGRAM_MODEL_PUBLISHED.csv`. Both use the **same 15-column contract**:

`release_id, element_id, variant_id, process_id, process_name, element_type, element_subtype, element_name, next_element_ids, role_id, role_name, evidence_reference, review_status, selected_for_release, notes`

- **Draft** is editable and generated from user-editable Java job definitions. Running `seed` **overwrites the draft**, so save any manual CSV edits before reseeding.
- **Published** is logically append-only: the supported `publish` command rejects an existing release identifier and preserves existing historical records. A `seed` run **never erases** published data.
- A release includes only explicitly selected rows for that release; **nothing is automatically inherited**.
- `load` explicitly copies a previously published release into a *different draft release*, leaving its rows **unselected** until you deliberately include them. It **replaces** the current draft CSV; preserve the draft first if needed.

## User-facing configuration: edit these Java files

| Java class | What you change |
| --- | --- |
| `src/main/java/programmodel/config/SeedJob.java` | Which seed domains participate in generation |
| `src/main/java/programmodel/config/PartnerOnboardingSeed.java` | Domain 1: activities, events, gateways, roles, evidence |
| `src/main/java/programmodel/config/PolicyGovernanceSeed.java` | Domain 2: policy interpretation and coordination |
| `src/main/java/programmodel/config/ProgramReadinessSeed.java` | Domain 3: technology and operations readiness |
| `src/main/java/programmodel/config/PublishJob.java` | Default target quarterly release (`RELEASE_ID`) |
| `src/main/java/programmodel/config/LoadJob.java` | Default source/target releases for explicit draft loading |

All seed records are **fictional** and intended for modeling experiments only. You can modify a seed Java class, run `seed`, inspect the draft CSV, and then publish. Or edit the generated draft CSV directly after seeding without regenerating it.

## Run on macOS / Linux (JDK 17 or newer)

From the project directory:

```sh
sh scripts/test.sh          # compile main + separate unit test module, run all tests
sh scripts/run.sh seed      # create draft and initial empty published CSV (unless already published)
sh scripts/run.sh validate  # validate entire draft
sh scripts/run.sh publish 2027-Q1
sh scripts/run.sh publish 2027-Q2
sh scripts/run.sh project published   # 5 relational CSV projections and full round-trip test
sh scripts/run.sh status
```

For a deliberate reuse of a prior release:

```sh
sh scripts/run.sh load 2027-Q1 2027-Q3
```

This copies Q1 into a new Q3 **draft** with `selected_for_release=false`, and does not publish Q3. Edit/confirm/select the rows in the draft CSV before publishing Q3; you may also update the Java seed configurations for later generation.

## Run on Windows PowerShell (JDK 17 or newer)

From the project directory:

```powershell
& .\scripts\test.ps1
& .\scripts\run.ps1 seed
& .\scripts\run.ps1 publish 2027-Q1
& .\scripts\run.ps1 publish 2027-Q2
& .\scripts\run.ps1 project published
```

If PowerShell blocks local scripts, consult your approved IT policy; you can compile/run with `javac` and `java` directly instead. No Maven, Gradle, Spring, JUnit, Node.js, or third-party components.

## Generated examples and prepared relational representation

`examples/` contains **real output of the Java jobs**: a 36-row draft, Q1+Q2 published history (36 rows total, 18 per release), and prepared files:

- `processes.csv` — `(release_id, process_id)` identifies a process in a release.
- `elements.csv` — `(release_id, element_id)` identifies an element in a release.
- `flows.csv` — `(release_id, source_element_id, flow_position)` identifies an outgoing flow; target must exist in the same release and process.
- `responsibilities.csv` — `(release_id, element_id)` maps the single role assignment currently supported for an element.
- `evidence.csv` — `(release_id, element_id)` maps the single evidence reference currently supported for an element.

`RelationalProjection.assertLossless` **reads those prepared CSVs back from disk**, rebuilds every canonical field, and compares the reconstructed rows with the original. This is a file-level relational proof, **not** actual Oracle primary/foreign-key constraint enforcement.

## Git milestones

Use `git log --oneline --reverse` to inspect the sequence. The ZIP includes `.git` and can be used directly as a local Git repo. `ARCHITECTURE.md` explains the choices behind each step.

## Boundaries / important cautions

- One canonical row per `(release_id, element_id)`. One role and one evidence reference per row. Multivalued outgoing flows use a `|` delimiter. Independent multiple roles, evidence relationships, and branch conditions remain modeling questions, not solved features.
- `variant_id` is currently a descriptive version label; immutable identity across releases is not enforced.
- `publish` validates selected release rows and refuses a duplicate release. It uses a temporary file and atomic replacement when supported; this is **logical append-only behavior**, not a physical append-only database guarantee. File permissions, tamper resistance, concurrent publishers, audit identities, formal approvals and crash-safe coordination require a real database/application layer.
- The prepared CSV files are a demonstration of relational shapes and round-trip equivalence, not installed Oracle tables. No APEX UI is included.
- These example releases are `2027-Q1` and `2027-Q2`, chosen for fictitious testing, not official organizational releases.

## Next architectural acceptance criteria

Prove the grain against multiple concurrent roles and evidence sources; formalize stable variant identity; implement Oracle PK/FK DDL plus round-trip views; then introduce APEX structured forms and governed release approval.
