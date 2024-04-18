#!/usr/bin/python

from asyncio.sslproto import SSLProtocolState
from pty import slave_open
import sys, getopt
import random
import math
from turtle import width
#from turtle import width

# the definition of constants Q_TERRAIN, F_SLOPE and L_SLOPE
def genBoard(seed, fileName) :  
    global fslope
    global lslope
    global qterrain
    global width
    global length
    fslope = [[0 for x in range(width)] for y in range(length-1)] # we create a bidimensiona array for fslope
    lslope = [[0 for x in range(width)] for y in range(length-1)] # similarly for lslope
    qterrain = [[0 for x in range(width)] for y in range(length-1)] # similarly for qterrain

    mFile = open(fileName,"w")
    mFile.write("//Since PRISM does not support arrays we use directly the constants as defined below:\n")
    random.seed(seed)
    # we define qterrain, the quality of the terrain for each cell
    for i in range(length-1) :
        for j in range(width) :
            qterrain[i][j] = random.uniform(-1,1)
            mFile.write(f"""// Q_TERRAIN[{i}][{j}] = {qterrain[i][j]}\n""") 

    for i in range(length-1) :
        for j in range(width) :
            fslope[i][j] = random.uniform(-1,1)
            mFile.write(f"""// F_SLOPE[{i}][{j}] =  {fslope[i][j]}\n""")

    for i in range(length-1) :
        for j in range(width) :
            lslope[i][j] = random.uniform(-1,1)
            mFile.write(f"""// S_SLOPE[{i}][{j}]   =  {lslope[i][j]}\n""")
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
""")
    mFile.close()


# Definition of Roborta
def writeRoborta(fileName) :
    global fslope
    global lslope
    global qterrain
    global width
    global length
    mFile = open(fileName, "a")
    mFile.write(f"""
