def gen_main(fileName) :
    # we output the basic files
    mFile = open(fileName, "w")
    mFile.write("""
        smg

        player p1
            attacker
        endplayer

        player p2
            defender
        endplayer

        player p3
            vs, ftr, rnd, agr, rdr, agf
        endplayer

        const int maxTime;
        const int max_succ_attacks;
        const int attacker_latency;
        const double lb;
        const double ub;

        global turn : [0..2] init 0;
        global time : [0..maxTime] init 0;
        global parameters_selection : bool init false;

        global retries : [2..6] init 2;

        global dfr_index : [0..2] init 0;
        global fpf_index : [0..2] init 0;
        global rdf_index : [0..2] init 0;
    """
    )
    mFile.close()

def gen_attacker(fileName, zombiesList ,max_succ_attacks, attacker_latency) :
    mFile = open(fileName, "a")
    mFile.write(f""" 
        module attacker
            zombies : [0..{zombiesList[-1]}] init 0;
            current_succ_attacks : [0..{max_succ_attacks}] init 0;
            wait_time : [0..{attacker_latency}] init 0;
    """)
    # when the number attacks reached the maximum
    mFile.write(f"""
            [] turn = 0 & time < maxTime & current_succ_attacks = {max_succ_attacks} & wait_time = {attacker_latency} -> (current_succ_attacks'=0) & (wait_time'=0);
            [] turn = 0 & time < maxTime & current_succ_attacks = {max_succ_attacks} & wait_time < {attacker_latency} -> (zombies'=0) & (wait_time'=wait_time+1);
    """   
    )
    # otherwise
    for z in zombiesList : 
        mFile.write(f"""
            [] turn = 0 & time < maxTime & current_succ_attacks < {max_succ_attacks} -> (zombies'={z})   & (current_succ_attacks'=0) & (turn'=1);
        """
        )
    mFile.write("endmodule")
    mFile.close()

def gen_defender(fileName) : 
    mFile = open(fileName, "a")
    mFile.write("""
        module defender
            countermeasure : [0..5] init 0;

            [] turn = 1 & time < maxTime -> (countermeasure'= 0) & (turn'= 2);
            [] turn = 1 & time < maxTime -> (countermeasure'= 1) & (parameters_selection'= true) & (turn'= 2);
            [] turn = 1 & time < maxTime -> (countermeasure'= 2) & (parameters_selection'= true) & (turn'= 2);
            [] turn = 1 & time < maxTime -> (countermeasure'= 3) & (parameters_selection'= true) & (turn'= 2);
            [] turn = 1 & time < maxTime -> (countermeasure'= 4) & (parameters_selection'= true) & (turn'= 2);
            [] turn = 1 & time < maxTime -> (countermeasure'= 5) & (parameters_selection'= true) & (turn'= 2);
        endmodule
    """
    )
    mFile.close()
  
