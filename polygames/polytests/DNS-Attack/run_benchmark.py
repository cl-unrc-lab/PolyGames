"""
A basic script for running the BAA example, the script also 
generates the plots
"""
import os, sys, subprocess, csv
import pandas as pd
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import numpy as np

rewards = False
try :
   arg = sys.argv[1] 
   assert arg in ["subset","model_verification_time_plot","robustness","plot_robustness","poly_vs_prism","strategy_robustness", "strategy_robustness_uncertainty", "strategy_robustness_uncertainty_plot", "strategy_robustness_uncertainty_baseline", "strategy_mimicry_robustness", "strategy_robustness_uncertainty_plot_mimicry", "model_construction_time_plot"]
except : 
   print("""
    error reading the parameter.
    Usage: python gen_bench <option> 
    where <option> in ["model_verification_time_plot","robustness","plot_robustness","poly_vs_prism","strategy_robustness", "strategy_robustness_uncertainty", "strategy_robustness_uncertainty_plot"]
        
    "robustness" option runs the tool with the robustness property, and produces csv for uncertain and standard model      
    "poly_vs_prism" option runs the model checker comparing polygames and prism on this examples, the result are saved in a .csv file
    "subset" runds the tools with a subset of the instances, this is for a rapid testing of the tool,
    "strategy_robustness" runs the tool increasing the unstability and the mimicry factor, the results are saved in a cvs file.
    "strategy_mimicry_robustness" it runs each strategy with increasing values of mimicry to know its behavior,
    "strategy_unstability_robustness" similar as before, but for unstability.
    The rest of the option are for plotting the results.
            
    All the results are saved in the folders cvs/ and plots/
    ""
   """)
   sys.exit()
inputdir = ""
if rewards :
   inputdir = inputdir + "-rewards"

