#!/usr/bin/python
import numpy as np
"""
This is a script for genereting the Roborta vs Rigoborto example, it has several options:
    -h prints de help
    -p generates a plain grid, without probabilities
    -rewards add random rewards to the grid
    -path the folder where the benchmark will be written
    -s (-seed) the seed used to generate the grid
    -l (-length) the length of the grid
    -w (-width) the width of the grid
    --rob-init the initial position of roborta
    --rig-init the initial position of rigoborto  

you can generate all the benchmark with the scrip Gen-bench.py
"""

from asyncio.sslproto import SSLProtocolState
from pty import slave_open
import sys, getopt
import random
import math
from turtle import width

# this generates latex code containing a graphical representation of the grid
def grid2Tex(seed, fileName) :
    mFile = open(fileName, "w")
    # the heading of the file
    mFile.write(r"""\documentclass{article}
\usepackage{graphicx} % Required for inserting images
\usepackage{multicol}
\usepackage{multirow}
\usepackage[table]{xcolor}
\usepackage{tikz}
""")                
    mFile.write(f"""\\title{{Table Roborta vs Rigoborto:  size: ${width} \\times {length}$}}""")
    mFile.write(r"""
    \author{}
    \date{}
    \begin{document}    
    \maketitle
""")
    # we define the beginning of the table
    mFile.write(r"""\begin{tikzpicture}
\matrix [nodes=draw, column sep=-0mm]
{
                """)
    for j in range(length) :
        for i in range(width) :
            mFile.write(f"""
    \\node (a{j}{i}) [fill=red! {qterrain[j][i]*100} ,minimum size=2cm]  {{\\begin{{tabular}}{{c}}   \\   \;  \\  \end{{tabular}}}}; 
    """)
            if (i != width-1) :
                mFile.write(""" & 
                            """)
            else :
                mFile.write(r""" \\ """)
    mFile.write("""
    };""")
    # we write the arrows to the file, for each node we write the corresponding arrows
    for j in range(length) :
        for i in range(width) :
            if (lslope[j][i] < 0) :
                mFile.write(f"""
    \\draw[line width=1ex,black!{100*abs(lslope[j][i])},->] ([xshift=-5pt]a{j}{i}.center) -- (a{j}{i}.west); """)
            if (lslope[j][i] > 0) :
                mFile.write(f"""
    \\draw[line width=1ex,black!{100*abs(lslope[j][i])},->] ([xshift=5pt]a{j}{i}.center) -- (a{j}{i}.east); """)
    for j in range(length) :
        for i in range(width) :
            if (fslope[j][i] < 0) :
                mFile.write(f"""
    \\draw[line width=1ex,black!{100*abs(fslope[j][i])},->] ([yshift=-5pt]a{j}{i}.center) -- (a{j}{i}.south); 
                    """)
            if (fslope[j][i] > 0) :
                mFile.write(f"""
    \\draw[line width=1ex,black!{100*abs(fslope[j][i])},->] ([yshift=5pt]a{j}{i}.center) -- (a{j}{i}.north);  
    """)
    mFile.write("""
\end{tikzpicture}
\end{document}""")
    mFile.close()

# it creates a board using the seed, this board can be written to a file using
# write board
def initBoard(seed) :
    global fslope
    global lslope
    global qterrain
    global width
    global length
    global plain # for the standard case with no uncertainties
    fslope = [[0 for x in range(width)] for y in range(length)] # we create a bidimensional array for fslope
    lslope = [[0 for x in range(width)] for y in range(length)] # similarly for lslope
    qterrain = [[0 for x in range(width)] for y in range(length)] # similarly for qterrain

    random.seed(seed)

    # we define qterrain, the quality of the terrain for each cell
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                qterrain[i][j] = random.uniform(0,0.5)
    # we define the fslope for each cell
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                fslope[i][j] = random.uniform(-1,1)
    #we define the lslope for each cell            
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                lslope[i][j] = random.uniform(-1,1)
              

