import os, sys, subprocess, csv

"""
This script runs the DNS benchmarks, and saves the results in a .cvs file
"""
rewards = False
results = [] # a list of dictionaries
for i in range(40,55) :
    row = {}
    row["maxTime"] = f"""{i}"""
    print(f"""running with time: {i}""")
    result = subprocess.run(['../../bin/polygames', "-javamaxmem","4g", "-const", f"""maxTime={i}""","polydns.smg", 'attacker.props'], capture_output=True).stdout.decode()
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
    results.append(row) # the row is appened to the list

keys = results[0].keys()
# the results are saved in a file
with open(f'results.csv', 'w', newline='') as output_file:
    dict_writer = csv.DictWriter(output_file, keys)
    dict_writer.writeheader()
    dict_writer.writerows(results)

with open(f'results.csv', 'r') as in_file:
    in_reader = csv.reader(in_file)
    header = next(in_reader)
    data = sorted(in_reader, key=lambda row: int(row[0]), reverse=False)

with open(f'sortedresults.csv', 'w', newline='') as out_file:
    out_file = csv.writer(out_file)
    out_file.writerow(header)
    out_file.writerows(data)
