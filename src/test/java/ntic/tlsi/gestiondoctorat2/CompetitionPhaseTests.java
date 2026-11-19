package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionPhase;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CompetitionPhaseTests {
    @Test
    void newOrHistoricalCompetitionDefaultsToRegistration() {
        InfoConcour competition = new InfoConcour();
        assertEquals(CompetitionPhase.REGISTRATION, competition.effectivePhase());
        competition.advanceTo(CompetitionPhase.PREPARATION);
        assertEquals(CompetitionPhase.PREPARATION, competition.effectivePhase());
    }

    @Test
    void cannotMoveBackwards() {
        InfoConcour competition = new InfoConcour();
        competition.advanceTo(CompetitionPhase.GRADING);
        assertThrows(IllegalStateException.class,
                () -> competition.advanceTo(CompetitionPhase.REGISTRATION));
        assertEquals(CompetitionPhase.GRADING, competition.effectivePhase());
        competition.advanceTo(CompetitionPhase.GRADING);
        assertEquals(CompetitionPhase.GRADING, competition.effectivePhase());
    }

    @Test
    void historicalPublicationTimestampOverridesNullPhase() {
        InfoConcour competition = new InfoConcour();
        competition.setResultsPublishedAt(Instant.now());
        assertEquals(CompetitionPhase.PUBLISHED, competition.effectivePhase());
        assertThrows(IllegalStateException.class,
                () -> competition.advanceTo(CompetitionPhase.PREPARATION));
    }
}
