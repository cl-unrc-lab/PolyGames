# model for DNS attack for polygames
# in this case df and fpf can vary.
BW = 458
AF = 15.31
bogus_rate = AF*10
R_l = 100
p_b_receive = 0
p_l_receive = 1
p_b_drop = 2
p_l_drop = 3
dffactor = 0.05
pfpfactor= 0.05

df_values = [0.85,0.9,0.95]
pfp_values = [0.05,0.1,0.15]
zombies_values = [0,20,40,60,80,100]
retries_values = [2,4,6]
rdf_values = [0.81,0.9,0.99]
mFile = open("polydns.smg", "w")
# the heading of the file
attacker_actions = ",".join([f"""[ch{i}]""" for i in range(1,7)])
#defender_actions = ",".join(["[nofix]"]+[f"""[FTR{i}]""" for i in range(0,9)]+[f"""[RND{i}]""" for i in range(0,3)]+ [f"""[AGR{i}]""" for i in range(1,4)]+[f"""[RDR{i}]""" for i in range(0,9)]+ [f"""[AGF{i}]""" for i in range(0,27)])
defender_actions = ",".join(["[nofix]"]+[f"""[FTR{i}]""" for i in range(0,9)]+ [f"""[AGF{i}]""" for i in range(0,27)])
mFile.write(f"""
smg

const double AF = 15.31;
const int BW = 458;       
const max_succs_attacks = 3;
const attack_latency = 2;    
global sched : [0..2]; // 0 attacker, 1 defender, 2 victim 
global zombies : [0..100];
global strat : [0..5]; // the strategy used 0:NOFIX, 1:FTR,2:RND,3:RDR,4: AGR, 5:AGF
const int maxTime;
global time : [0..maxTime];

player p1 
   Attacker, {attacker_actions}
endplayer

player p2
   Defender, {defender_actions}
endplayer
""")
# the attacker module
mFile.write(f"""
module Attacker 
    succsAttacks : [0..3];
    attackLatency : [0..1];
	[ch1] sched = 0 & time < maxTime & (attackLatency > 0 | succsAttacks = 3)  -> (zombies' = 0) & (sched' = 1) & (time' = time+1) & (succsAttacks' = 0) & (attackLatency' = mod(attackLatency+1,2));
	[ch2] sched = 0 & time < maxTime & attackLatency=0 & succsAttacks < 3  -> (zombies' = 20) & (sched' = 1) & (time' = time+1) & (succsAttacks' = succsAttacks+1);
	[ch3] sched = 0 & time < maxTime & attackLatency=0 & succsAttacks < 3-> (zombies' = 40) & (sched' = 1) & (time' = time+1) & (succsAttacks' = succsAttacks+1);
	[ch4] sched = 0 & time < maxTime & attackLatency=0 & succsAttacks < 3-> (zombies' = 60) & (sched' = 1) & (time' = time+1) & (succsAttacks' = succsAttacks+1);
	[ch5] sched = 0 & time < maxTime & attackLatency=0 & succsAttacks < 3-> (zombies' = 80) & (sched' = 1) & (time' = time+1) & (succsAttacks' =  succsAttacks+1);
	[ch6] sched = 0 & time < maxTime & attackLatency=0 & succsAttacks < 3-> (zombies' = 100) & (sched' = 1) & (time' = time+1) & (succsAttacks' = succsAttacks+1);
endmodule                 
""")
# the defender and victim module
mFile.write(f"""
module Defender
    df :[0..2]; // fraction of attack traffick detected as bogus
    pfp : [0..2]; // false positives 
    retries : [2..6];
    rdf : [0..2];
	receive_status : [0..4] init 4; //0:p_b_receive,1:p_l_receive, 2:p_b_drop, 3:p_l_drop
	[nofix] sched = 1  & time < maxTime ->  (sched' = 2) & (strat' = 0);
""")
i = 0
# actions for FTR

# actions for FTR
for df in [0,1,2] :
    for pfp in [0,1,2] :
        mFile.write(f"""[FTR{i}] sched = 1  & time < maxTime -> (sched' = 2) & (df'={df}) & (pfp' = {pfp}) & (strat' = 1);\n""") 
        i = i + 1

