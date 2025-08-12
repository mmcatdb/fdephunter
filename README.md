# FDepHunter

> This repository hosts the implementation of **FDepHunter**, a data profiling tool published at VLDB 2025.
>
> Pavel Koupil, Jáchym Bártík, Stefan Klessinger, André Conrad, and Stefanie Scherzinger. "FDepHunter: Harnessing Negative Examples to Expose Fakes and Reveal Ghosts." *PVLDB*, 18(12): 5227-5230, 2025. [https://doi.org/10.14778/3750601.3750612](https://doi.org/10.14778/3750601.3750612)

## Table of Contents

- [About FDepHunter](#about-fdephunter)
- [Setup](#setup)
- [Getting Started](#getting-started)
- [How to Cite](#how-to-cite)

## About FDepHunter

FDepHunter is a data profiling tool for identifying genuine functional dependencies (FDs) – that is, FDs that hold across all realistic dataset instances – in relational data.

Starting from an initial set of FDs discovered by an FD discovery algorithm, it leverages negative and positive examples in a human-in-the-loop workflow to:

- eliminate **fake** FDs: spurious FDs that do not hold when certain additional valid tuples are introduced
- reveal **ghost** FDs: FDs which remain undetected due to data errors but would hold in a clean dataset

### Architecture Overview

FDepHunter is a client–server web application accessed in the browser. The frontend is built with React and TypeScript. The backend is implemented in Java 21 using the Spring Boot framework.

## Setup

### Manual

- See the [backend](./backend/README.md) and [frontend](./frontend/README.md) READMEs for more information.

### Docker

1. Follow the *Configuration* sections in the backend and frontend README files.
   - **Note:** Pay close attention to the environment files (`.env`), as there are differences between local development and Docker.
2. Build and start the containers:

   ```bash
   docker compose up -d --build
   ```

## Getting Started

- After opening the client, click the **Reset DB** button to create example datasets.

## How to Cite

Please cite FDepHunter as follows:

```bibtex
@article{Koupil2025FDepHunter,
  author    = {Pavel Koupil and Jáchym Bártík and Stefan Klessinger and André Conrad and Stefanie Scherzinger},
  title     = {FDepHunter: Harnessing Negative Examples to Expose Fakes and Reveal Ghosts},
  journal   = {PVLDB},
  volume    = {18},
  number    = {12},
  pages     = {5227--5230},
  year      = {2025},
  doi       = {10.14778/3750601.3750612}
}
```
