#!/bin/bash
#SBATCH -A c_gnn_001               # Account name to be debited
#SBATCH --job-name=tsp22          # Job name
#SBATCH --time=0-01:00:00        # Maximum walltime (30 minutes)
#SBATCH --partition=cpu           # Select the ai partition
## --gres=gpu:1          # Request 1 to 4 GPUs per node
#SBATCH --mem-per-cpu=100000       # Memory per CPU core (16 GB)
#SBATCH --nodes=1               # Request 1 node


export JAVA_HOME=/project/c_gnn_001/tools/java/jdk-17.0.12
export PATH=$JAVA_HOME/bin:$PATH
export PATH=/project/c_gnn_001/conda_envs/torch/gradle/bin:$PATH