# the definition of constants Q_TERRAIN, F_SLOPE and L_SLOPE
def writeBoard(fileName) :  
    global fslope
    global lslope
    global qterrain
    global width
    global length
    global plain # for the standard case with no uncertainties

    #fslope = [[0 for x in range(width)] for y in range(length)] # we create a bidimensional array for fslope
    #lslope = [[0 for x in range(width)] for y in range(length)] # similarly for lslope
    #qterrain = [[0 for x in range(width)] for y in range(length)] # similarly for qterrain

    mFile = open(fileName,"w")
    mFile.write("//Since PRISM does not support arrays we use directly the constants as defined below:\n")
    #random.seed(seed)
    # we define qterrain, the quality of the terrain for each cell
    mFile.write(f"""// Terrain quality:
                    // Q_TERRAIN is a constant (WIDTH x LENGTH) matrix of floats in the interval [0,0.5]
                    // where 0 means "optimal quality" while 0.5 corresponds to the worst quality)
                """)
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                #qterrain[i][j] = random.uniform(0,0.5)
                mFile.write(f"""// Q_TERRAIN[{i}][{j}] = {qterrain[i][j]}\n""") 

    mFile.write(f"""
                // F_SLOPE is a constant (WIDTH x LENGTH) matrix of floats in the interval [-1,1]
                // corresponding to the frontal inclination where 0 indicats that the terrain has no frontal inclination,
                // -1 is the maximum descending inclination, and 1 is the maximum ascending inclination.
                """)
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                #fslope[i][j] = random.uniform(-1,1)
                mFile.write(f"""// F_SLOPE[{i}][{j}] =  {fslope[i][j]}\n""")

    mFile.write(f"""
                // L_SLOPE is a constant (WIDTH x LENGTH) matrix of floats in the interval [-1,1]
                // corresponding to the lateral inclination where 0 indicats that the terrain has no lateral inclination,
                // -1 is the maximum inclination towards the left, and 1 is the maximum inclination towards the right.
                """)
    for i in range(length) :
        for j in range(width) :
            if (not plain) :
                #lslope[i][j] = random.uniform(-1,1)
                mFile.write(f"""// L_SLOPE[{i}][{j}]   =  {lslope[i][j]}\n""")
    mFile.close();    
    
# it writes the heading of the file
def writeHeading(fileName) :
    global width
    global length
    mFile = open(fileName, "a")
    mFile.write("smg\n")
    mFile.write("player p1\n")
    mFile.write("   roborta, [robl], [robr], [robf], [robb]\n")
    mFile.write("endplayer\n")
    mFile.write("\n");
    mFile.write("player p2\n")
    mFile.write("  rigoborto, [rigl], [rigr], [rigf], [rigb]\n")
    mFile.write("endplayer\n")
    mFile.write("\n");
    mFile.write(f"const int LENGTH = {length};\n")
    mFile.write("global sched : [0..1] init 0;\n")
    mFile.write("""
// In this case Roborta is a minimizer she wants to reach the exit asap
// on the other hand Rigoborto wants to maximize, that is to stop Roborta.
rewards "r0"
    Collision : 10;
    !Collision & robrow < LENGTH : 1;
    !Collision & robrow = LENGTH : 0;
endrewards
                  
// Roborta and Rigoborto occupy the same cell producing a collision
    formula Collision = (robcol = rigcol) & (robrow = rigrow);

// Roborta reached the end fo the grid
    formula Robwins =  (robrow = LENGTH);
                
// end of game 
    formula end = Collision | Robwins;
""")
    # if rewards is True we have to write down the rewards
    if rewards :
        mFile.write("""rewards "r1"\n """)
        rews = np.random.randint(0,3, size=(length, width)) # a random matrix of rewards is generated
        for i in range(length) :
            for j in range(width) :
                mFile.write(f""" robrow = {j} & robcol = {i} & robrow < LENGTH : {rews[i][j]};\n  """)
        mFile.write("""endrewards""")
    mFile.close()

