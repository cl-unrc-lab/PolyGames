# PolyGames

PolyGames is an extension of PrismGames for supporting the use of polytopes in the definition of games. The too is based on the theoretical results described in:

*Pablo F. Castro, Pedro R. D'Argenio: Polytopal Stochastic Games. Principles of Formal Quantitative Analysis 2025*

## Installing the tool

The tool is distributed with makefiles to compile it in different platforms.

### Requirements

To compile the tool you will need the following:

* gcc compiler, any version > 13
* Java > 17
* PPL, this is the Polyhedral Pharma Library (PPL). 

#### Installing PPL

The PPL source code is also distributed with the tool, you can find the sources in the folder `ppl/`, or you can download the sources from `https://www.bugseng.com/ppl/`. You will find the instructions to build PPL from `INSTALL.txt`. After compiling you need the following files: `libppl_java.jnilib`and `ppl_java.jar` typically located in folder `/usr/local/lib/ppl/` or similar. You need to copy them to `polygames/lib`.

Some linux distributions (e.g., Fedora) provide PPL as a package.

### Compiling the tool

To compile the tool just execute:

`make`

from the folder `polygames/` this will create the binaries in `polygames/bin`

## Benchmarks

In the folder `polygames/polytests` you can find some case studies, among them you can find:

### Roborta vs Rigoborto

This is the example described in:

* *RobortavsRigoborto*, the example described in the paper:
    *Pablo F. Castro, Pedro R. D'Argenio: Polytopal Stochastic Games. Principles of Formal Quantitative Analysis 2025*

You can execute the example as follows. First, move to the example folder:

`cd polygames/polytests/RobortavsRigoborto`

There you can model check the example by calling polygames, or use the scripts. For model checking the example using the polygame execute, for instance:

```
../../bin/polygames roborta-plain.prims RobortavsRigoborto.props -const length=5,width=5,lowerb=0,upperb=0.5
```

this model check the example with the properties in `RobortavsRigoborto.props` and with a grid of 5x5, where the lower bound for q_terrain in 0 and the upper bound is 0.5

To use the script, you can execute:

`python run_benchmark.py`

and this will show you several options for model checking the example, the results will be saved in a .cvs file. For instance:

`python run_benchmark.py subset`

will model check the properties for a 5x5 grid.

### DNS Attack

This is a version of the case study introduced in: 

    *Tushar Deshpande, Panagiotis Katsaros, Stylianos Basagiannis, Scott A. Smolka. Formal Analysis of the DNS Bandwidth Amplification Attack and Its Countermeasures Using Probabilistic Model Checking. HASE '11*

This folder contains the files and scripts of the BAA example

#### BAA without uncertainty

The file for the standard BAA (without) can be found in the following folder `BAA/` the file `BAA/baa.prism `

To execute the model checker with this example you can execute from this folder:

`../../bin/polygames BAA/baa.prism UncertainBAA/attacker.props -const maxTime=10`

This will exacute polygames with the BAA standard model with 10 rounds.

#### Uncertain BAA
  
The uncertain model for this case study can be found in the folder: `UncertainBAA`. You can execute the followign command to model check the example:

`../../bin/polygames network_unreliability=0.5,mimicry_capability=0.5,maxTime=10,nofix=1,ftr=1,rnd=1,agr=1,agf=1,rdr=1` 


the example (as it can be observed) has several parameters, in this case all the strategies are used.

#### Using the script

The case study can be run via a script `run_benchmark.py`. Executing:

`
python run_benchmark.py
`  

will show several options for benchmarking using the BAA example.




