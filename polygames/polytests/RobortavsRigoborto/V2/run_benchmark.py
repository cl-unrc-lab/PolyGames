import os, sys, subprocess, csv

"""
This script runs the benchmarks, and saves the results in a .cvs file
"""
rewards = False
try :
   arg = sys.argv[1] 
   assert arg in ["benchmark", "diagonal", "paper-benchmark"]
   if len(sys.argv) > 2 :
      assert sys.argv[2] == "rewards"
      rewards = True 
except : 
   print("""
   error reading the parameter.
   Usage: python gen_bench <option> <reward>
   where <option> in [benchmark, diagonal, paper-benchmark]
         <reward> in [rewards]
   """)
   sys.exit()

inputdir = arg
if rewards :
   inputdir = inputdir + "-rewards"

#inputdir = "benchmark"
#inputdir = "diagonal"
results = [] # a list of dictionaries
for file in os.listdir(inputdir+"/") :  
    row = {}  # a rwo corresponding to this file 
    if file.split('.')[1] == "smg" :
        try :  
           txt = file.split('-')
           seed = txt[1]
           row["width"] = txt[2]
           row["length"] = txt[3]
           row["rob_x"] = txt[4].strip("RobX")
           row["rob_y"] = txt[5].strip("RobY")
           row["rig_x"] = txt[6].strip("RigX")
           row["rig_y"] = txt[7].strip("RigY")
           row["id"] = txt[8].strip(".smg").strip("Number")
           if inputdir == "diagonal" :
              row["distance"] = abs(int(row["rob_x"]) - int(row["rig_x"]))
        except Exception:
          print("Error reading file: "+file)
        # we run the command
        print("running: "+file)
        if rewards :
            result = subprocess.run(['../../../bin/polygames',f"-maxiters", '50000', inputdir+"/"+file, 'RobortavsRigobortoRewards.props'], capture_output=True).stdout.decode()
        else :
            result = subprocess.run(['../../../bin/polygames', inputdir+"/"+file, 'RobortavsRigoborto.props'], capture_output=True).stdout.decode()
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
              
        results.append(row) # the row is appened to the list

# finally a .cvs is generated
keys = results[0].keys()

# the results are saved in a file
with open(f'results-{inputdir}.csv', 'w', newline='') as output_file:
    dict_writer = csv.DictWriter(output_file, keys)
    dict_writer.writeheader()
    dict_writer.writerows(results)

with open(f'results-{inputdir}.csv', 'r') as in_file:
    in_reader = csv.reader(in_file)
    header = next(in_reader)
    data = sorted(in_reader, key=lambda row: int(row[0]), reverse=False)

with open(f'sortedresults-{inputdir}.csv', 'w', newline='') as out_file:
    out_file = csv.writer(out_file)
    out_file.writerow(header)
    out_file.writerows(data)






