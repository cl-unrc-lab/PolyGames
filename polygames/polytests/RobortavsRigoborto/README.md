# Roborta VS Rigoborto example

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




