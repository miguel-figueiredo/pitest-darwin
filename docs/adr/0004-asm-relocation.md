# Mutator code imports org.pitest.reloc.asm, not org.objectweb.asm

The published `org.pitest:pitest` artifact shades ASM and **relocates** it to `org.pitest.reloc.asm`,
while `org.pitest:pitest-entry` shades ASM **un-relocated** under `org.objectweb.asm`. Everything PIT
hands a mutator — the `MethodVisitor` passed to `MethodMutatorFactory.create` — is therefore a
relocated type, even though PIT's own source imports the plain package (relocation happens at shade
time, so the source is misleading).

Getting this wrong fails at runtime with `NoSuchMethodError`, not at compile time, which is why it is
worth writing down. pitest-descartes imports both packages in different files for exactly this reason.

## Consequences

- All darwin mutator code imports `org.pitest.reloc.asm.*`, including `Opcodes`, `Type` and
  `MethodVisitor`, and uses `org.pitest.bytecode.ASMVersion.ASM_VERSION` for the visitor's API level.
- Reading PIT's source as a reference means mentally rewriting its ASM imports.
- Darwin does not depend on `pitest-entry`, which keeps the un-relocated ASM off the classpath
  entirely. Verifying mutator registration therefore goes through `Mutator.fromStrings` (in `pitest`)
  rather than `PluginServices` (in `pitest-entry`).