# Definition of Roborta
def writeRoborta(fileName) :
    global fslope
    global lslope
    global qterrain
    global width
    global length
    global rob_x
    global rob_y
    mFile = open(fileName, "a")
    mFile.write(f"""
// Module for Roborta, in case a collision the games finishes
module roborta
    robcol : [0..{width-1}] init {rob_x};
    robrow : [0..{length}] init {rob_y};\n""")
    
    # probabilistic choices
    for i in range(0,length) :
        for j in range(0,width) :
            mFile.write(f"""    [robl] (sched = 0) & (robrow = {i}) & (robcol = {j}) & !Collision  -U->""")
            mFile.write(f"""
            pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
            pr : (robcol' = {min(width-1, j+1)}) & (sched' = 1) +   // corresponds to environments setbacks
            pf : (robrow' = {i+1}) & (sched' = 1) +
            pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
            pm : (robcol' = {max(0, j-1)}) & (sched' = 1)            // Command successful
            """)
            mFile.write("{");
            mFile.write(f"""
            // Success is affected by the quality and inclination of the terrain
            pm <= {1 - (qterrain[i][j] + (1 - (1 - abs(lslope[i][j])) * (1 - abs(fslope[i][j]))) / 2)},
            // The following are the conditions for the probabilities of sideways and frontal displacements
            {(1-max(0,-lslope[i][j]))} * pl + (- {(1 - qterrain[i][j]) * (1 - max(0,lslope[i][j]))} * pr) >= 0,
            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1 - max(0,-lslope[i][j]))} * pl) >= 0,
            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1 - max(0,-fslope[i][j]))} * pb) >= 0,
            {1-max(0,-fslope[i][j])} * pb + (- {(1 - qterrain[i][j]) * (1 - max(0,fslope[i][j]))} * pf) >= 0
            """)
            mFile.write("};\n")

    # second action
            if (i < length) :
                mFile.write(f"""    [robr] (sched = 0) & robrow = {i} & robcol = {j} & !Collision  -U->""")
                mFile.write(f"""
            pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
            pr : (robcol' = {min(width-1, j+1)}) & (sched' = 1) +   // corresponds to environments setbacks
            pf : (robrow' = {i+1}) & (sched' = 1) +
            pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
            pm : (robcol' = {min(width-1, j+1)}) & (sched' = 1)     // Command successful
            """)
                mFile.write("{")
                mFile.write(f"""    
            // Success is affected by the quality and inclination of the terrain
            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
            // The following are the conditions for the probabilities of sideways and frontal displacements
            {(1-max(0,-lslope[i][j]))} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1- max(0,-fslope[i][j]))} * pb) >= 0,
            {(1-max(0,-fslope[i][j]))} * pb + (- {(1 - qterrain[i][j]) * (1 - fslope[i][j])} * pf) >= 0 
            """)
                mFile.write("};")          
            # third action
            if (i < length) :
                mFile.write(f"""
            [robf] (sched = 0) &  (robrow = {i}) & (robcol = {j})  & !Collision  -U->
            pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
            pr : (robcol' = {min(width-1, i+1)}) & (sched' = 1) +   // corresponds to environments setbacks
            pf : (robrow' = {i+1}) & (sched' = 1) +
            pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
            pm : (robrow' = {i+1}) & (sched' = 1)                   // Command successful 
            """)
                mFile.write("{")
                mFile.write(f"""
            // Success is affected by the quality and inclination of the terrain
            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
            // The following are the conditions for the probabilities of sideways and frontal displacements
            { (1-max(0,-lslope[i][j])) } * pl + (- { (1 - qterrain[i][j]) * (1-max(0,lslope[i][j])) } * pr) >= 0,
            { (1-max(0,lslope[i][j])) } * pr + (-{(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j])) } * pl) >= 0,
            { (1-max(0,fslope[i][j])) } * pf + (-{ (1 - qterrain[i][j]) * (1- max(0,-fslope[i][j])) } * pb) >= 0,
            { (1-max(0,-fslope[i][j]))} * pb + (-{ (1 - qterrain[i][j]) * (1-max(0,fslope[i][j])) } * pf) >= 0 
            """)     
            
                mFile.write("};\n")
            #fourth action
            if (i < length) :
                mFile.write(f"""
            [robb] (sched = 0) &  (robrow = {i}) & (robcol = {j})  & !Collision  -U->
            pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
            pr : (robcol' = {min(width-1, j+1)}) & (sched' = 1) +   // corresponds to environments setbacks
            pf : (robrow' = {i+1}) & (sched' = 1) +
            pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
            pm : (robrow' = {max(0, i-1)}) & (sched' = 1)           // Command successful
            """)
                mFile.write("{")
                mFile.write(f"""
            // Success is affected by the quality and inclination of the terrain
            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
            // The following are the conditions for the probabilities of sideways and frontal displacements
            {(1-max(0,-lslope[i][j]))} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1-max(0,-fslope[i][j]))} * pb) >= 0,
            {(1-max(0,-fslope[i][j]))} * pb + (- {(1 - qterrain[i][j]) * (1-max(0,fslope[i][j]))} * pf) >= 0
            """)
                mFile.write("};\n") 
    mFile.write("endmodule\n")
    mFile.close()       
    
