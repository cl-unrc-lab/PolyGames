
import roborta_gen
import sys
"""
A basic script for generating the benchmark of the Roborta vs Rigoborto example.
It generates the benchmark and 
"""

# this function generates the benchmark, l and w are the maximum length and width
# parameter l : the maximum length of the grid to be generated
# parameter w : the maximum width of th grid to be generated
# Given (l,w)  the script generates grids for (2,2), (2,3) ... (3,2) ... etc
def generateBenchmark(seed, l, w, reward) :
    if reward :
        path = "benchmark-reward/"
    else :
        path = "benchmark/"
    #global length, width, rob_x, rob_y, rig_x, rig_y 
    # we generate 4x4 cases
    # first with the robots in the corners, the case 1x1 has no sense
    for length in range(2,l) :
        for width in range(2,w) :
            rob_x, rob_y, rig_x, rig_y = 0, 0, width-1,length-1 
            #for rob_x, rob_y in [(0,y) for y in range(0, width)] : 
                #for rig_x, rig_y in [(width-1, length-n) for n in range(1,length)] :
            if reward :
                roborta_gen.main(["-s 0","-rewards",f'--width={width}', f'--length={length}', f'--rob-init={rob_x},{rob_y}', f'--rig-init={rig_x},{rig_y}', "--path=benchmark/"])
            else :
                roborta_gen.main(["-s 0",f'--width={width}', f'--length={length}', f'--rob-init={rob_x},{rob_y}', f'--rig-init={rig_x},{rig_y}', "--path=benchmark/"])


def generatePaperBenchmark(reward) :
    """ This function generates the results shown in the tables of the paper, and saves the results
        to the folder table-results 
    """
    if reward :
        path = "paper-benchmark-rewards/"
    else :
        path = "paper-benchmark/"
    seed = 0
    for s in range(2,16) :
        rob_x, rob_y, rig_x, rig_y = 0, 0, s-1,s-1 
        if reward :
            roborta_gen.main(["-s 0","--rewards",f'--width={s}', f'--length={s}', f'--rob-init={rob_x},{rob_y}', f'--rig-init={rig_x},{rig_y}', f'--path={path}'])
        else :
            roborta_gen.main(["-s 0",f'--width={s}', f'--length={s}', f'--rob-init={rob_x},{rob_y}', f'--rig-init={rig_x},{rig_y}', f'--path={path}'])


# this function generates the benchmark for the diagonal, given a size it generates a grid, and 
# different versions of roborta vs rigoborto placed in the diagonal
def generateDiagonal(seed, width, length, batches, reward) :
    if reward :
        path = "diagonal-rewards"
    else :
        path = "diagonal"
    # assert width == length - 1, 'for generating the grid has to be a square'
    if reward :
        roborta_gen.main(["-s 0","-rewards", f'--width={width}', f'--length={length}', "--path=diagonal/", f'--diagonal={batches}']) 
    else :
        roborta_gen.main(["-s 0",f'--width={width}', f'--length={length}', "--path=diagonal/", f'--diagonal={batches}']) 
     

if __name__ == "__main__" :
    reward = False
    try :
        arg = sys.argv[1] 
        rew = sys.argv[2]
        assert arg in ["benchmark", "diagonal", "paper-benchmark"]
        assert rew in ["rewards","no-rewards"]
        reward = (rew == "rewards")
    except : 
        print("""
        error reading the parameter.
            Usage: python gen_bench <option> <rewards>
            where <option> in [benchmark, diagonal, paper-benchmark]
                  <rewards> in [rewards,no-rewards]
        """)
        sys.exit()
#generateBenchmark(0, 15, 15) # we generate benchmark up to size 15
#generateDiagonal(0, 6, 6, 20) # we generate the diagonal with a grid of size 6, and 10 batches for each initial position
    if arg == "benchmark" :
        generateBenchmark(0, 15, 15, reward)
    if arg == "diagonal" :
        generateDiagonal(0, 6, 6, 20, reward)
    if arg == "paper-benchmark" :
        generatePaperBenchmark(reward)