def gen_vs(fileName, zombiesList) : 
    global AF
    global bogus_rate 
    global R_l 
    global BW 
    global lb
    global ub
    mFile = open(fileName, "a")
    mFile.write("""
    module vs
        vs_step : [0..1] init 0;
        packet_status : [0..4] init 0; // packet_status = { 0: "default", 1: "bogus_packet_received", 2: "bogus_packet_dropped", 3: "legit_packet_received", 4: "legit_packet_dropped" }

            [] turn = 2 & time < maxTime & !parameters_selection & !rnd_selected & vs_step = 0 -> p_b_receive : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop    : (packet_status'=4) & (vs_step'=1);
    """)
    for z in zombiesList :
        R_b = 153.1*z # The net arrival rate for the bogus DNS packets
        # (Rates at which bogus and legit packets, respectively, pass through the Defender.)
        R_b_in = R_b  # this is for the case of random defense 
        R_l_in = R_l  # similar as above 

        R_in = R_b_in + R_l_in 

        # Rates at which bogus and legit packets, respectively, are dropped at the Defender.
        # for this case the randomfactor is multiplied later
        R_b_out = R_b
        R_l_out = R_l

        # Probability that a bogus packet passes through the Defender.
        p_b_in  = R_b_in / (R_b_in + R_b_out) if z > 0 else 0
        
        # Probability with which a legit packet passes through the Defender.
        p_l_in  = R_l_in / (R_l_in + R_l_out)

        # Probability that a bogus packet is dropped at the Defender.
        p_b_out =  R_b_out / (R_b_in + R_b_out) if (z > 0) else 1
        
        # Probability with which a legitimate packet is dropped at the Defender.
        p_l_out = R_l_out / (R_l_in + R_l_out)

        # Rate at which packets, both legit and bogus, pass through the Defender.
        R_in = R_b_in + R_l_in

        # Probability that a packet, whether bogus or legit, is dropped.
        p_drop = 0 if BW >= R_in else (R_in - BW) / R_in

        # Probability that a packet, whether bogus or legit, is received.
        p_receive = (1 - p_drop)

        p_b_drop_in = (p_b_in * p_drop)/2
        p_b_drop_out = p_b_out/2
        p_b_drop_in_limit = p_b_in/2

        p_l_drop_in = p_l_in * p_drop / 2
        p_l_drop_out = p_l_out / 2;
        p_l_drop_in_limit = p_l_in/2        

        if z == 0 : 
            mFile.write(f"""
            // case that zombies is equal to 0
            [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies = 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1) 
            {{ // equations
                {BW} >= rnd * {R_in},  // this is needed to rule out values of rnd, check out this!
                {lb} <= rnd,
                rnd <= {ub},
                p_b_receive_rnd = 0,
                p_l_receive_rnd  =  {p_l_in/2} + (- {p_l_in/2}*rnd),
                p_b_drop_rnd = 1/2,
                p_l_drop_rnd = {p_l_out/2} * rnd
            }};
            [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies = 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1) 
            {{ // equations
                {R_in} * rnd  + (-{BW}) >= 0, // this only models this case
                {lb} <= rdf,
                rnd <= {ub},
                p_b_receive_rnd = 0,
                p_l_receive_rnd = {p_l_in/2 * (BW / R_in)} + ( -  {p_l_in/2 * (BW / R_in)}* rnd),
                p_b_drop_rnd = 1/2,
                p_l_drop_rnd = {p_l_out/2} * rnd
            }};
            """)
        else :
        # if zombies are greater than 0
            p_b_in =  R_b_in / (R_b_in + R_b_out);
            mFile.write(f"""
                [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies > 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1) 
                {{ // equations
                    {BW} >= {R_in} * rnd, // this only models this case
                    {lb} <= rnd,
                    rnd <= {ub},
                    p_b_receive_rnd = {p_b_in/2} * rnd,
                    p_l_receive_rnd = {p_l_in/2} + (- {p_l_in/2}*rnd), 
                    p_b_drop_rnd =  {p_b_drop_in_limit}+(- {p_b_drop_in_limit} * rnd)  + {p_b_drop_out} * rnd,
                    p_l_drop_rnd = {p_l_drop_in_limit}+ (- {p_l_drop_in_limit} * rnd) + {p_l_drop_out} * rnd
                }};
            [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies > 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1) 
            {{ // equations
                {R_in} * rnd  + (-{BW}) >= 0,
                {lb} <= rnd,
                rnd <= {ub},
                p_b_receive_rnd = {(p_b_in * p_receive)/2},
                p_l_receive_rnd = {p_l_in/2 * (BW / R_in)}+(- {p_l_in/2 * (BW / R_in)} * rnd),
                p_b_drop_rnd =  {p_b_drop_in} + {p_b_drop_out} * rnd,
                p_l_drop_rnd = {p_l_drop_in} + {p_l_drop_out} * rnd
            }};

            [] turn = 2 & time < maxTime & !parameters_selection & vs_step = 1 -> (vs_step'=0) & (packet_status'=0) & (time'=time+1) & (turn'= 0);
        """)
    mFile.write("""endmodule""")
    mFile.close()

