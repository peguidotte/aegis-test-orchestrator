# Persistence Notes

- Collections such as tag IDs or custom variable IDs are stored in dedicated link tables via `@ElementCollection`, matching the array semantics described in `docs/data_model.md` without introducing extra aggregate roots. This pattern can be replicated with Spanner interleaved tables in the future.
- Audit metadata (`createdAt`, `updatedAt`, `createdBy`, `lastUpdatedBy`) agora fica centralizada em `AuditableEntity`, garantindo propagação automática via `@MappedSuperclass` e reduzindo duplicação sem depender de triggers do banco.