# actions for RND
#for rdf_pos in [0,1,2] :
#    mFile.write(f"""[RND{rdf_pos}] sched = 1  & time < maxTime -> (sched' = 2)   & (strat' = 2) & (rdf'={rdf_pos}) & (time' = time+1); // this is a parameter?\n""") 

#actions for AGR
#i = 0
#for ret in retries_values :
#    i = i + 1
#    mFile.write(f"""[AGR{i}] sched = 1  & time < maxTime -> (sched' = 2) & (retries'={ret}) & (strat' = 3) & (time' = time+1);\n """)

# actions for RDR
#i = 0
#for ret in retries_values :
#    for rdf_pos in [0,1,2] :
#        mFile.write(f"""[RDR{i}] sched = 1  & time < maxTime -> (sched' = 2) & (retries'={ret}) & (rdf'={rdf_pos}) & (strat' = 4) & (time' = time+1);\n """)
#        i = i+1

# actions for AGF
i = 0
for ret in retries_values :
    for df in [0,1,2] :
        for pfp in [0,1,2] :
            mFile.write(f"""[AGF{i}] sched = 1  & time < maxTime -> (sched' = 2) & (retries' = {ret}) & (df'= {df}) & (pfp' = {pfp})  & (strat' = 5);\n""")
            i = i + 1

# now the actions for the victim for NOFIX

for zombies in zombies_values : 
    R_b = bogus_rate * zombies
    p_drop = (R_b + R_l - BW) / (R_b + R_l)
    if (BW >= R_b + R_l) :
        mFile.write(f"""[] sched = 2 & strat = 0 & zombies = {zombies}  -> {R_b/(R_b+R_l)}: (receive_status'={p_b_receive}) & (sched' = 0) 
                                                    + {R_l/(R_b+R_l)}:(receive_status'={p_l_receive}) &  (sched' = 0);\n """)	
    else :
        mFile.write(f"""[] sched = 2 & strat = 0 & zombies = {zombies}  -> {(R_b/(R_b+R_l))*(1-p_drop)}: (receive_status'={p_b_receive}) & (sched' = 0) +  {(R_l/(R_b+R_l))*(1-p_drop)}:(receive_status'={p_l_receive}) + {(R_b/(R_b+R_l)) * p_drop}:(receive_status'={p_b_drop}) & (sched' = 0) + {(R_l/(R_b+R_l))*p_drop}:(receive_status'={p_l_drop}) & (sched' = 0);\n """)	


