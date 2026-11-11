# Program Model — architecture decisions

1. **Plain Java** (Java 17+) and CSV are the only runtime requirements. Job configuration is Java source code, not an opaque external configuration system.
2. **One row = one element in one release.** Both canonical CSVs share the same header and column order. Outgoing flows are delimited within one field; this limits expressiveness and is documented, not disguised as normalized.
3. **Two independent canonical datasets:** `PROGRAM_MODEL_DRAFT.csv` is replaceable working data; `PROGRAM_MODEL_PUBLISHED.csv` is logically append-only. A publication may not rewrite an existing release through the supported API.
4. **Explicit membership:** publication selects only draft rows marked for the requested release. No carry-forward is implied. Loading a prior release into draft is an explicit operator action and produces a new release candidate.
5. **Validate before publication:** ensure unique element keys, typed nodes, consistent process and role identifiers, valid local flow targets, and structural checks. A failed validation must not modify published data.
6. **Prepared relational projection:** derived process, element, flow, responsibility and evidence CSV files expose possible relational entities. A round-trip check verifies that this projection preserves all canonical fields.
7. **Not a production transaction engine:** publication uses an atomic file replacement where available; concurrent writers, malicious edits, access controls, official signoff and database constraints require Oracle implementation.

## Planned commits

1. `docs: establish Program Model boundaries and Git plan`
2. `feat: define canonical row contract and CSV serialization`
3. `feat: provide editable Java seed jobs across three domains`
4. `feat: validate draft and publish selected quarterly releases`
5. `feat: load releases into draft and project relational outputs`
6. `test: implement dependency-free unit suite and scripts`
7. `docs: add examples, verified outputs and release instructions`

## Deferred decisions

- Whether the canonical row grain can express independent many-to-many evidence, multiple roles and conditional gateway branches.
- Whether an element/variant ID denotes immutable content across releases or is release-relative.
- APEX authorization, relational Oracle DDL, and approvals; these are deliberately not implied by CSV tests.