def gen_mods(fileName) : 
    """ this generate each module for the attacks
    """
    mFile = open(fileName, "a")
    mFile.write("""
        module ftr
            ftr_step : [0..1] init 0;
            [] ftr_selected & parameters_selection & ftr_step= 0 -> (dfr_index'= 0) & (ftr_step'= 1);
            [] ftr_selected & parameters_selection & ftr_step= 0 -> (dfr_index'= 1) & (ftr_step'= 1);
            [] ftr_selected & parameters_selection & ftr_step= 0 -> (dfr_index'= 2) & (ftr_step'= 1);
            [] ftr_selected & parameters_selection & ftr_step= 1 -> (fpf_index'= 0) & (parameters_selection'= false) & (ftr_step'= 0);
            [] ftr_selected & parameters_selection & ftr_step= 1 -> (fpf_index'= 1) & (parameters_selection'= false) & (ftr_step'= 0);
            [] ftr_selected & parameters_selection & ftr_step= 1 -> (fpf_index'= 2) & (parameters_selection'= false) & (ftr_step'= 0);
        endmodule

        module rnd
            [] rnd_selected & parameters_selection ->  (parameters_selection' = false);
        endmodule

        module agr
            [] agr_selected & parameters_selection -> (retries' = 2) & (parameters_selection' = false);
            [] agr_selected & parameters_selection -> (retries' = 4) & (parameters_selection' = false);
            [] agr_selected & parameters_selection -> (retries' = 6) & (parameters_selection' = false);
        endmodule

        module rdr
            rdr_step : [0..1] init 0;

            [] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 0) & (rdr_step' = 1);
            [] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 1) & (rdr_step' = 1);
            [] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 2) & (rdr_step' = 1);

            [] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 2) & (parameters_selection' = false) & (rdr_step' = 0);
            [] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 4) & (parameters_selection' = false) & (rdr_step' = 0);
            [] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 6) & (parameters_selection' = false) & (rdr_step' = 0);
        endmodule

        module agf
            agf_step : [0..2] init 0;

            [] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 0) & (agf_step' = 1);
            [] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 1) & (agf_step' = 1);
            [] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 2) & (agf_step' = 1);

            [] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 0) & (agf_step' = 2);
            [] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 1) & (agf_step' = 2);
            [] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 2) & (agf_step' = 2);

            [] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 2) & (parameters_selection' = false) & (agf_step' = 0);
            [] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 4) & (parameters_selection' = false) & (agf_step' = 0);
            [] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 6) & (parameters_selection' = false) & (agf_step' = 0);
        endmodule  
    """
    )
    mFile.close()

# default value for the constants
AF = 15.31
bogus_rate = 10 * AF

R_l = 100.0
R_b = lambda z : 153.1*z
BW = 458.0;
lb = 0.95
ub= 0.85