if arg == "robustness" :
    #inputdir = "benchmark"
    #inputdir = "diagonal"
    results = [] # a list of dictionaries
    network_uncertainty = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1]
    mimicry_factor = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1]
    for n in network_uncertainty :
        for m in mimicry_factor :
            row = {}  # a row corresponding to this instance 
            print(f"running instance:  network uncertainty:{n} - mimicry factor{m}")
            result = subprocess.run(['../../bin/polygames', "uncertainBAA/uncertain-baa.prism", 'uncertainBAA/attacker.props', '-const', f'network_unreliability={n},mimicry_capability={m},maxTime=15,nofix=1,ftr=1,rnd=1,agr=1,agf=1,rdr=1', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result)
            #row["size"] = instance
            row = {}
            for line in result.splitlines() : 
            #print(line)
                words = line.split()
                if line.startswith("Model checking:") : 
                    row["network uncertainty"] = n
                    row["mimicry_factor"] = m
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
    with open(f'results-uncertain-robustness.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Results saved to results-uncertain-robustness.csv file.")

if arg == "plot_robustness" :
    # Load the data
    df = pd.read_csv('csv/results-uncertain-robustness.csv')

    # Create a meshgrid for the X and Y axes
    X_unique = sorted(df['network uncertainty'].unique())
    Y_unique = sorted(df['mimicry_factor'].unique())
    X, Y = np.meshgrid(X_unique, Y_unique)

    # Pivot the data to get Z (Game Value) in the correct grid format
    Z = df.pivot(index='mimicry_factor', columns='network uncertainty', values='value').values

    # Create the 3D plot
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    # Plot the surface
    surf = ax.plot_surface(X, Y, Z, cmap='RdYlGn_r', edgecolor='k', alpha=0.8, linewidth=0.5)

    # Labeling
    ax.set_xlabel('Network Unreliability', fontsize=20)
    ax.set_ylabel('Mimicry Factor', fontsize=20)
    ax.set_zlabel('Game Value', fontsize=20)
    ax.set_title('', fontsize=14, pad=20)

    # Add a color bar to represent the value scale
    fig.colorbar(surf, ax=ax, shrink=0.5, aspect=10)

    # Adjust viewing angle for clarity
    #ax.view_init(elev=25, azim=-135)

    plt.tight_layout()
    #plt.show()
    plt.savefig("Robustness.png", dpi=300)

# we measure for each strategy its robustness wrt network unreliability 
if arg == "strategy_robustness_uncertainty" :
    network_uncertainty = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1]
    results = [] # a list of dictionaries
    for n in network_uncertainty :
        for nofix, ftr, rnd, agr, agf, rdr in [(1,0,0,0,0,0),(0,1,0,0,0,0),(0,0,1,0,0,0),(0,0,0,1,0,0),(0,0,0,0,1,0), (0,0,0,0,0,1)] :
            row = {}  # a row corresponding to this instance 
            print(f"running instance:  network uncertainty:{n} - strat:{nofix,ftr,rnd,agr,agf,rdr}")
            result = subprocess.run(['../../bin/polygames', "uncertainBAA/uncertain-baa.prism", 'uncertainBAA/attacker.props', '-const', f'network_unreliability={n},mimicry_capability={0.1},maxTime=15,nofix={nofix},ftr={ftr},rnd={rnd},agr={agr},agf={agf},rdr={rdr}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result)
            #row["size"] = instance
            row = {}
            for line in result.splitlines() : 
            #print(line)
                words = line.split()
                if line.startswith("Model checking:") :
                    #row = {}
                    row["network uncertainty"] = n
                    row["mimicry_factor"] = 0
                    if nofix == 1 : 
                        row["strat"] = "nofix"
                    elif ftr == 1 :
                        row["strat"] = "ftr"
                    elif rnd == 1 :
                        row["strat"] = "rnd"
                    elif agr == 1 :
                        row["strat"] = "agr"
                    elif agf == 1 :
                        row["strat"] = "agf"
                    elif rdr == 1 :
                        row["strat"] = "rdr"
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

    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-strategy-robustness-uncertainty.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Result saved to results-strategy-robustness-uncertainty.csv")

# we measure for each strategy its robustness wrt network mimicry 
if arg == "strategy_mimicry_robustness" :
    mimicry = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1]
    results = [] # a list of dictionaries
    for m in mimicry :
        for nofix, ftr, rnd, agr, agf, rdr in [(1,0,0,0,0,0),(0,1,0,0,0,0),(0,0,1,0,0,0),(0,0,0,1,0,0),(0,0,0,0,1,0), (0,0,0,0,0,1)] :
            row = {}  # a row corresponding to this instance 
            print(f"running instance:  mimicry:{m} - strat:{nofix,ftr,rnd,agr,agf,rdr}")
            result = subprocess.run(['../../bin/polygames', "uncertainBAA/uncertain-baa.prism", 'uncertainBAA/attacker.props', '-const', f'network_unreliability={0.1},mimicry_capability={m},maxTime=15,nofix={nofix},ftr={ftr},rnd={rnd},agr={agr},agf={agf},rdr={rdr}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
            #print(result)
            #row["size"] = instance
            for line in result.splitlines() : 
            #print(line)
                words = line.split()
                if line.startswith("Model checking:") :
                    row = {}
                    row["network uncertainty"] = 0
                    row["mimicry_factor"] = m
                    if nofix == 1 : 
                        row["strat"] = "nofix"
                    elif ftr == 1 :
                        row["strat"] = "ftr"
                    elif rnd == 1 :
                        row["strat"] = "rnd"
                    elif agr == 1 :
                        row["strat"] = "agr"
                    elif agf == 1 :
                        row["strat"] = "agf"
                    elif rdr == 1 :
                        row["strat"] = "rdr"
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
    with open(f'csv/results-strategy-robustness-mimicry.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)

if arg == "strategy_robustness_uncertainty_plot" :
    # Load CSV
    df = pd.read_csv("csv/results-strategy-robustness-uncertainty.csv")

    df = df.sort_values(by="network uncertainty")

    strategies = df["strat"].unique()

    style_map = {
        "nofix": ("-", "o", 1.5),   # thick solid line
        "ftr": ("-", "s", 1.5),
        "rnd": ("-", "^", 1.5),
        "agr": ("-", "D", 1.5),
        "agf": ("-", "x", 1.5),
        "rdr": ("-", "*", 1.5)
    }
    plt.ylim(0.7, 2.5)
    plt.figure(figsize=(8, 5))

    for i, strat in enumerate(strategies):
        subset = df[df["strat"] == strat]
        
        linestyle, marker, width = style_map[strat]
    
        plt.plot(
            subset["network uncertainty"],
            subset["value"],
            linestyle=linestyle,
            marker=marker,
            linewidth=width,
            label=strat,
            alpha=0.7
        )

    plt.xlabel("network_unreliability",fontsize=20)
    plt.ylabel("Value",fontsize=20)
    plt.title("")
    plt.legend(title="")
    plt.grid(True)
    plt.tight_layout()
    #plt.show()
    plt.savefig("plots/strat-robustness-network.png")

# 
if arg == "strategy_robustness_uncertainty_plot_mimicry" :

    # Load CSV
    df = pd.read_csv("csv/results-strategy-robustness-mimicry.csv")

    df = df.sort_values(by="mimicry_factor")

    strategies = df["strat"].unique()

    style_map = {
        "nofix": ("-", "o", 1.5),   # thick solid line
        "ftr": ("-", "s", 1.5),
        "rnd": ("-", "^", 1.5),
        "agr": ("-", "D", 1.5),
        "agf": ("-", "x", 1.5),
        "rdr": ("-", "*", 1.5)
    }
    plt.ylim(0.7, 2.5)
    plt.figure(figsize=(8, 5))

    for i, strat in enumerate(strategies):
        subset = df[df["strat"] == strat]
        
        linestyle, marker, width = style_map[strat]
    
        plt.plot(
            subset["mimicry_factor"],
            subset["value"],
            linestyle=linestyle,
            marker=marker,
            linewidth=width,
            label=strat,
            alpha=0.7
        )

    plt.xlabel("mimicry_factor", fontsize=20)
    plt.ylabel("Value", fontsize=20)
    plt.title("")
    plt.legend(title="")
    plt.grid(True)
    plt.tight_layout()
    #plt.show()
    plt.savefig('plots/strat-robustness-mimicry.png')
   

# plots similarly as the case above using nofix as baseline
if arg == "strategy_robustness_uncertainty_baseline" :
    df = pd.read_csv("results-strategy-robustness-uncertainty.csv")
    # Sort for clean plotting
    df = df.sort_values(by="network uncertainty")
    baseline = df[df["strat"] == "nofix"][["network uncertainty", "value"]]
    baseline = baseline.rename(columns={"value": "baseline"})
    df = df.merge(baseline, on="network uncertainty")
    df["diff"] = df["value"] - df["baseline"]
    styles = ['-', '--', '-.', ':', '-', '--']
    markers = ['o', 's', '^', 'D', 'x', '*']

    strategies = df["strat"].unique()

    plt.figure(figsize=(8, 5))

    for i, strat in enumerate(strategies):
        if strat == "nofix":
            continue  # skip baseline (always 0)
        
        subset = df[df["strat"] == strat]
        
        plt.plot(
            subset["network uncertainty"],
            subset["diff"],
            linestyle=styles[i % len(styles)],
            marker=markers[i % len(markers)],
            linewidth=2,
            label=strat
        )

    # Horizontal zero line 
    plt.axhline(0, linestyle='--', linewidth=1)

    plt.xlabel("Network Uncertainty")
    plt.ylabel("Difference from NoFix")
    plt.title("Strategy Improvement over NoFix")

    plt.legend(title="Strategy")
    plt.grid(True)
    plt.tight_layout()

    plt.show()


# we compare the standard case against poly
if arg == "poly_vs_prism" :
    instances = [5,6,7,8,9,10,11,12,13,14,15]
    results = []
    # the standard case
    for i in instances :
        print(f"running instance:{i}x{i}")
        result = subprocess.run(['../../bin/polygames', "BAA/baa.prism", 'uncertainBAA/attacker.props', '-const', f'maxTime={i}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
        row = {}  # a row corresponding to this instance 
        #print(result)
        row["size"] = i
        row["version"] = "prism"
        for line in result.splitlines() : 
            #print(line)
            words = line.split()
            if line.startswith("Model checking:") :
                #row = {}
                pass
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
        keys = results[0].keys()

        # the results are saved in a file
        with open(f'results-prism-vs-poly.csv', 'w', newline='') as output_file:
            dict_writer = csv.DictWriter(output_file, keys)
            dict_writer.writeheader()
            dict_writer.writerows(results)
        row = {}  # a row corresponding to this instance 
        result = subprocess.run(['../../bin/polygames', "uncertainBAA/uncertain-baa.prism", 'uncertainBAA/attacker.props', '-const', f'network_unreliability=0.5,mimicry_capability=0.5,maxTime={i},nofix=1,ftr=1,rnd=1,agr=1,agf=1,rdr=1', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
        #print(result)
        row["size"] = i
        row["version"] = "poly"
        for line in result.splitlines() : 
            #print(line)
            words = line.split()
            if line.startswith("Model checking:") :
                #row = {}
                pass
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
    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-prism-vs-poly.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Result saved to results-prism-vs-poly.csv file.")


if arg == "model_construction_time_plot" :
    # Create DataFrame
    df = pd.read_csv("results-uncertain-robustness.csv")

    # Create grid for the plot
    x = df['network uncertainty'].unique()
    y = df['mimicry_factor'].unique()
    X, Y = np.meshgrid(x, y)

    # Reshape Z data (model construction)
    Z = np.zeros(X.shape)
    for i in range(len(x)):
        for j in range(len(y)):
            val = df[(df['network uncertainty'] == x[i]) & (df['mimicry_factor'] == y[j])]['model construction']
            if not val.empty:
                Z[j, i] = val.iloc[0]

    # Plotting
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')
    surf = ax.plot_surface(X, Y, Z, cmap='RdYlGn_r', edgecolor='none', alpha=0.9)

    # Labels
    ax.set_xlabel('Network Unreliability', fontsize=20)
    ax.set_ylabel('Mimicry Factor', fontsize=20)
    ax.set_zlabel('Model Construction Time (s)', fontsize=20)
    ax.set_title('')

    # Colorbar
    fig.colorbar(surf, ax=ax, shrink=0.5, aspect=10, label='Construction Time (s)')

    plt.tight_layout()
    plt.savefig('ModelConstruction.png')

if arg == "model_verification_time_plot" :
    # Create DataFrame
    df = pd.read_csv("results-uncertain-robustness.csv")

    # Create grid for the plot
    x = df['network uncertainty'].unique()
    y = df['mimicry_factor'].unique()
    X, Y = np.meshgrid(x, y)

    # Reshape Z data (model construction)
    Z = np.zeros(X.shape)
    for i in range(len(x)):
        for j in range(len(y)):
            val = df[(df['network uncertainty'] == x[i]) & (df['mimicry_factor'] == y[j])]['time']
            if not val.empty:
                Z[j, i] = val.iloc[0]

    # Plotting
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')
    surf = ax.plot_surface(X, Y, Z, cmap='RdYlGn_r', edgecolor='none', alpha=0.9)

    # Labels
    ax.set_xlabel('Network Unreliability', fontsize=20)
    ax.set_ylabel('Mimicry Factor', fontsize=20)
    ax.set_zlabel('Model Verification Time (s)', fontsize=20)
    ax.set_title('')

    # Colorbar
    fig.colorbar(surf, ax=ax, shrink=0.5, aspect=10, label='Construction Time (s)')

    plt.tight_layout()
    plt.savefig('ModelVerification.png')

if arg == "subset" :
    instances = [5,6,7]
    results = []
    # the standard case
    for i in instances :
        print(f"running instance: {i}")
        result = subprocess.run(['../../bin/polygames', "BAA/baa.prism", 'uncertainBAA/attacker.props', '-const', f'maxTime={i}', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
        row = {}  # a row corresponding to this instance 
        #print(result)
        row["size"] = i
        row["version"] = "prism"
        for line in result.splitlines() : 
            #print(line)
            words = line.split()
            if line.startswith("Model checking:") :
                #row = {}
                pass
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
        keys = results[0].keys()

        # the results are saved in a file
        with open(f'results-prism-vs-poly.csv', 'w', newline='') as output_file:
            dict_writer = csv.DictWriter(output_file, keys)
            dict_writer.writeheader()
            dict_writer.writerows(results)
        row = {}  # a row corresponding to this instance 
        result = subprocess.run(['../../bin/polygames', "uncertainBAA/uncertain-baa.prism", 'uncertainBAA/attacker.props', '-const', f'network_unreliability=0.5,mimicry_capability=0.5,maxTime={i},nofix=1,ftr=1,rnd=1,agr=1,agf=1,rdr=1', '-javamaxmem', '4g'], capture_output=True).stdout.decode()
        #print(result)
        row["size"] = i
        row["version"] = "poly"
        for line in result.splitlines() : 
            #print(line)
            words = line.split()
            if line.startswith("Model checking:") :
                #row = {}
                pass
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
    # finally a .cvs is generated
    keys = results[0].keys()

    # the results are saved in a file
    with open(f'results-prism-vs-poly-subset.csv', 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, keys)
        dict_writer.writeheader()
        dict_writer.writerows(results)
        print("Results saved to results-prism-vs-poly-subset.csv file.")