# Definition of Rigoborto
def writeRigoborto(fileName) :
    global width
    global length
    global rig_x
    global rig_y
    mFile = open(fileName, "a")
    mFile.write(f""" // Module for Rigoborto, in case a collision the games finishes
            module rigoborto
            rigcol : [0..{width-1}] init {rig_x};
            rigrow : [0..{length}] init {rig_y};
            """)
    for i in range(0,length) :
        for j in range(0,width) :

        # action one
            mFile.write(f"""
                [rigl] (sched = 1) & (rigrow = {i}) & (rigcol = {j}) & !Collision  -U->
                pl : (rigcol' = { max(0, j-1)}) & (sched' = 0) +        // The first four probabilistic options
                pr : (rigcol' = {min(width-1, j+1)}) & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {min(length-1, i+1)}) & (sched' = 0) +
                pb : (rigrow' = {max(0, i-1)}) & (sched' = 0) +
                pm : (rigcol' = {max(0, j-1)}) & (sched' = 0)           // Command successful
                """)
            mFile.write("{\n")
            mFile.write(f"""
                // Success is affected by the quality and inclination of the terrain
	            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {(1-max(0,-lslope[i][j]))} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
	            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
	            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1-max(0,-fslope[i][j]))} * pb) >= 0,
	            {(1-max(0,-fslope[i][j]))} * pb + (- {(1 - qterrain[i][j]) * (1-max(0,fslope[i][j]))} * pf) >= 0 
            """)
            mFile.write("};")    

            #action two
            mFile.write(f""" 
                [rigr] (sched = 1) & (rigrow = {i}) & (rigcol = {j}) & !Collision  -U->
                pl : (rigcol' = {max(0, j-1)}) & (sched' = 0) +         // The first four probabilistic options
                pr : (rigcol' = {min(width-1, j+1)}) & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {min(length-1, i+1)}) & (sched' = 0) +
                pb : (rigrow' = {max(0, i-1)}) & (sched' = 0) +
                pm : (rigcol' = {min(width-1, j+1)}) & (sched' = 0)     // Command successful 
                """)
            mFile.write("{\n")
            mFile.write(f""" 
                // Success is affected by the quality and inclination of the terrain
	            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {(1-max(0,-lslope[i][j]))} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
	            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
	            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1-max(0,-fslope[i][j]))} * pb) >= 0,
	            {(1-max(0,-fslope[i][j]))} * pb + (- {(1 - qterrain[i][j]) * (1-max(0,fslope[i][j]))} * pf) >= 0
                """)
            mFile.write("};\n")

            #action three
            mFile.write(f"""
                [rigf] (sched = 1) & !Collision  & rigrow = {i} & rigcol = {j} -U->
                pl : (rigcol' = {max(0, j-1)}) & (sched' = 0) +         // The first four probabilistic options
                pr : (rigcol' = {min(width-1, j+1)}) & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {min(length-1, i+1)}) & (sched' = 0) +
                pb : (rigrow' = {max(0, i-1)}) & (sched' = 0) +
                pm : (rigrow' = {min(length-1, i+1)}) & (sched' = 0)    // Command successful
                """)
            mFile.write("{")
            mFile.write(f"""
                // Success is affected by the quality and inclination of the terrain
	            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {1-max(0,-lslope[i][j])} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
	            {(1-max(0,lslope[i][j]))} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
	            {(1-max(0,fslope[i][j]))} * pf + (- {(1 - qterrain[i][j]) * (1-max(0,-fslope[i][j]))} * pb) >= 0,
	            {(1-max(0,-fslope[i][j]))} * pb + (- {(1 - qterrain[i][j]) * (1-max(0,fslope[i][j]))} * pf) >= 0
                """)
            mFile.write("};\n")

            #action fourth
            mFile.write(f"""
                [rigb] (sched = 1) &  rigrow = {i} & rigcol = {j} & !Collision  -U->
                pl : (rigcol' = {(max(0, j-1))}) & (sched' = 0) +         // The first four probabilistic options
                pr : (rigcol' = {(min(width-1, j+1))}) & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {(min(length-1, i+1))}) & (sched' = 0) +
                pb : (rigrow' = {max(0, i-1)}) & (sched' = 0) +
                pm : (rigrow' = {max(0, i-1)}) & (sched' = 0)           // Command successful
                """)
            mFile.write("{")
            mFile.write(f"""
	            // Success is affected by the quality and inclination of the terrain
	            pm <= {1 - (qterrain[i][j] + (1 - (1-abs(lslope[i][j])) * (1-abs(fslope[i][j]))) / 2)},
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {1-max(0,-lslope[i][j])} * pl + (- {(1 - qterrain[i][j]) * (1-max(0,lslope[i][j]))} * pr) >= 0,
	            {1-max(0,lslope[i][j])} * pr + (- {(1 - qterrain[i][j]) * (1-max(0,-lslope[i][j]))} * pl) >= 0,
	            {1-max(0,fslope[i][j])} * pf + (- {(1 - qterrain[i][j]) * (1-max(0,-fslope[i][j]))} * pb) >= 0,
	            {1-max(0,-fslope[i][j])} * pb + (- {(1 - qterrain[i][j]) * (1-max(0,fslope[i][j]))} * pf) >= 0
                """)
            mFile.write("};")
    mFile.write("endmodule\n")
    mFile.close()