# victim actions for the victim for FTR
for zombies in zombies_values :
    for df_pos in [0,1,2] :
        for pfp_pos in [0,1,2] :
            df = df_values[df_pos]
            pfp = pfp_values[pfp_pos]
            R_b = bogus_rate * zombies
            R_in = (R_b*(1-df))+(R_l*(1-pfp))
            p_drop = (R_in - BW) / R_in
            if (BW >= R_in) :
                bound_pfp = f'''plrec*{((R_l + R_b)/(R_l))}+ (-{0.177* ((R_l + R_b)/(R_b))}* pbrec )<= {1-0.177},''' if R_b>0 else ''
                mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 1 & df = {df_pos} & pfp = {pfp_pos} -U-> pbrec: (receive_status' = {p_b_receive}) & (sched' = 0)
                                                                                              + plrec: (receive_status' = {p_l_receive}) & (sched' = 0)
                                                                                              + pbdrop : (receive_status' = {p_b_drop}) & (sched' = 0)
                                                                                              + pldrop : (receive_status' = {p_l_drop}) & (sched' = 0)
                                                                                              {{ 
                                                                                                 pbrec >= {(R_b*(1-df-dffactor))/(R_b+R_l)},
                                                                                                 pbrec <= {(R_b*(1-df+dffactor))/(R_b+R_l)},
                                                                                                 plrec >= {(R_l*(1-pfp-pfpfactor))/(R_b+R_l)},
                                                                                                 plrec <= {(R_l*(1-pfp+pfpfactor))/(R_b+R_l)},
                                                                                                 pbdrop >= {(R_b*(df-dffactor))/(R_b+R_l)},
                                                                                                 pbdrop <= {(R_b*(df+dffactor))/(R_b+R_l)},
                                                                                                 pldrop >= {(R_l*(pfp-pfpfactor))/(R_b+R_l)},
                                                                                                 pldrop <= {(R_l*(pfp+pfpfactor))/(R_b+R_l)}
                                                                                                                                                       
                                                                                              }};

                """)
            else :
                bound_pfp =  f""" plrec*{((R_l + R_b)/(R_l*(1-p_drop)))}+ (-{0.177* ((R_l + R_b)/(R_b*(1-p_drop)))}* pbrec )<= {1-0.177},""" if R_b>0 else ''
                mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 1 & df = {df_pos} & pfp = {pfp_pos} -U-> pbrec: (receive_status' = {p_b_receive}) & (sched' = 0)
                                                                                             + plrec: (receive_status'={p_l_receive}) & (sched' = 0)
                                                                                             + pbdrop: (receive_status'={p_b_drop}) & (sched' = 0)
                                                                                             + pldrop: (receive_status'={p_l_drop}) & (sched' = 0)
                                                                                             {{
                                                                                                     
                                                                                                 pbrec >= {((R_b*(1-df-dffactor))/(R_b+R_l))*(1-p_drop)},
                                                                                                 pbrec <= {((R_b*(1-df+dffactor))/(R_b+R_l))*(1-p_drop)},
                                                                                                 plrec >= {((R_l*(1-pfp-pfpfactor))/(R_b+R_l))*(1-p_drop)},
                                                                                                 plrec <= {((R_l*(1-pfp+pfpfactor))/(R_b+R_l))*(1-p_drop)},
                                                                                                 pbdrop >= {(((R_b*(1-df-dffactor))/(R_b+R_l))*p_drop) + ((R_b*(df+0.0005))/(R_b+R_l))},
                                                                                                 pbdrop <= {(((R_b*(1-df+dffactor))/(R_b+R_l))*p_drop) + ((R_b*(df-0.0005))/(R_b+R_l))},
                                                                                                 pldrop >= {(((R_l*(1-pfp-pfpfactor))/(R_b+R_l))*p_drop)  + ((R_l*pfp-0.0005)/(R_b+R_l))},
                                                                                                 pldrop <= {(((R_l*(1-pfp+pfpfactor))/(R_b+R_l))*p_drop)  + ((R_l*pfp+0.0005)/(R_b+R_l))}          
                                                                                             }};
	        """)

# victim actions for RND, here we have to change it if we want an interval
#for zombies in zombies_values :            
#    for rdf_pos in [0,1,2] :
#        rdf = rdf_values[rdf_pos]
#        R_b = bogus_rate * zombies
#        R_in = R_b*(1-rdf)+R_l*(1-rdf)
#        p_drop = (R_in - BW) / R_in
#        if (BW >= R_in) :
#            mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 2 & rdf = {rdf_pos}  ->  {(R_b*(1-rdf))/(R_b+R_l)}: (receive_status' = {p_b_receive}) & (sched' = 0)
#                                                                + {(R_l*(1-rdf)) /(R_b+R_l)}: (receive_status' = {p_l_receive}) & (sched' = 0)
#                                                                + {(R_b*rdf) / (R_b + R_l)} : (receive_status' = {p_b_drop}) & (sched' = 0)
#                                                                + {(R_l*rdf) / (R_b + R_l)} : (receive_status' = {p_l_drop}) & (sched' = 0);
#                """)
#        else :
#            mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 2 & rdf = {rdf_pos}  ->  {((R_b*(1-rdf))/(R_b+R_l)) * (1-p_drop)}: (receive_status' = {p_b_receive}) & (sched' = 0)
#                                                                + {((R_l*(1-rdf)) /(R_b+R_l))* (1-p_drop)}: (receive_status' = {p_l_receive}) & (sched' = 0)
#                                                                + {(((R_b*(1-rdf))/(R_b+R_l)) * p_drop) + ((R_b*rdf) / (R_b + R_l))} : (receive_status' = {p_b_drop}) & (sched' = 0)
#                                                                + {(((R_l*(1-rdf))/(R_b+R_l)) * p_drop) + ((R_l*rdf) / (R_b + R_l))} : (receive_status' = {p_l_drop}) & (sched' = 0);
#                """)
       