// Module for Roborta, in case a collision the games finishes
module roborta
    robcol : [0..{width-1}] init 0;
    robrow : [0..{length}] init 0;\n""")
    
    # probabilistic choices
    for i in range(0,length) :
        for j in range(0,width-1) :
            mFile.write(f"""    [robl] (sched = 0) & robrow ={i} & robcol={j} & !Collision  -U->""")
            mFile.write(f"""
        pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
        pr : (robcol' = {min(width-1, j+1)}) & (sched' = 1) +   // corresponds to environments setbacks
        pf : (robrow' = {i}+1) & (sched' = 1) +
        pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
        pm : (robcol' = {max(0, j-1)}) & (sched' = 1)            // Command successful
        """)
            mFile.write("{");
            mFile.write(f"""
        // Success is affected by the quality and inclination of the terrain
        {1 - ((qterrain[j][i] + (1 - (1 - abs(lslope[j][i]))) * (1 - abs(fslope[j][i]))) / 2)} >= pm,
        // The following are the conditions for the probabilities of sideways and frontal displacements
        {(1-max(0,-lslope[j][i]))} * pl - {(1 - qterrain[j][i]) * (1 - max(0,lslope[j][i]))} * pr >= 0,
        {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1 - max(0,-lslope[j][i]))} * pl >= 0,
        {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1 - max(0,-fslope[j][i]))} * pb >= 0,
        {1-max(0,-fslope[j][i])} * pb - {(1 - qterrain[j][i]) * (1 - max(0,fslope[j][i]))} * pf >= 0
        """)
            mFile.write("};\n")

    # second action
            if (i < length) :
                mFile.write(f"""    [robr] (sched = 0) & robrow = {i} & robrow = {j} & !Collision  -U->""")
                mFile.write(f"""
        pl : (robcol' = {max(0, j-1)} & (sched' = 1) +         // The first four probabilistic options
        pr : (robcol' = {min(width-1, j+1)} & (sched' = 1) +   // corresponds to environments setbacks
        pf : (robrow' = {i+1} & (sched' = 1) +
        pb : (robrow' = {max(0, i+1)} & (sched' = 1) +
        pm : (robcol' = {min(width-1, j+1)} & (sched' = 1)     // Command successful
        """)
                mFile.write("{")
                mFile.write(f"""    
        // Success is affected by the quality and inclination of the terrain
        {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i]))) * (1-abs(fslope[j][i]))) / 2} >= pm,
        // The following are the conditions for the probabilities of sideways and frontal displacements
        {(1-max(0,-lslope[j][i]))} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
        {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
        {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1- max(0,-fslope[j][i]))} * pb >= 0,
        {(1-max(0,-fslope[j][i]))} * pb - {(1 - qterrain[j][i]) * (1 - fslope[j][i])} * pf >= 0 
        """)
                mFile.write("};")          
            # third action
            if (i < length) :
                mFile.write(f"""
        [robf] (sched = 0) &  robrow = {i} & robrow = {j}  & !Collision  -U->
        pl : (robcol' = {max(0, j-1)}) & (sched' = 1) +         // The first four probabilistic options
        pr : (robcol' = {min(j-1, i+1)}) & (sched' = 1) +   // corresponds to environments setbacks
        pf : (robrow' = {i+1}) & (sched' = 1) +
        pb : (robrow' = {max(0, i-1)}) & (sched' = 1) +
        pm : (robrow' = {i+1}) & (sched' = 1)                   // Command successful 
        """)
                mFile.write("{")
                mFile.write(f"""
        // Success is affected by the quality and inclination of the terrain
        {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i])) * (1-abs(fslope[j][i]))) / 2)} >= pm,
        // The following are the conditions for the probabilities of sideways and frontal displacements
        ({ (1-max(0,-lslope[j][i])) } * pl) - ({ (1 - qterrain[j][i]) * (1-max(0,lslope[j][i])) } * pr) >= 0,
        ({ (1-max(0,lslope[j][i])) } * pr) - ({ (1 - qterrain[j][i]) * (1-max(0,-lslope[j][i])) } * pl) >= 0,
        ({ (1-max(0,fslope[j][i])) } * pf) - ({ (1 - qterrain[j][i]) * (1- max(0,-fslope[j][i])) } * pb) >= 0,
        ({ (1-max(0,-fslope[j][i]))} * pb) - ({ (1 - qterrain[j][i]) * (1-max(0,fslope[j][i])) } * pf) >= 0 
        """)     
            
                mFile.write("};\n")
            #fourth action
            if (i < length) :
                mFile.write(f"""
        [robb] (sched = 0) &  robrow = {i} & robrow = {j}  & !Collision  -U->
        pl : (robcol' = {max(0, j-1)} & (sched' = 1) +         // The first four probabilistic options
        pr : (robcol' = {min(width-1, j+1)} & (sched' = 1) +   // corresponds to environments setbacks
        pf : (robrow' = {i+1} & (sched' = 1) +
        pb : (robrow' = {max(0, i-1)} & (sched' = 1) +
        pm : (robrow' = {max(0, i+1)} & (sched' = 1)           // Command successful
        """)
                mFile.write("{")
                mFile.write(f"""
        // Success is affected by the quality and inclination of the terrain
        {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i]))) * (1-abs(fslope[j][i])) / 2)} >= pm,
        // The following are the conditions for the probabilities of sideways and frontal displacements
        {(1-max(0,-lslope[j][i]))} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
        {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
        {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1-max(0,-fslope[j][i]))} * pb >= 0,
        {(1-max(0,-fslope[j][i]))} * pb - {(1 - qterrain[j][i]) * (1-max(0,fslope[j][i]))} * pf >= 0
        """)
                mFile.write("};\n") 
    mFile.write("endmodule\n")
    mFile.close()       
    
# Definition of Rigoborto
def writeRigoborto(fileName) :
    global width
    global length
    mFile = open(fileName, "a")
    mFile.write(f""" // Module for Rigoborto, in case a collision the games finishes
            module rigoborto
            rigcol : [0..{width}-1] init 0;
            rigrow : [0..{length}] init 0;
            """)
    for i in range(0,length) :
        for j in range(0,width-1) :

        # action one
            mFile.write(f"""
                [rigl] (sched = 0) & robrow = {i} & robrow = {j} & !Collision  -U->
                pl : (rigcol' = { max(0, j-1)} & (sched' = 0) +         // The first four probabilistic options
                pr : (rigcol' = {min(width-1, j+1)} & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {min(length-1, i+1)} & (sched' = 0) +
                pb : (rigrow' = {max(0, i-1)} & (sched' = 0) +
                pm : (rigcol' = {max(0, j-1)} & (sched' = 0)            // Command successful
                """)
            mFile.write("{\n")
            mFile.write(f"""
                // Success is affected by the quality and inclination of the terrain
	            {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i]))) * (1-abs(fslope[j][i])) / 2)} >= pm,
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {(1-max(0,-lslope[j][i]))} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
	            {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
	            {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1-max(0,-fslope[j][i]))} * pb >= 0,
	            {(1-max(0,-fslope[j][i]))} * pb - {(1 - qterrain[j][i]) * (1-max(0,fslope[j][i]))} * pf >= 0 
            """)
            mFile.write("};")    

            #action two
            mFile.write(f""" 
                [rigr] (sched = 0) &  robrow = {i} & robrow = {j} & !Collision  -U->
                pl : (rigcol' = {max(0, j-1)} & (sched' = 0) +         // The first four probabilistic options
                pr : (rigcol' = {min(width-1, j+1)}) & (sched' = 0) +   // corresponds to environments setbacks
                pf : (rigrow' = {min(length-1, length+1)} & (sched' = 0) +
                pb : (rigrow' = {max(0, length-1)}) & (sched' = 0) +
                pm : (rigcol' = {min(width-1, j+1)} & (sched' = 0)     // Command successful 
                """)
            mFile.write("{\n")
            mFile.write(f""" 
                // Success is affected by the quality and inclination of the terrain
	            {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i]))) * (1-abs(fslope[j][i])) / 2)} >= pm,
	            // The following are the conditions for the probabilities of sideways and frontal displacements
	            {(1-max(0,-lslope[j][i]))} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
	            {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
	            {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1-max(0,-fslope[j][i]))} * pb >= 0,
	            {(1-max(0,-fslope[j][i]))} * pb - {(1 - qterrain[j][i]) * (1-max(0,fslope[j][i]))} * pf >= 0
                """)
            mFile.write("};\n")

            #action three
            mFile.write(f"""
            [rigf] (sched = 0) & !Collision  -U->
            pl : (rigcol' = {max(0, j-1)} & (sched' = 0) +         // The first four probabilistic options
            pr : (rigcol' = {min(width-1, j+1)} & (sched' = 0) +   // corresponds to environments setbacks
            pf : (rigrow' = {min(length-1, i+1)} & (sched' = 0) +
            pb : (rigrow' = {max(0, i-1)} & (sched' = 0) +
            pm : (rigrow' = {min(length-1, i+1)} & (sched' = 0)    // Command successful
            """)
            mFile.write("{")
            mFile.write(f"""
            // Success is affected by the quality and inclination of the terrain
	        {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i])) * (1-abs(fslope[j][i]))) / 2)} >= pm,
	        // The following are the conditions for the probabilities of sideways and frontal displacements
	        {1-max(0,-lslope[j][i])} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
	        {(1-max(0,lslope[j][i]))} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
	        {(1-max(0,fslope[j][i]))} * pf - {(1 - qterrain[j][i]) * (1-max(0,-fslope[j][i]))} * pb >= 0,
	        {(1-max(0,-fslope[j][i]))} * pb - {(1 - qterrain[j][i]) * (1-max(0,fslope[j][i]))} * pf >= 0
            """)

            #action fourth
            mFile.write(f"""
            [rigb] (sched = 0) & !Collision  -U->
            pl : (rigcol' = {(max(0, j-1))} & (sched' = 0) +         // The first four probabilistic options
            pr : (rigcol' = {(min(width-1, j+1))} & (sched' = 0) +   // corresponds to environments setbacks
            pf : (rigrow' = {(min(length-1, i+1))} & (sched' = 0) +
            pb : (rigrow' = {max(0, i-1)} & (sched' = 0) +
            pm : (rigrow' = {max(0, i-1)} & (sched' = 0)           // Command successful
            """)
            mFile.write("{")
            mFile.write(f"""
	        // Success is affected by the quality and inclination of the terrain
	        {1 - (qterrain[j][i] + (1 - (1-abs(lslope[j][i])) * (1-abs(fslope[j][i]))) / 2)} >= pm,
	        // The following are the conditions for the probabilities of sideways and frontal displacements
	        {1-max(0,-lslope[j][i])} * pl - {(1 - qterrain[j][i]) * (1-max(0,lslope[j][i]))} * pr >= 0,
	        {1-max(0,lslope[j][i])} * pr - {(1 - qterrain[j][i]) * (1-max(0,-lslope[j][i]))} * pl >= 0,
	        {1-max(0,fslope[j][i])} * pf - {(1 - qterrain[j][i]) * (1-max(0,-fslope[j][i]))} * pb >= 0,
	        {1-max(0,-fslope[j][i])} * pb - {(1 - qterrain[j][i]) * (1-max(0,fslope[j][i]))} * pf >= 0
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

    # default value for the arguments
    width = 2
    length = 2
    seed = 0
    path = ""

    try:
        opts, args = getopt.getopt(argv,"hs:w:l:",["seed=","width=","length="])
    except getopt.GetoptError:
        usage(2)
    for opt, arg in opts:
        if opt == '-h':
            usage(0)
        elif opt in ("-s","seed=") :
            try :
                seed = int(arg)
            except ValueError :
                print("The width must be a nonnegative integer")
                sys.exit(2)            
            if seed < 0 :
                print("The width must be a nonnegative integer")
                sys.exit(2)
        elif opt in ("-w","width=") :
            try :
                width = int(arg)
            except ValueError :
                print("The width must be a positive integer")
                sys.exit(2)            
            if width <= 0 :
                print("The width must be a positive integer")
                sys.exit(2)
        elif opt in ("-l","length=") :
            try :
                length = int(arg)
            except ValueError :
                print("The length must be a positive integer")
                sys.exit(2)            
            if length <= 0 :
                print("The length must be a positive integer")
                sys.exit(2)

    genBoard(seed,path+f"RobRig_{seed}-{width}-{length}.smg")
    writeHeading(path+f"RobRig_{seed}-{width}-{length}.smg")
    writeRoborta(path+f"RobRig_{seed}-{width}-{length}.smg")
    writeRigoborto(path+f"RobRig_{seed}-{width}-{length}.smg")
    #genRndBoard(seed)

    #writeRobotA(path+"roborta_A["+str(seed)+"-"+str(width)+"-"+str(length)+"].smg")
    #writeRobotB(path+"roborta_B["+str(seed)+"-"+str(width)+"-"+str(length)+"].smg")
    #writeRobotC(path+"roborta_C["+str(seed)+"-"+str(width)+"-"+str(length)+"].smg")



main(sys.argv[1:])