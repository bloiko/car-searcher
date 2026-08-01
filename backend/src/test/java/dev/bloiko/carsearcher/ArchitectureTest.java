package dev.bloiko.carsearcher;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {

    @Test
    void packagesShouldBeFreeOfCycles() {
        // allowEmptyShould: no sub-packages exist yet (bootstrap-only scaffold).
        // Remove once task #1 in docs/semantic-car-search/tasks.md adds real packages.
        ArchRule rule = slices().matching("dev.bloiko.carsearcher.(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true);

        rule.check(new ClassFileImporter().importPackages("dev.bloiko.carsearcher"));
    }
}