# victim actions for AGR
#for zombies in zombies_values :
#    for ret in retries_values :
#        rdf = 0.5;
#        R_b = bogus_rate * zombies
#        R_in = R_b+(R_l*(2**ret))
#        p_drop = (R_in - BW) / R_in
#        if (BW >= R_in) :
#            mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 3 & retries={ret} -> {R_b/(R_b+R_l*2**ret)} : (receive_status' = {p_b_receive}) & (sched' = 0)
#                                                            +{(R_l*2**ret)/(R_b+R_l*2**ret)} : (receive_status' = {p_l_receive}) & (sched' = 0);
#                """)
#        else :
#            mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 3 & retries={ret} -> {R_b/(R_b+R_l*2**ret)*(1 - p_drop)} : (receive_status' = {p_b_receive}) & (sched' = 0)
#                                                            +{((R_l*2**ret)/(R_b+R_l*2**ret))*(1 - p_drop)} : (receive_status' = {p_l_receive}) & (sched' = 0)
#                                                            +{((R_b)/(R_b+R_l*2**ret))*p_drop} : (receive_status' = {p_b_drop}) & (sched' = 0)
#                                                            +{((R_l*2**ret)/(R_b+R_l*2**ret))*p_drop} : (receive_status' = {p_l_drop}) & (sched' = 0);
#                """)
            

# victim actions for RDR
#for zombies in zombies_values :
#    for rdf_pos in [0,1,2] :
#        for ret in retries_values :
#            rdf = rdf_values[rdf_pos]
#            R_b = bogus_rate * zombies
#            R_in = ((1-rdf)*R_b) + (R_l*(2**ret)*(1-rdf))
#            p_drop = (R_in-BW) / R_in
#            if (BW >= R_in) :
#                mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 4 & retries={ret} & rdf={rdf_pos} -> {(R_b * (1-rdf))/(R_b+(R_l*(2**ret)))} : (receive_status' = {p_b_receive}) &  (sched' = 0)
#                                                            +{(R_l*(2**ret)*(1-rdf))/(R_b+(R_l*(2**ret)))} : (receive_status' = {p_l_receive}) & (sched' = 0)
#                                                            +{(R_b*rdf)/(R_b+(R_l*(2**ret)))} : (receive_status' = {p_b_drop}) & (sched' = 0)
#                                                            +{(R_l*2**ret*rdf)/(R_b+(R_l*(2**ret)))} : (receive_status' = {p_l_drop}) & (sched' = 0);
#                """)
#            else :
#                mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 4 & retries={ret} & rdf={rdf_pos} -> {((R_b * (1-rdf))/(R_b+(R_l*(2**ret)))) * (1-p_drop)} : (receive_status' = {p_b_receive}) & (sched' = 0)
#                                                            + {((R_l *(2**ret)* (1-rdf))/(R_b+(R_l*(2**ret)))) * (1-p_drop)} : (receive_status' = {p_l_receive}) & (sched' = 0)
#                                                            + {((R_b*(1-rdf))/(R_b+(R_l*(2**ret)))) * p_drop + ((R_b*rdf)/(R_b+(R_l*2**ret)))} : (receive_status' = {p_b_drop}) & (sched' = 0)
#                                                            + {((R_l*(2**ret)*(1-rdf))/(R_b+(R_l*(2**ret))))*p_drop + ((R_l*(2**ret)*rdf)/(R_b+(R_l*2**ret)))} : (receive_status' = {p_l_drop}) & (sched' = 0);
#                    """)
               
