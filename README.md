# Program Model (plain Java)

This repository is built one reviewed Git commit at a time. See `ARCHITECTURE.md` for design decisions and limits.

The canonical files are `PROGRAM_MODEL_DRAFT.csv` and `PROGRAM_MODEL_PUBLISHED.csv` under `output/` after running the jobs. **The editable Java job classes under `src/main/java/programmodel/config` determine the generated records and which release is published.**

Requirements: JDK 17+, no external dependencies. Full commands and tests are added in later commits.
