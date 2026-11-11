# Verified build and test results

Java version used for verification: OpenJDK 21, compiling with `javac --release 17`.

- 16 / 16 dependency-free tests passed.
- `seed`: generated 36 draft rows (three domains × two quarters × six elements).
- `publish 2027-Q1`: 18 selected rows published.
- `publish 2027-Q2`: 18 additional selected rows published.
- `project published`: reconstructed all 36 rows from five relational CSV projections and compared all canonical fields.
- Verified rejection: duplicate quarterly publication, incomplete selection, missing flow targets, invalid node subtype, orphan relational projections.

See `src/test/java/programmodel/tests/AllTests.java` for the executable assertions.
