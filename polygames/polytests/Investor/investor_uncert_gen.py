"""
    A simple script for generating different instances of the investor with uncertainty
"""
import pandas as pd
import os, sys, subprocess, csv
import matplotlib.pyplot as plt


def gen_spec(filename) :
    """ 
        This function generates the spec and writes it down in the given file
    """
    global uf_up
    global uf_down
    global share_max_val
    global share_init
    mFile = open(filename,  "w") 
    mFile.write(f"""
        smg

        // Player info
        player investor [invest], [noinvest], [cashin], [done] endplayer
        player env [bar], [nobar], [month] endplayer

        // Scheduler use to synchronise system transitions
        module sched
            
            m : [0..2];

            // At the start of the month, investor makes decision
            [noinvest] (m=0) -> (m'=1); 
            [invest] (m=0) -> (m'=1);
            // Then, decision is made whether to bar or not
            [bar] m=1 -> (m'=2);
            [nobar] m=1 -> (m'=2);
            // Then, market changes
            [month] m=2 -> (m'=0);
            // Once investor has cashed in shares nothing changes
            [cashin] m=0 -> (m'=0);
            [done] m=0 -> (m'=0);

        endmodule

        // Investor
        module investor
            
            // State: 0 = no reservation, 1 = made reservation, 2 = finished
            i : [0..2];

            // Decide whether to do nothing or make reservation
            // (if currently not reserving or was barred last time)
            [noinvest] (i=0 | i=1 & b=1) -> (i'=0);
            [invest] (i=0 | i=1 & b=1) -> (i'=1);
            // Cash in shares (if not barred)
            [cashin] i=1 & b=0 -> (i'=2);
            // Finished
            [done] (i=2) -> true;

        endmodule

        // Bar on the investor
        module barred
            
            // State: 0 = not barred, 1 = barred
            // (initially cannot bar)
            b : [0..1] init 1;

            // Bar or not bar (cannot if do so last month)
            [nobar] true -> (b'=0);
            [bar] b=0 -> (b'=1);

        endmodule

        // Value of the shares
        const int vmax = {share_max_val};
        const int vinit= {share_init};
        module value
            
            v : [0..vmax] init vinit;

            [month] true -> p/10 : (v'=min(v+1,c,vmax)) + (1-p/10) : (v'=min(max(v-1,0),c));

            // Note that, because the shares and the cap are updated simultaneously,
            // v can exceed c temporarily (but by at most 1).
            // We leave this as-is for compatibility with the original model

        endmodule
        // Probability of shares going up/down
        const int pmax = 10;
        module probability
            
            // Probability is p/pmax and initially the probability is approx 1/2
            p : [0..pmax] init floor(pmax/2);
        """)
    
    for v in range(share_max_val+1) :
        if (v < 5 ) :
            mFile.write(f"""
                        [month] (v = {v}) -U-> p1 : (p'=min(p+1,pmax)) + p2 : (p'=max(p-1,0))
                        {{  
                        p2+p1 = 1,     
                        p1 >= {2/3 - (uf_up*((10-v)/10))}, 
                        p1 <= {2/3 + (uf_up*((10-v)/10))}
                        }};
            """)
        if (v == 5) :
            mFile.write(f"""
                        [month] (v = {v}) -U-> p1 : (p'=min(p+1,pmax)) + p2 : (p'=max(p-1,0))
                        {{  
                        p2 + p1 = 1,     
                        p1 >= {1/2 - (uf_up*((10-v)/10))} , 
                        p1 <= {1/2 + (uf_down*((10-v)/10))}
                        }};
            """)
        if (v > 5) :
            mFile.write(f"""
                        [month] (v = {v}) -U-> p1 : (p'=min(p+1,pmax)) + p2 : (p'=max(p-1,0))
                        {{  
                        p2+p1=1,     
                        p2 >= {2/3 - (uf_down*(v/10))}, 
                        p2 <= {2/3 + (uf_down*((10-v)/10))}
                        }};
            """)
    mFile.write("""
        endmodule
        // Cap on the value of the shares
        const int cmax = vmax;
        module cap
            
            c : [0..cmax] init cmax;

            [month] true -> 1/2 : (c'=max(c-1,0)) + 1/2 : (c'=c); // probability 1/2 the cap decreases

        endmodule

        // Labels
        label "finished" = i=2;

        // Reward: one-off collection of shares value at the end
        rewards "profit"

            // Use state rewards:
            i=1 & b=0 & m=0 : v;
            
            // Could also use transition rewards
            // [cashin] i=1 : v;
            
        endrewards
            """
    )
    mFile.close()

def gen_benchmark() :
    """
    this function generates the benchmark used in the paper
    """
    global uf_up
    global uf_down
    global share_max_val
    global share_init
    ufs = [0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12,0.13,0.14,0.15]
    uf_down = 0
    for i in ufs :
        uf_up = i
        filename = f"""Benchmark/investor-{share_max_val}-{share_init}-uf-up-{i}.prism"""
        gen_spec(filename)
    uf_up = 0
    for i in ufs :
        uf_down = i
        filename = f"""Benchmark/investor-{share_max_val}-{share_init}-uf-down-{i}.prism"""
        gen_spec(filename)

def run_benchmark() :
    """
    This function runs the benchmark and saves the corresponding data
    """
    rowlist_uf_down = [] # this saves the data obtained when there is uncertainty about the shares going up
    rowlist_uf_up = []
    for file in os.listdir("Benchmark/") :  
        row = {}  # a rwo corresponding to this file 
        result = subprocess.run(['../../bin/polygames', "Benchmark/"+file, 'investor.props'], capture_output=True).stdout.decode()
        # print(result)
        for line in result.splitlines() : 
            #print(line)
            words = line.split()
            if line.startswith("States:") :
                row["states"] = words[1] #we add the property checked to the dictionary
            elif line.startswith("Transitions:") : 
                row["transitions"] = words[1]
            elif line.startswith("Value in the initial state:") :
                row["value"] = words[5]
            elif line.startswith("Time for model checking:") :
                row["time"] = words[4]
        txt = file.split('-')
        row["factor"] = txt[5].strip(".prism")
        if "uf-down" in file :   
            rowlist_uf_down.append(row)
        if "uf-up" in  file :
            rowlist_uf_up.append(row)
    results_down = pd.DataFrame(rowlist_uf_down)
    results_down.sort_values("factor", axis=0, ascending=True, inplace=True, na_position='last')
    results_up = pd.DataFrame(rowlist_uf_up)
    results_up.sort_values("factor", axis=0, ascending=True, inplace=True, na_position='last')
    results_up = results_up.astype(float)
    results_down = results_down.astype(float)
    #print(results_up)
    #print(results_down)
    results_up.info()
    results_down.info()
    results_up.plot(x="factor", y="value")
    results_down.plot(x="factor", y="value")
    plt.show()




# The entry point
if __name__ == "__main__" :
    # we set the default value of the variables
    share_max_val = 10
    share_init = 0
    uf_up = 0.02 # uncertainty factor that affects the shares when thet go up
    uf_down = 0.02 # uncertainty factor that affects the shares when thet go down

    #filename = f"""investor-{share_max_val}-{share_init}.prism"""
    #gen_spec(share_max_val, share_init, filename)
    gen_benchmark()
    run_benchmark()