# victim actions for AGF
for zombies in zombies_values :
    for ret in retries_values :
        for df_pos in [0,1,2] :
            for pfp_pos in [0,1,2] :
                df = df_values[df_pos]
                pfp = pfp_values[pfp_pos]
                R_b = bogus_rate * zombies
                R_in = (R_b*(1-df))+(R_l*(2**ret)*(1-pfp))
                p_drop = (R_in - BW) / R_in 
                if (BW >= R_in) :
                    bound_pfp = f'''plrec*{((R_l*(2**ret) + R_b)/(R_l*(2**ret)))}+ (-{0.177* ((R_l*(2**ret) + R_b)/(R_b))}* pbrec )<= {1-0.177},''' if R_b>0 else ''
                    mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 5 & df={df_pos} & retries = {ret} & pfp = {pfp_pos} -U-> pbrec : (receive_status' = {p_b_receive}) & (sched' = 0)
                                                            + plrec : (receive_status' = {p_l_receive}) & (sched' = 0)
                                                            + pbdrop : (receive_status' = {p_b_drop}) & (sched' = 0)
                                                            + pldrop : (receive_status' = {p_l_drop}) & (sched' = 0)
                                                            {{
                                                               
                                                                pbrec <= {(R_b*(1-df+dffactor))/(R_b+(R_l*2**ret))},
                                                                pbrec >= {(R_b*(1-df-dffactor))/(R_b+(R_l*2**ret))},
                                                                plrec <= {(R_l*2**ret*(pfp+pfpfactor))/(R_b+R_l*2**ret)},
                                                                plrec >= {(R_l*2**ret*(pfp-pfpfactor))/(R_b+R_l*2**ret)},
                                                                pbdrop <= {(R_b*(df+dffactor))/(R_b+R_l*2**ret)},
                                                                pbdrop >= {(R_b*(df-dffactor))/(R_b+R_l*2**ret)},
                                                                pldrop <= {(R_l*(2**ret)*(1-pfp+pfpfactor))/(R_b+R_l*2**ret)},
                                                                pldrop >= {(R_l*(2**ret)*(1-pfp-pfpfactor))/(R_b+R_l*2**ret)}
                                                            }};
                    """)
                else :
                    bound_pfp =  f""" plrec*{((R_l*(2**ret) + R_b)/(R_l*(2**ret)*(1-p_drop)))}+ (-{0.177* ((R_l*(2**ret) + R_b)/(R_b*(1-p_drop)))}* pbrec )<= {1-0.177},""" if R_b>0 else ''
                    mFile.write(f"""[] sched = 2 & zombies = {zombies} & strat = 5 & df={df_pos} & retries = {ret} & pfp = {pfp_pos} -U-> pbrec : (receive_status' = {p_b_receive}) & (sched' = 0)
                                                            + plrec : (receive_status' = {p_l_receive}) & (sched' = 0)
                                                            + pbdrop : (receive_status' = {p_b_drop}) & (sched' = 0)
                                                            + pldrop : (receive_status' = {p_l_drop}) & (sched' = 0)
                                                            {{
                                                                
                                                                pbrec <= {((R_b*(1-df + dffactor))/(R_b+R_l*2**ret))*(1-p_drop)},
                                                                pbrec >= {((R_b*(1-df - dffactor))/(R_b+R_l*2**ret))*(1-p_drop)},
                                                                plrec <= {((R_l*(2**ret)*(1-pfp+pfpfactor))/(R_b+R_l*2**ret))*(1-p_drop)},
                                                                plrec >= {((R_l*(2**ret)*(1-pfp-pfpfactor))/(R_b+R_l*2**ret))*(1-p_drop)},
                                                                pbdrop <= {(((R_b*(1-df+dffactor))/(R_b+R_l*2**ret))*p_drop) + ((R_b*(df+dffactor))/(R_b+R_l*2**ret))},
                                                                pbdrop >= {(((R_b*(1-df-dffactor))/(R_b+R_l*2**ret))*p_drop) + ((R_b*(df-dffactor))/(R_b+R_l*2**ret))},
                                                                pldrop <= {(((R_l*(2**ret)*(1-pfp+pfpfactor))/(R_b+R_l*2**ret))*p_drop) + (R_l*(2**ret)*(pfp+pfpfactor))/(R_b+R_l*2**ret)},
                                                                pldrop >= {(((R_l*(2**ret)*(1-pfp-pfpfactor))/(R_b+R_l*2**ret))*p_drop) + (R_l*(2**ret)*(pfp-pfpfactor))/(R_b+R_l*2**ret)}
                                                            }};
                    """)

mFile.write("endmodule")

mFile.write(f"""
// received bogus packages
rewards "received_bogus"
 sched=0 & receive_status=0 : 1;
endrewards

// received legal packages
rewards "received_legal"
 sched=0 & receive_status=1 : 1;
endrewards

// dropped bogus packages
rewards "dropped_blogus"
 sched=0 & receive_status=2 : 1;
endrewards

// droppes legal packages
rewards "dropped_legal"
 sched=0 & receive_status=3 : 1;
endrewards
"""
)