# Roborta vs Rigoborto Example

In this folder you can find all the files for the Roborta vs Rigoborto example, as well as some scripts for
generating the benchmark and running them. There two main folders containing the .prism source codes for the benchmarks

* `benchmarks/` contains many randomly generated grids for the example
* `diagonal/` contains different girds where the robots are getting closer in the diagonal, are 10 instances of each possible initial possition

It contains random generated grids for Roborta and Rigoborto

# Running the Case Studies

For running any case study you can call polygame and the specific files. For instance, you can run:

> ../../../../bin/polygames benchmark/RobRig-0-4-4-Rob00-Rig33.smg benchmark/RobortavsRigoborto.props

# The Scripts

There several scripts for this example:

gen_bench.py: it generates all the benchmark in the folder benchmark
roborta_gen.py: it contains the code for generating one instance of roborta, you can run -h option to see all the options
run_benchmark.py: runs the benchmarks and save te results in a .csv file
gen_plot.py: it generates de plot

# Running the scripts
Typically, you can execute

> python gen_bench.py
> python run_bench.py

to generate and run the benchmarks, or just the second command if you do not want to generate the files again