def usage(exitVal) :
    print("\nusage Roborta_gen.py [-h] [-s <int> ] [-w <int>] [-l <int>]\n")
    print("-h :\n")
    print("  Print this help\n")
    print("-s num :\n")
    print("  Sets the seed for the pseudo-random number generator to 'num'. 'num' must be")
    print("  a non-negative integer (0 or higher) (default = 0)\n")
    print("-w num :\n")
    print("  Sets the width of the board to 'num'. 'num' must be a positive integer")
    print("  (greater than 0) (default = 5)\n")
    print("-l num :\n")
    print("  Sets the length of the board to 'num'. 'num' must be a positive integer")
    print("  (greater than 0) (default = 10)\n")
    sys.exit(exitVal)

def main(argv):
    global width
    global length
    global rob_x
    global rob_y
    global rig_x
    global rig_y
    global plain
    global list_init_rob
    global list_init_rig
    global diagonal 
    global rewards

    # default value for the arguments
    width = 4
    length = 4
    seed = 0
    path = ""
    rob_x = 0
    rob_y = 0
    rig_x = width - 1
    rig_y = length - 1 
    plain = False # this var is used to generate a standard matrix with no uncertainties
    pdf = False # var used to indicate if a pdf will be produced
    diagonal = False # if we generate the diagonal benchmark
    rewards = False # it says if a reward structure have to be generated

    try:
        opts, args = getopt.getopt(argv,"phs:w:l:",["seed=","width=","length=","rob-init=","rig-init=", "path=","diagonal=", "pdf", "rewards"])
        print(opts,args)
    except getopt.GetoptError:
        usage(2)
    for opt, arg in opts: 
        if opt == '-h': # prints the help
            usage(0)
        elif opt in ("-p") :  # a plain grid, no probabilities
            plain = True  
        elif opt in ("--rewards") :  # random rewards are generated
            rewards = True 
        elif opt in ("--path") : #  the path where the benchmark will be saved
            path = arg           
        elif opt in ("-s","--seed") : # the seed to be used
            try :
                seed = int(arg)
            except ValueError :
                print("The width must be a nonnegative integer")
                sys.exit(2)            
            if seed < 0 :
                print("The width must be a nonnegative integer")
                sys.exit(2)
        elif opt in ("-w","--width") : # the width of the grid
            try :
                width = int(arg)
            except ValueError :
                print("The width must be a positive integer")
                sys.exit(2)            
            if width <= 0 :
                print("The width must be a positive integer")
                sys.exit(2)
        elif opt in ("-l","--length") : # the length of the grid
            try :
                length = int(arg)
            except ValueError :
                print("The length must be a positive integer")
                sys.exit(2)            
            if length <= 0 :
                print("The length must be a positive integer")
                sys.exit(2)
        elif opt in ("--rob-init") : # the initial position for roborta
            try :
                rob_x, rob_y = tuple(int(i) for i in arg.split(","))
            except ValueError :
                print("Error in the initial position of Roborta")
        elif opt in ("--rig-init") : # the initial position for rigoborto
            try :
                rig_x, rig_y = tuple(int(i) for i in arg.split(","))
            except ValueError :
                print("Error in the initial position of Rigoborto")
        elif opt in ("--pdf") :
            pdf = True
        elif opt in ("--diagonal") :  # it creates all the files where the diagonal are the starting cells for the robots
            diagonal = True
            batch = int(arg) # the batch is the number of batchs that it will be created for each instance of the grid
            assert length == width, 'for the diagonal option the grid has to be a square'  
    # now, the files are generated and saved in the corresponding folders
    b = 0        
    if diagonal : # it generates a diagonal  using the length for recorring the diagonal, we assume a square grid
        for b in range(0,batch) : # we run the number of batches 
            initBoard(b) # we use the batch as the seed
            rob_x, rob_y = 0, 0
            
            for i in range(0,length) :
                rob_x, rob_y = i, i
                rig_x, rig_y = length  - 1, length  - 1   
                #for j in range(1,length-i) :
                #rig_x, rig_y = length - j, length - j
                fileName = path+f"RobRig-{b}-{width}-{length}-RobX{rob_x}-RobY{rob_y}-RigX{rig_x}-RigY{rig_y}-Number{b}.smg"
                writeBoard(fileName)
                    #grid2Tex(seed,path+f"RobRig_{seed}-{width}-{length}-Rob{rob_x}{rob_y}-Rig{rig_x}{rig_y}.tex")
                writeHeading(fileName)
                writeRoborta(fileName)
                writeRigoborto(fileName)  
    else :
        fileName = path+f"RobRig-{seed}-{width}-{length}-RobX{rob_x}-RobY{rob_y}-RigX{rig_x}-RigY{rig_y}-Number{b}.smg"
        pdfFile = path+f"RobRig-{seed}-{width}-{length}-RobX{rob_x}-RobY{rob_y}-RigX{rig_x}-RigY{rig_y}-Number{b}.tex"
        initBoard(seed)
        writeBoard(fileName)
        if pdf :
            grid2Tex(seed,pdfFile)
        writeHeading(fileName)
        writeRoborta(fileName)
        writeRigoborto(fileName)  

if __name__ == "__main__":
    main(sys.argv[1:])