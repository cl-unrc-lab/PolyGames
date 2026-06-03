# PolyGames

PolyGames is an extension of [PRISM-games](https://www.prismmodelchecker.org/games/) that adds support for **polyhedral probability distributions** in stochastic game models.

Instead of assigning fixed probabilities to state transitions, PolyGames lets you define a system of linear constraints over those probabilities. The solution space of the constraints forms a polytope — each vertex is a valid probability distribution, and PRISM-games reasons over all of them. This is useful when exact probabilities are unknown but relationships between them can be specified.

The tool also extends the PRISM language with **array support**, allowing more compact and readable model descriptions.

## Requirements

The easiest way to build and run PolyGames is via Docker. No local dependencies are required beyond Docker itself.

For a native build, you will need:

- Java JDK 17+
- GCC / G++
- GNU Make
- JavaCC
- GNU GMP (`libgmp-dev`)
- GNU M4 and Automake
- Parma Polyhedra Library (PPL) 1.2 with Java interface

## Quick start with Docker

```bash
# 1. Build the image (takes several minutes on first run — PPL is compiled from source)
docker compose build

# 2. Verify the tool runs
docker compose run --rm polygames

# 3. Run a bundled example
docker compose run --rm coins

# 4. Run your own model
docker compose run --rm polygames /models/mymodel.prism /models/mymodel.props
```

The `./models` directory on your host is mounted at `/models` inside the container.

Docker images are pinned to `linux/amd64`. On Apple Silicon the build runs via Rosetta 2 emulation — slower to build, but fully functional.

## Language extension: uncertain updates

PolyGames introduces a new command syntax using `-U->` to declare uncertain transitions:

```prism
[] s = 0 -> p1: (s'=1) + p2: (s'=2) + p3: (s'=3)
  {
    p1 <= 0.6,
    p1 >= p2 + p3,
    p2  = 0.5 * p3
  };
```

The constraints in `{ }` define a polytope over `p1`, `p2`, `p3`. PolyGames finds all vertices and generates one standard PRISM command per vertex before model checking.

## Language extension: arrays

```prism
const double p[3] = { 0.25, 0.25, 0.50 };

module main
  int v[3] : [1..3] = { 1, 2, 3 };
  [] true -> (v'[i] = 2);
endmodule
```

Arrays are desugared into flat variables and guarded commands before model checking.

## Repository layout

```
polygames/      Main Java project (modified PRISM-games)
ppl/            Parma Polyhedra Library source (with Java interface)
cudd/           CUDD BDD library (PRISM dependency)
prism-games/    Original upstream PRISM-games (reference only)
```

## Running examples

Several example models are included in `polygames/polytests/`:

| Model | Command |
|-------|---------|
| Coins (known results) | `docker compose run --rm coins` |
| Investor case study | `docker compose run --rm investor` |
| Roborta vs Rigoborto | `docker compose run --rm roborta` |

## Native build

```bash
cd polygames
make
bin/polygames model.prism model.props
```

The Makefile auto-detects your OS, Java installation, and PPL location via `ppl-config`.
