# 1.20.1 side of the autotest driver

Not compiled here. These are the two files that differ for the Forge 1.20.1 reference build (`Dumps.java` and
`JeiHook.java` are shared as-is). To set up the reference (see TESTING-HANDOFF.md, "Automated side-by-side
comparison"):

1. `git worktree add ../Destroy-1201 origin/1.20.1`
2. Copy these two files plus `../java/destroytest/{Dumps,JeiHook}.java` into `../Destroy-1201/src/main/java/destroytest/`.
3. Append to `src/main/resources/META-INF/mods.toml`: a `[[mods]]` entry with `modId="destroytest"`.
4. `build.gradle`: library `implementation fg.deobf("maven.modrinth:petrolpark:${minecraft_version}-${petrolpark_library_version}")`,
   Registrate `compileOnly`, JEI forge `implementation`, and in the client run
   `property 'destroytest.script', project.findProperty('autotestScript') ?: ''` + `args '--width', '1280', '--height', '720'`.
5. `JAVA_HOME=<JDK 17> ./gradlew runClient -PautotestScript=<absolute path to a script>`; output in `run/autotest/<script>/`.
