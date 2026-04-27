"""
A basic script for running the roborta vs rigoborto example, the script also 
generates the plots
"""
import os, sys, subprocess, csv
import pandas as pd
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

rewards = False
try :
   arg = sys.argv[1] 
   assert arg in ["bounded","plot_bounded", "terrain", "plot_terrain", "subset"]
   if len(sys.argv) > 2 :
      assert sys.argv[2] == "rewards"
      rewards = True 
except : 
   print("""
   error reading the parameter.
   Usage: python gen_bench <option> 
   where <option> in [bounded,terrain,plot_bounded, plot_terrain, subset]
    
   The "bounded" option check the properties RobortavsRigoborto.props, for different number of steps.
   The "terrain" property checks the property <<p2>>Pmax=?[F Rigwins]with diferent instances of terrains, as explained in the paper
   The subset options only runs the tool for a given small subset for rapid testing
   Note that if you run again the script the shown results may be different, scince the scenarios are randomly generated.
   The other options are for plotting the results.      
   """)
   sys.exit()
inputdir = ""
if rewards :
   inputdir = inputdir + "-rewards"

if arg == "bounded" :
    #inputdir = "benchmark"
    #inputdir = "diagonal"
    results = [] # a list of dictionaries
    instances = [5,10,15]
    #instances = [5]
    for instance in instances :
            row = {}  # a row corresponding to this instance 
            print(f"running instance: {instance}x{instance}")
            result = subprocess.run(['../../bin/polygames', "roborta-plain.prims", 'RobortavsRigoborto.props', '-const', f'length={instance},width={instance},lowerb={0},upperb={0.5}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result.stdout)
            #row["size"] = instance
            #row = {}
            for line in result.splitlines() : 
                #print(line)
                words = line.split()
                if line.startswith("Model checking:") :
                    row = {}
                    row["instance"] = instance
                    row["t"] = words[4].replace("F<","")
                elif line.startswith("Time for solving linear equations:") :
                    #row = {}
                    equation_solving = words[5]
                elif line.startswith("Max number of vertices processed:") :
                    vertices_number = words[5]
                elif line.startswith("Time for model construction:") :
                    model_construction = words[4]
                elif line.startswith("States:") :
                    states = words[1] #we add the property checked to the dictionary
                elif line.startswith("Transitions:") : 
                    transitions = words[1]
                elif line.startswith("Value in the initial state:") :
                    row["value"] = words[5]
                elif line.startswith("Time for model checking:") :
                    row["time"] = words[4]
                    row["equation_solving"] = equation_solving
                    row["vertices_number"] =vertices_number
                    row["model_construction"] = model_construction
                    row["states"] = states
                    row["transitions"] = transitions
                    results.append(row) # the row is appened to the list

            #print(results)

    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-bounded.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Results saved to result result-bounded.cvs file.")

if arg == "plot_bounded" :
    # Load the data
    df = pd.read_csv('results-bounded.csv')

    # Define labels for the legend
    labels = {
        5: '5 (5x5 matrix)',
        10: '10 (10x10 matrix)',
        15: '15 (15x15 matrix)'
    }

    # Create the plot
    plt.figure(figsize=(10, 6))
    plt.margins(0)
    for instance in df['instance'].unique():
        subset = df[df['instance'] == instance].copy()
        row = {}
        row["instance"] = instance
        row["t"] = 0
        row["value"] = 0
        row["model construction"] = 0
        row["states"] = 0
        row["transitions"] = 0
        row["time"] = 0
        subset.loc[len(subset)] = row 
        subset = subset.sort_values(by='t')
        plt.plot(subset['t'], subset['value'], marker='o', label=labels.get(instance))

    plt.xlabel('t', fontsize=20)
    plt.ylabel('Pmax=? [F<t(Rigwins)]',fontsize=20)
    plt.title('')
    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.7)
    plt.savefig('plots/rigwins_plot.png')

if arg == "terrain" :
    values = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5]
    pairs = [(x,y) for x in values for y in values if y <= x]
    results = []
    for x,y in pairs :
            print(f"Running interval: [{y},{x}]")
            row = {}  # a row corresponding to this instance 
            result = subprocess.run(['../../../bin/polygames', "roborta-plain.prims",'-pf','<<p2>>Pmax=?[F Rigwins]','-const', f'length={10},width={10},lowerb={y},upperb={x}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result.stdout)
            #row["size"] = instance
            for line in result.splitlines() : 
            #print(line)
                words = line.split()
                if line.startswith("Model checking:") :
                    row = {}
                    row["Lower Bound"] = y
                    row["Upper Bound"] = x
                elif line.startswith("Time for solving linear equations:") :
                    row["equation_solving"] = words[5]
                elif line.startswith("Max number of vertices processed:") :
                    row["vertices_number"] = words[5]
                elif line.startswith("Time for model construction:") :
                    row["model construction"] = words[4]
                elif line.startswith("States:") :
                    row["states"] = words[1] #we add the property checked to the dictionary
                elif line.startswith("Transitions:") : 
                    row["transitions"] = words[1]
                elif line.startswith("Value in the initial state:") :
                    row["value"] = words[5]
                elif line.startswith("Time for model checking:") :
                    row["time"] = words[4]
                    results.append(row) # the row is appened to the list

            #print(results)

    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-terrain.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Results saved to result results-terrain.csv file.")

if arg == "plot_terrain" :

    # Load your csv data
    df = pd.read_csv('results-terrain.csv')

    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    # 3. Create the surface plot with Green-to-Red gradient
    # 'RdYlGn_r' is Red-Yellow-Green reversed, resulting in Green -> Yellow -> Red
    surf = ax.plot_trisurf(df['Lower Bound'], df['Upper Bound'], df['value'], 
                        cmap='RdYlGn_r', edgecolor='none', alpha=0.9)

    # 4. Styling
    ax.set_xlabel('Lower Bound', fontsize=20)
    ax.set_ylabel('Upper Bound', fontsize=20)
    ax.set_zlabel('Value', fontsize=20)
    #ax.set_title('3D Mountain Plot: Green (Lower) to Red (Higher)')
    ax.set_title('')


    # Add color bar to show the value scale
    fig.colorbar(surf, shrink=0.5, aspect=5)

    plt.tight_layout()
    plt.savefig('plots/terrain_plot.png')
    #plt.show()

if arg == "subset" :
    results = [] # a list of dictionaries
    instances = [5]
    #instances = [5]
    for instance in instances :
            row = {}  # a row corresponding to this instance 
            print(f"running instance: {instance}x{instance}")
            result = subprocess.run(['../../bin/polygames', "roborta-plain.prims", 'RobortavsRigoborto.props', '-const', f'length={instance},width={instance},lowerb={0},upperb={0.5}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result.stdout)
            #row["size"] = instance
            #row = {}
            for line in result.splitlines() : 
                #print(line)
                words = line.split()
                if line.startswith("Model checking:") :
                    row = {}
                    row["instance"] = instance
                    row["t"] = words[4].replace("F<","")
                elif line.startswith("Time for solving linear equations:") :
                    #row = {}
                    equation_solving = words[5]
                elif line.startswith("Max number of vertices processed:") :
                    vertices_number = words[5]
                elif line.startswith("Time for model construction:") :
                    model_construction = words[4]
                elif line.startswith("States:") :
                    states = words[1] #we add the property checked to the dictionary
                elif line.startswith("Transitions:") : 
                    transitions = words[1]
                elif line.startswith("Value in the initial state:") :
                    row["value"] = words[5]
                elif line.startswith("Time for model checking:") :
                    row["time"] = words[4]
                    row["equation_solving"] = equation_solving
                    row["vertices_number"] =vertices_number
                    row["model_construction"] = model_construction
                    row["states"] = states
                    row["transitions"] = transitions
                    results.append(row) # the row is appened to the list

            #print(results)

    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-subset.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        print("Results saved to results-subset.csv file.")
        dict_writer.writerows(results)