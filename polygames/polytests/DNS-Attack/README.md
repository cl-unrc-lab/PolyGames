# Bandwitdh Amplification Attack example

This folder contains the files and scripts of the BAA example

# BAA without uncertainty

The file for the standard BAA (without) can be found in the following folder `BAA/` the file `BAA/baa.prism `

To execute the model checker with this example you can execute from this folder:

`../../bin/polygames BAA/baa.prism UncertainBAA/attacker.props -const maxTime=10`

This will exacute polygames with the BAA standard model with 10 rounds.

# Uncertain BAA
  
The uncertain model for this case study can be found in the folder: `UncertainBAA`. You can execute the followign command to model check the example:

`../../bin/polygames network_unreliability=0.5,mimicry_capability=0.5,maxTime=10,nofix=1,ftr=1,rnd=1,agr=1,agf=1,rdr=1` 


the example (as it can be observed) has several parameters, in this case all the strategies are used.

# Using the scripts

The case study can be run via a script `run_benchmark.py`. Executing:

`
python run_benchmark.py
`  

will show several options for benchmarking using the BAA example.
  



