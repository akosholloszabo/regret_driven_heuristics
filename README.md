# Regret-Driven Heuristics

A Kotlin-based research project for solving the **Asymmetric Travelling Salesman Problem (ATSP)** using regret-driven construction heuristics combined with local search optimization. This project evaluates how predicted regret matrices—produced by machine learning models—can guide greedy tour construction and subsequent 3-opt improvement, and analyses the impact of prediction noise on solution quality.

> **📄 Publication:** This repository implements the heuristic components (Stage 3) of the following paper:
>
> **[Heterogeneous graph neural networks for scalable asymmetric traveling salesman problem optimization](https://doi.org/10.1016/j.neucom.2026.132718)**
>
> *Walid Guettala, Ákos Levente Holló-Szabó, László Gulyás, János Botzheim*
>
> **Neurocomputing**, Volume 671, 28 March 2026, Article 132718
>
> DOI: [`10.1016/j.neucom.2026.132718`](https://doi.org/10.1016/j.neucom.2026.132718) — Open Access (CC BY 4.0)
>
> 🔗 GNN training code (Stages 1 & 2): [github.com/walidgeuttala/atsp_gnn](https://github.com/walidgeuttala/atsp_gnn)

---

## Table of Contents

- [Publication](#publication)
- [Overview](#overview)
- [Key Concepts](#key-concepts)
- [Project Structure](#project-structure)
- [Algorithms](#algorithms)
  - [One-Shot Edge Builder](#one-shot-edge-builder)
  - [Regret-Guided 3-Opt](#regret-guided-3-opt)
- [Evaluation Pipelines](#evaluation-pipelines)
- [Data Format](#data-format)
- [Visualization](#visualization)
- [Prerequisites](#prerequisites)
- [Building & Running](#building--running)
- [Output](#output)
- [Citation](#citation)
- [License](#license)

---

## Publication

This repository is part of a larger research effort published in *Neurocomputing*. The paper presents a **three-stage pipeline** for solving the ATSP at scale:

| Stage | Component | Description | Repository |
|:---:|---|---|---|
| 1 | **Heterogeneous Line Graph HL(G)** | Converts directed edges into nodes connected via four distinct adjacency types, preserving asymmetric relations and reducing density by 25% relative to undirected L(G). | [atsp_gnn](https://github.com/walidgeuttala/atsp_gnn) |
| 2 | **Het-GAT** | Relation-aware GNN that processes HL(G) to learn edge-level regret via relation fusion (summation, concatenation, or attention). Achieves 91.88% regret correlation on ATSP50. | [atsp_gnn](https://github.com/walidgeuttala/atsp_gnn) |
| 3 | **Regret-Guided Heuristics** | Batch-Selecting Regret-Guided Edge Builder + Regret-Guided Pre-Evaluating 3-Opt for tour construction and refinement. | **This repository** |

### Key results from the paper

- **Het-GAT-Concat** achieves **8.34%** optimality gap on ATSP250 and **7.89%** on ATSP500, compared to GLOP's 27.96% and 38.96%.
- Runtimes of **0.75s** (ATSP250) and **7.20s** (ATSP500) — **86% faster than LKH-3**.
- **98% reduction** in training time and **75% reduction** in training data compared to prior approaches.
- Scales effectively to **1000 nodes** via subgraph inference.

---

## Overview

This project implements and benchmarks **Stage 3** of the pipeline — the regret-driven heuristic solvers for ATSP instances:

1. **Construction phase** — The *Batch-Selecting Regret-Guided Edge Builder* constructs an initial tour in O(n² log n) by iteratively selecting the lowest-regret edges from a predicted regret matrix, while maintaining feasibility constraints (no sub-tours, proper degree).
2. **Improvement phase** — *Regret-Guided Pre-Evaluating 3-Opt* iteratively improves the tour in O(n³) by evaluating segment reconnections, prioritizing edges with high predicted regret for removal.

The project provides multiple entry points for evaluating:
- Heuristic performance across different **problem sizes** (50, 100, 150, 250, 500 nodes)
- Impact of varying numbers of **3-opt iterations**
- Sensitivity to **Gaussian noise** injected into predicted regret matrices

---

## Key Concepts

| Concept | Description |
|---|---|
| **Regret Matrix** | A square matrix where entry *(i, j)* represents the "regret" of not including edge *(i → j)* in the optimal solution. Lower regret → higher priority for selection. |
| **Predicted vs. Expected Regret** | The *expected* regret matrix is the ground truth; the *predicted* regret matrix is produced by a machine learning model and may contain noise. |
| **Gap Ratio** | The relative difference between the heuristic solution cost and the known optimum: `(cost / optimal) - 1.0`. A gap of `0.0` means optimal. |
| **Noise Level** | Standard deviation of additive Gaussian noise applied to the expected regret matrix, used to simulate prediction errors. |

---

## Project Structure

```
regret_driven_heuristics/
├── build.gradle                          # Gradle build configuration (Kotlin 2.2.0, JVM 17)
├── settings.gradle                       # Project name definition
├── gradle.properties                     # Kotlin code style settings
│
├── src/main/kotlin/hu/akos/hollo/szabo/
│   ├── EvaluateDataset.kt                # Dataset analysis (regret matrix sparsity statistics)
│   ├── EvaluateHeuristicsOnDifferent
│   │   ProblemSizesAndHyperParameters.kt # Evaluate across problem sizes & 3-opt iterations
│   ├── EvaluateHeuristicsOnDifferent
│   │   ProblemSizesAndNoise.kt           # Noise sensitivity analysis (all instances)
│   ├── EvaluateHeuristicsOnDifferent
│   │   ProblemSizesAndNoise2.kt          # Noise sensitivity analysis (single-solution instances)
│   ├── CalculateNoiseOnRegretPredictions.kt  # (Placeholder for noise calculation)
│   │
│   ├── heuristics/
│   │   ├── OneShotEdgeBuilder.kt         # Greedy tour construction from regret matrix
│   │   ├── ThreeOptCycle.kt              # Regret-guided 3-opt improvement cycle
│   │   ├── ThreeOptOperator.kt           # 3-opt move application & cost delta computation
│   │   └── EvaluateTsp.kt               # Tour cost evaluation
│   │
│   └── model/
│       ├── RegretData.kt                 # Data class for an ATSP instance with regret matrices
│       ├── LoadRegretData.kt             # File parser for regret datasets
│       ├── CostRecord.kt                 # Data class for tracking solution costs
│       ├── GraphEdge.kt                  # Generic weighted directed edge
│       ├── DoubleStatistics.kt           # Descriptive statistics data class
│       ├── ArrayExtensions.kt            # Matrix utilities & statistics computation
│       └── NoiseRecord.kt               # Record parser for experiment output files
│
├── notebooks/
│   ├── NoiseAnalyses.ipynb               # Kotlin Notebook for visualizing noise experiments
│   └── output/                           # Exported SVG plots
│       ├── noise_plot_light.svg
│       ├── noise_plot_light_heatmap.svg
│       ├── noise_plot_light_merged.svg
│       └── ...
│
└── output/                               # Raw experiment output files
    ├── output-2025-09-21.txt
    └── output-2025-09-21-2.txt
```

---

## Algorithms

### One-Shot Edge Builder

**File:** `OneShotEdgeBuilder.kt`

A greedy construction heuristic that builds a Hamiltonian tour in a single pass:

1. For each source node, sorts candidate edges by ascending regret value.
2. Iteratively selects the globally lowest-regret feasible edge, ensuring:
   - No node has more than one outgoing or one incoming selected edge.
   - No premature sub-tour is formed (tracked via sequence/chain bookkeeping).
3. After selecting *n − 1* edges, closes the tour with the remaining edge.
4. Converts the sequential (successor) representation to a permutation.

This approach is efficient and deterministic—given a regret matrix, it always produces the same tour.

### Regret-Guided 3-Opt

**Files:** `ThreeOptCycle.kt`, `ThreeOptOperator.kt`

A local search improvement operator that performs 3-opt moves guided by the predicted regret matrix:

1. **Edge prioritization:** Ranks tour edges by descending predicted regret. Edges with high regret are more likely to be suboptimal and are prioritized for removal.
2. **Candidate filtering:** For problem size 500, limits candidates to the top 206 highest-regret edges for tractability.
3. **Move evaluation:** For each triple of candidate positions:
   - Estimates cost change using the regret matrix as a fast filter.
   - Computes exact cost delta from the distance matrix.
   - Applies the move in-place if it improves the tour.
4. **Validation:** Includes a runtime assertion verifying that incremental cost tracking matches the full re-evaluation.

The 3-opt move reconnects three tour segments by removing three edges and adding three new ones, producing a different segment ordering.

---

## Evaluation Pipelines

| Entry Point | Description |
|---|---|
| `EvaluateHeuristicsOnDifferentProblemSizesAndHyperParameters.kt` | Tests the heuristic pipeline on ATSP instances of size 100, 150, 250, and 500 with varying 3-opt iteration counts (0–9). Reports gap ratio and execution time statistics. |
| `EvaluateHeuristicsOnDifferentProblemSizesAndNoise.kt` | Injects Gaussian noise (σ = 0.00–0.50) into expected regret matrices and evaluates the effect on solution quality across 0–10 3-opt iterations on size-50 instances. |
| `EvaluateHeuristicsOnDifferentProblemSizesAndNoise2.kt` | Same as above, but filtered to **single-solution instances** (where the expected regret matrix has exactly 2 zeros per row, and the edge builder recovers the optimal tour from clean data). Uses 10 noise samples per instance. |
| `EvaluateDataset.kt` | Analyses regret matrix properties: sparsity (zero-count) statistics and combinatorial branching factor of the expected regret. |

---

## Data Format

Each ATSP instance file follows a plain-text format:

```
<header line>
<distance matrix rows: space-separated doubles>

regret:
<expected regret matrix rows: space-separated doubles>

regret_pred:
<predicted regret matrix rows: space-separated doubles>

opt_cost <optimal tour cost>
```

Instance files are organized in directories by problem size (e.g., `test_atsp100/`, `test_atsp150/`).

---

## Visualization

The `notebooks/NoiseAnalyses.ipynb` Kotlin Notebook uses the [Kandy](https://kotlin.github.io/kandy/) plotting library to visualize experimental results:

- **Line plots:** Gap ratio vs. noise level, grouped by 3-opt iteration count
- **Ribbon plots:** Shaded area charts showing gap accumulation
- **Heatmaps:** Gap ratio as a function of both noise level and 3-opt iterations

Pre-rendered SVG plots are available in `notebooks/output/`.

---

## Prerequisites

- **JDK 17** or later
- **Gradle** (wrapper included — no separate installation needed)
- **ATSP dataset files** stored locally (paths are hardcoded and must be adjusted to your environment)

---

## Building & Running

### Build the project

```bash
./gradlew build
```

### Run an evaluation

Each evaluation pipeline has its own `main()` function. Run them via Gradle or your IDE:

```bash
# Example: Run the noise sensitivity analysis
./gradlew run -PmainClass=hu.akos.hollo.szabo.EvaluateHeuristicsOnDifferentProblemSizesAndNoiseKt
```

Or run directly from IntelliJ IDEA by clicking the green ▶ gutter icon next to any `fun main()`.

> **Note:** Dataset paths are currently hardcoded (e.g., `H:\by-domain\official\phd\research\datasets\...`). Update these paths in the source files to point to your local dataset location before running.

---

## Output

Evaluation results are written to the `output/` directory as plain-text files with the naming convention `output-<date>.txt`. Each run produces pairs of lines:

1. **Gap ratio statistics** — `DoubleStatistics(max=..., min=..., avg=..., q1=..., q2=..., median=..., standardDeviation=...)`
2. **Execution time statistics** (milliseconds) — same format

These files can be loaded and visualized using the provided Kotlin Notebook.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.2.0 | Primary language |
| Kotlin Coroutines | 1.6.4 | Concurrency support |
| Gradle | Wrapper | Build system |
| JVM | 17 | Target runtime |
| Kandy | — | Plotting (notebooks) |

---

## Citation

If you use this code in your research, please cite:

```bibtex
@article{guettala2026heterogeneous,
  title     = {Heterogeneous graph neural networks for scalable asymmetric traveling salesman problem optimization},
  author    = {Guettala, Walid and Holló-Szabó, Ákos Levente and Gulyás, László and Botzheim, János},
  journal   = {Neurocomputing},
  volume    = {671},
  pages     = {132718},
  year      = {2026},
  publisher = {Elsevier},
  doi       = {10.1016/j.neucom.2026.132718},
  url       = {https://www.sciencedirect.com/science/article/pii/S0925231226001153},
  note      = {Open Access (CC BY 4.0)}
}
```

### Authors

All authors are affiliated with the **Department of Artificial Intelligence, ELTE Eötvös Loránd University**, Budapest, Hungary.

| Author | Role | ORCID |
|---|---|---|
| **Walid Guettala** *(corresponding)* | Conceptualization, Methodology, Software, Investigation, Data curation, Formal analysis, Writing | [0000-0001-5941-6818](https://orcid.org/0000-0001-5941-6818) |
| **Ákos Levente Holló-Szabó** | Methodology, Software, Investigation, Writing | [0009-0008-6428-634X](https://orcid.org/0009-0008-6428-634X) |
| **László Gulyás** | Supervision, Validation, Writing – review & editing | [0000-0002-6367-6695](https://orcid.org/0000-0002-6367-6695) |
| **János Botzheim** | Supervision, Validation, Writing – review & editing | [0000-0002-7838-6148](https://orcid.org/0000-0002-7838-6148) |

---

## License

This project accompanies an Open Access publication licensed under [CC BY 4.0](http://creativecommons.org/licenses/by/4.0/).




