package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.core.model.Lattice;
import de.uni.passau.core.model.Lattice.CellType;
import de.uni.passau.core.model.Lattices;

public class ComputeLatticesTest {

    // TODO use the sets from the paper and test they generate the following lattices

    private Lattices createInitialMockLattices() {
        return new Lattices(List.of(
            new Lattice(
                "tconst",
                new String[] { "primaryTitle", "startYear", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.INVALID_TEMP, CellType.INVALID_TEMP, CellType.INVALID_TEMP, CellType.GENUINE_TEMP, CellType.GENUINE_TEMP, CellType.GENUINE_TEMP },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "primaryTitle",
                new String[] { "tconst", "startYear", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_TEMP, CellType.GENUINE_TEMP, CellType.INVALID_TEMP, CellType.GENUINE_TEMP },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "startYear",
                new String[] { "tconst", "primaryTitle", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_TEMP, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_TEMP, CellType.INVALID_TEMP, CellType.GENUINE_TEMP },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "runtimeMinutes",
                new String[] { "tconst", "primaryTitle", "startYear", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_TEMP, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_TEMP, CellType.INVALID_TEMP, CellType.GENUINE_TEMP },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "genres",
                new String[] { "tconst", "primaryTitle", "startYear", "runtimeMinutes" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_TEMP, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_TEMP, CellType.INVALID_TEMP, CellType.GENUINE_TEMP },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            )
        ));
    }

    private Lattices createFinalMockLattices() {
        return new Lattices(List.of(
            new Lattice(
                "tconst",
                new String[] { "primaryTitle", "startYear", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.FAKE_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.FAKE_FINAL }
                }
            ),
            new Lattice(
                "primaryTitle",
                new String[] { "tconst", "startYear", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_FINAL, CellType.FAKE_DERIVED, CellType.INVALID_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.FAKE_FINAL },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "startYear",
                new String[] { "tconst", "primaryTitle", "runtimeMinutes", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_FINAL, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.FAKE_FINAL },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "runtimeMinutes",
                new String[] { "tconst", "primaryTitle", "startYear", "genres" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_FINAL, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.FAKE_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.FAKE_FINAL },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            ),
            new Lattice(
                "genres",
                new String[] { "tconst", "primaryTitle", "startYear", "runtimeMinutes" },
                new CellType[][] {
                    new CellType[] { CellType.GENUINE_FINAL, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED, CellType.INVALID_DERIVED },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.INVALID_FINAL, CellType.INVALID_FINAL, CellType.FAKE_FINAL },
                    new CellType[] { CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_DERIVED, CellType.GENUINE_FINAL },
                    new CellType[] { CellType.GENUINE_DERIVED }
                }
            )
        ));
    }

}
