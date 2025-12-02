# Persistence Notes

- Primary keys are application-provided `String` values (UUIDs or domain identifiers). Keeping IDs as strings avoids sequence dependencies, making it straightforward to migrate the same schema to Cloud Spanner later.
- Collections such as tag IDs or custom variable IDs are stored in dedicated link tables via `@ElementCollection`, matching the array semantics described in `docs/data_model.md` without introducing extra aggregate roots. This pattern can be replicated with Spanner interleaved tables in the future.
- Audit metadata (`createdAt`, `updatedAt`, `createdBy`, `lastUpdatedBy`) stays inside each aggregate rather than relying on database triggers to preserve deterministic behavior across Oracle and Spanner.