fileName = "baa.sgm"
gen_main(fileName)
gen_attacker(fileName,[10,20,30,40,50,60,70,80,90,100],10,3)
gen_defender(fileName)
gen_mods(fileName)
gen_vs(fileName,[10,20,30,40,50,60,70,80,90,100])
mFile = open(fileName, "a")
mFile.write(f"""
                formula AF = 15.31;
                formula bogus_rate = 10 * AF;

                // The net arrival rate for the bogus DNS packets
                formula R_b = bogus_rate * zombies;

                // The rate at which the legitimate DNS packets arrive at and flow out of VS 
                formula R_l = 100.0;

                formula BW = 458.0;
                formula R_b_in = {{
                    [] no_fix_selected | agr_selected : R_b,
                    [] ftr_selected | agf_selected    : R_b * (1 - dfr),
                    [] otherwise                      : R_b 
                }};

                formula R_l_in = {{
                    [] no_fix_selected : R_l,
                    [] ftr_selected    : R_l * (1 - fpf),
                    [] rnd_selected    : R_l,
                    [] agr_selected    : R_l * pow(2, retries),
                    [] rdr_selected    : R_l * pow(2, retries),
                    [] otherwise       : R_l * pow(2, retries) * (1 - fpf)
                }};

                // R_b_out, R_l_out: Rates at which bogus and legit packets, respectively, are dropped at the Defender.
                formula R_b_out = {{
                    [] no_fix_selected | agr_selected : 0,
                    [] ftr_selected | agf_selected    : R_b * dfr,
                    [] otherwise                      : R_b 
                }};

                formula R_l_out = {{
                    [] no_fix_selected | agr_selected : 0,
                    [] ftr_selected                   : R_l * fpf,
                    [] rnd_selected                   : R_l,
                    [] rdr_selected                   : R_l * pow(2, retries) * rdf,
                    [] otherwise                      : R_l * pow(2, retries) * fpf
                }};

                // These formulaes are used for readability purposes
                formula no_fix_selected = (countermeasure = 0);
                formula ftr_selected    = (countermeasure = 1);
                formula rnd_selected    = (countermeasure = 2);
                formula agr_selected    = (countermeasure = 3);
                formula rdr_selected    = (countermeasure = 4);
                formula agf_selected    = (countermeasure = 5);

                // p_b_in: Probability that a bogus packet passes through the Defender.
                // If the number of zombies is zero (i.e., the attack has stopped), the probability of a bogus packet reaching the VS is zero.
                formula p_b_in  = (zombies > 0) ? R_b_in / (R_b_in + R_b_out) : 0 ;

                // p_l_in: Probability with which a legit packet passes through the Defender.
                formula p_l_in  = R_l_in / (R_l_in + R_l_out);

                // p_b_out: Probability that a bogus packet is dropped at the Defender.
                // If the number of zombies is zero (i.e., the attack has stopped), the probability of a bogus packet not reaching the VS is one.
                formula p_b_out = (zombies > 0) ? R_b_out / (R_b_in + R_b_out) : 1 ;

                // p_l_out: Probability with which a legitimate packet is dropped at the Defender.
                formula p_l_out = R_l_out / (R_l_in + R_l_out);

                // R_in: Rate at which packets, both legit and bogus, pass through the Defender.
                formula R_in = R_b_in + R_l_in;

                // p_drop: Probability that a packet, whether bogus or legit, is dropped.
                formula p_drop = (BW >= R_in ? 0 : ((R_in - BW) / R_in));

                // p_receive: Probability that a packet, whether bogus or legit, is received.
                formula p_receive = (1 - p_drop);

                // p_b_receive: Probability that a bogus packet is received.
                formula p_b_receive = (p_b_in * p_receive)/2;

                // p_l_receive: Probability that a legit packet is received.
                formula p_l_receive = (p_l_in * p_receive)/2;

                // p_b_drop: Probability that a bogus packet is dropped.
                formula p_b_drop = (p_b_in * p_drop + p_b_out)/2; 

                // p_l_drop: Probability that a legit packet is dropped.
                formula p_l_drop = (p_l_in * p_drop + p_l_out)/2;
            
                const double dfr_values[3] = {{0.85, 0.90, 0.95}}; // Detection fraction parameter values
                const double fpf_values[3] = {{0.05, 0.10, 0.15}}; // False positive fraction parameter values
                const double rdf_values[3] = {{0.75, 0.85, 0.95}}; // Random-drop fraction parameter values

                formula dfr = dfr_values[dfr_index];
                formula fpf = fpf_values[fpf_index];
                formula rdf = rdf_values[rdf_index];

                rewards "AttackerPayoff_1"
                turn = 2 & legit_packet_received : 0;
                turn = 2 & (bogus_packet_dropped | legit_packet_dropped) : 1;
                turn = 2 & bogus_packet_received : 2;
                turn = 0 | turn = 1 : 0;
                endrewards

                formula bogus_packet_received = packet_status = 1;
                formula bogus_packet_dropped  = packet_status = 2;
                formula legit_packet_received = packet_status = 3;
                formula legit_packet_dropped  = packet_status = 4;
""")
mFile.close()

