# baa_ftr.py
# BAA model generator — FTR Uncertainty Extension
#
# Extends baa.py to make dfr and fpf jointly uncertain in the FTR countermeasure,
# producing a 2D polytope instead of discrete parameter selection.
#
# Changes from baa.py:
#   - RND bug fixed: lb=0.85, ub=0.95 (was swapped)
#   - FTR module: discrete dfr/fpf index selection replaced with -U-> transitions
#   - VS module default guard now excludes ftr_selected
#   - New -U-> transitions in VS for ftr_selected
#   - Cross-constraint models sensitivity/specificity trade-off:
#       fpf >= lb_fpf + k_ftr * (dfr - lb_dfr)
#     With k_ftr=0.5 this gives a 4-vertex trapezoid; the bottom-right corner
#     (high dfr, low fpf) is cut, capturing the real constraint that a more
#     aggressive filter inevitably produces more false positives.
#
# FTR probability expressions (non-congested case, zombies > 0):
#   p_b_in  = (1 - dfr)   →   p_b_receive = (1-dfr)/2,  p_b_drop = dfr/2
#   p_l_in  = (1 - fpf)   →   p_l_receive = (1-fpf)/2,  p_l_drop = fpf/2
# These are purely linear in dfr and fpf (no dependence on zombie count).
#
# Non-congestion constraint: R_b*(1-dfr) + R_l*(1-fpf) <= BW
# Rewritten as a linear lower bound: R_b*dfr + R_l*fpf >= R_b + R_l - BW
# For z in [10..40] this bound intersects the polytope; for z >= 50 the
# polytope becomes empty (network is congested even at peak filtering).
# FTR uncertain transitions are only generated for z where the polytope is
# non-empty (checked by ftr_polytope_feasible below).

# ── global parameters ──────────────────────────────────────────────────────────
AF        = 15.31
bogus_rate = 10 * AF
R_l       = 100.0
BW        = 458.0

# RND bounds (bug-fixed: lb < ub)
lb = 0.85
ub = 0.95

# FTR uncertainty bounds
lb_dfr = 0.85   # minimum detection fraction
ub_dfr = 0.95   # maximum detection fraction
lb_fpf = 0.05   # minimum false-positive fraction
ub_fpf = 0.15   # maximum false-positive fraction

# Cross-constraint slope: fpf >= lb_fpf + k_ftr*(dfr - lb_dfr)
# k_ftr = 0.5 gives a trapezoid (4 vertices, bottom-right corner cut).
# k_ftr = 1.0 gives a triangle (3 vertices, full diagonal cut).
k_ftr = 0.5

# Cross-constraint RHS constant: lb_fpf - k_ftr * lb_dfr
cross_rhs = lb_fpf - k_ftr * lb_dfr   # = 0.05 - 0.5*0.85 = -0.375


def R_b_for_z(z):
    return 153.1 * z


def ftr_polytope_feasible(z):
    """
    Return True if the FTR non-congested polytope is non-empty for this z.

    The non-congestion constraint requires R_b*dfr + R_l*fpf >= R_b + R_l - BW.
    The polytope is non-empty iff the most favourable corner (ub_dfr, ub_fpf)
    satisfies this bound.  We also verify the cross-constraint is satisfiable
    (always true for k_ftr <= (ub_fpf-lb_fpf)/(ub_dfr-lb_dfr)).
    """
    R_b = R_b_for_z(z)
    required = R_b + R_l - BW
    best     = R_b * ub_dfr + R_l * ub_fpf
    return best >= required


# ── model sections ─────────────────────────────────────────────────────────────

def gen_main(fileName):
    mFile = open(fileName, "w")
    mFile.write(f"""
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


def gen_attacker(fileName, zombiesList, max_succ_attacks, attacker_latency):
    mFile = open(fileName, "a")
    mFile.write(f"""
        module attacker
            zombies : [0..{zombiesList[-1]}] init 0;
            current_succ_attacks : [0..{max_succ_attacks}] init 0;
            wait_time : [0..{attacker_latency}] init 0;
    """)
    mFile.write(f"""
            [] turn = 0 & time < maxTime & current_succ_attacks = {max_succ_attacks} & wait_time = {attacker_latency} -> (current_succ_attacks'=0) & (wait_time'=0);
            [] turn = 0 & time < maxTime & current_succ_attacks = {max_succ_attacks} & wait_time < {attacker_latency} -> (zombies'=0) & (wait_time'=wait_time+1);
    """)
    for z in zombiesList:
        mFile.write(f"""
            [] turn = 0 & time < maxTime & current_succ_attacks < {max_succ_attacks} -> (zombies'={z})   & (current_succ_attacks'=0) & (turn'=1);
        """)
    mFile.write("endmodule")
    mFile.close()


def gen_defender(fileName):
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
    """)
    mFile.close()


def gen_mods(fileName):
    """Generate countermeasure parameter-selection modules.

    FTR is now uncertain: no discrete dfr/fpf index selection.
    The ftr module simply clears parameters_selection (like rnd),
    deferring all probability computation to the -U-> transitions in vs.
    AGF still uses discrete dfr/fpf index selection (unchanged).
    """
    mFile = open(fileName, "a")
    mFile.write("""
        module ftr
            // FTR uncertainty: dfr and fpf are continuously uncertain.
            // Parameter selection just clears the flag; the -U-> transitions
            // in the vs module carry the polytope constraints.
            [ftr] ftr_selected & parameters_selection -> (parameters_selection' = false);
        endmodule

        module rnd
            [rnd] rnd_selected & parameters_selection ->  (parameters_selection' = false);
        endmodule

        module agr
            [agr] agr_selected & parameters_selection -> (retries' = 2) & (parameters_selection' = false);
            [agr] agr_selected & parameters_selection -> (retries' = 4) & (parameters_selection' = false);
            [agr] agr_selected & parameters_selection -> (retries' = 6) & (parameters_selection' = false);
        endmodule

        module rdr
            rdr_step : [0..1] init 0;

            [rdr] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 0) & (rdr_step' = 1);
            [rdr] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 1) & (rdr_step' = 1);
            [rdr] rdr_selected & parameters_selection & rdr_step = 0 -> (rdf_index' = 2) & (rdr_step' = 1);

            [rdr] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 2) & (parameters_selection' = false) & (rdr_step' = 0);
            [rdr] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 4) & (parameters_selection' = false) & (rdr_step' = 0);
            [rdr] rdr_selected & parameters_selection & rdr_step = 1 -> (retries' = 6) & (parameters_selection' = false) & (rdr_step' = 0);
        endmodule

        module agf
            agf_step : [0..2] init 0;

            [agf] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 0) & (agf_step' = 1);
            [agf] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 1) & (agf_step' = 1);
            [agf] agf_selected & parameters_selection & agf_step = 0 -> (dfr_index' = 2) & (agf_step' = 1);

            [agf] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 0) & (agf_step' = 2);
            [agf] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 1) & (agf_step' = 2);
            [agf] agf_selected & parameters_selection & agf_step = 1 -> (fpf_index' = 2) & (agf_step' = 2);

            [agf] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 2) & (parameters_selection' = false) & (agf_step' = 0);
            [agf] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 4) & (parameters_selection' = false) & (agf_step' = 0);
            [agf] agf_selected & parameters_selection & agf_step = 2 -> (retries' = 6) & (parameters_selection' = false) & (agf_step' = 0);
        endmodule
    """)
    mFile.close()


def gen_vs(fileName, zombiesList):
    """Generate the victim server (vs) module.

    Differences from baa.py:
      1. Default transition guard adds !ftr_selected, so the formula-based
         transition no longer fires for FTR.
      2. New -U-> transitions handle ftr_selected with linear probability
         expressions in dfr and fpf.
      3. RND lb/ub are corrected (lb=0.85 < ub=0.95).
    """
    mFile = open(fileName, "a")
    mFile.write("""
    module vs
        vs_step : [0..1] init 0;
        packet_status : [0..4] init 0; // 0:default 1:bogus_received 2:bogus_dropped 3:legit_received 4:legit_dropped

            // Default: all countermeasures except rnd and ftr (uses PRISM formulas)
            [] turn = 2 & time < maxTime & !parameters_selection & !rnd_selected & !ftr_selected & vs_step = 0 -> p_b_receive : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop    : (packet_status'=4) & (vs_step'=1);
    """)

    for z in zombiesList:
        R_b     = R_b_for_z(z)
        R_in    = R_b + R_l          # unfiltered total rate
        R_b_in  = R_b                # RND: no pre-filter
        R_l_in  = R_l
        R_b_out = R_b
        R_l_out = R_l

        p_b_in  = R_b_in / (R_b_in + R_b_out) if z > 0 else 0
        p_l_in  = R_l_in / (R_l_in + R_l_out)
        p_b_out = R_b_out / (R_b_in + R_b_out) if z > 0 else 1
        p_l_out = R_l_out / (R_l_in + R_l_out)

        p_drop        = 0 if BW >= R_in else (R_in - BW) / R_in
        p_receive     = 1 - p_drop
        p_b_drop_in   = p_b_in * p_drop / 2
        p_b_drop_out  = p_b_out / 2
        p_b_drop_in_limit = p_b_in / 2
        p_l_drop_in   = p_l_in * p_drop / 2
        p_l_drop_out  = p_l_out / 2
        p_l_drop_in_limit = p_l_in / 2

        # ── RND uncertain transitions (identical logic to baa.py, lb/ub fixed) ──
        if z == 0:
            mFile.write(f"""
            // RND: zombies = 0
            [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies = 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1)
            {{ // non-congested case (zombies=0 means no bogus traffic)
                {BW} >= rnd * {R_in},
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
            {{ // congested case (zombies=0)
                {R_in} * rnd  + (-{BW}) >= 0,
                {lb} <= rnd,
                rnd <= {ub},
                p_b_receive_rnd = 0,
                p_l_receive_rnd = {p_l_in/2 * (BW / R_in)} + ( -  {p_l_in/2 * (BW / R_in)}* rnd),
                p_b_drop_rnd = 1/2,
                p_l_drop_rnd = {p_l_out/2} * rnd
            }};
            """)
        else:
            p_b_in_nz = R_b_in / (R_b_in + R_b_out)
            mFile.write(f"""
                // RND: zombies > 0 (z={z})
                [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies > 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1)
                {{ // non-congested case
                    {BW} >= {R_in} * rnd,
                    {lb} <= rnd,
                    rnd <= {ub},
                    p_b_receive_rnd = {p_b_in_nz/2} * rnd,
                    p_l_receive_rnd = {p_l_in/2} + (- {p_l_in/2}*rnd),
                    p_b_drop_rnd =  {p_b_drop_in_limit}+(- {p_b_drop_in_limit} * rnd)  + {p_b_drop_out} * rnd,
                    p_l_drop_rnd = {p_l_drop_in_limit}+ (- {p_l_drop_in_limit} * rnd) + {p_l_drop_out} * rnd
                }};
                [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies > 0 & vs_step = 0 -U-> p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1)
                {{ // congested case
                    {R_in} * rnd  + (-{BW}) >= 0,
                    {lb} <= rnd,
                    rnd <= {ub},
                    p_b_receive_rnd = {(p_b_in_nz * p_receive)/2},
                    p_l_receive_rnd = {p_l_in/2 * (BW / R_in)}+(- {p_l_in/2 * (BW / R_in)} * rnd),
                    p_b_drop_rnd =  {p_b_drop_in} + {p_b_drop_out} * rnd,
                    p_l_drop_rnd = {p_l_drop_in} + {p_l_drop_out} * rnd
                }};

                [] turn = 2 & time < maxTime & !parameters_selection & vs_step = 1 -> (vs_step'=0) & (packet_status'=0) & (time'=time+1) & (turn'= 0);
            """)

        # ── FTR uncertain transitions ──────────────────────────────────────────
        # Probabilities for FTR (non-congested regime):
        #   p_b_in  = 1 - dfr   →  p_b_receive = (1-dfr)/2,  p_b_drop = dfr/2
        #   p_l_in  = 1 - fpf   →  p_l_receive = (1-fpf)/2,  p_l_drop = fpf/2
        # These are purely linear in dfr and fpf.
        #
        # Non-congestion constraint: R_b*(1-dfr) + R_l*(1-fpf) <= BW
        # Equivalent linear form:   R_b*dfr + R_l*fpf >= R_b + R_l - BW
        #
        # Cross-constraint (sensitivity/specificity trade-off):
        #   fpf >= lb_fpf + k_ftr*(dfr - lb_dfr)
        # Equivalent: fpf + -dfr*k_ftr >= lb_fpf - k_ftr*lb_dfr

        if z == 0:
            # No bogus traffic: p_b_receive=0, p_b_drop=0.5 (fixed), legit is uncertain
            mFile.write(f"""
            // FTR uncertainty: zombies = 0 (no bogus traffic)
            [] turn = 2 & time < maxTime & !parameters_selection & ftr_selected & zombies = 0 & vs_step = 0 -U-> p_b_receive_ftr : (packet_status'=1) & (vs_step'=1)
                                                                      + p_b_drop_ftr    : (packet_status'=2) & (vs_step'=1)
                                                                      + p_l_receive_ftr : (packet_status'=3) & (vs_step'=1)
                                                                      + p_l_drop_ftr    : (packet_status'=4) & (vs_step'=1)
            {{ // fpf uncertain in [{lb_fpf}, {ub_fpf}]; dfr bounds included for polytope completeness
                {lb_dfr} <= dfr,
                dfr <= {ub_dfr},
                {lb_fpf} <= fpf,
                fpf <= {ub_fpf},
                fpf + -dfr * {k_ftr} >= {cross_rhs},
                p_b_receive_ftr = 0,
                p_b_drop_ftr = 0.5,
                p_l_receive_ftr = 0.5 + -fpf * 0.5,
                p_l_drop_ftr = fpf * 0.5
            }};
            """)
        elif ftr_polytope_feasible(z):
            # zombies > 0: full 2D polytope, non-congestion constraint active
            noncong_rhs = R_b + R_l - BW
            mFile.write(f"""
                // FTR uncertainty: zombies > 0 (z={z}, non-congested regime)
                // Polytope: dfr in [{lb_dfr},{ub_dfr}], fpf in [{lb_fpf},{ub_fpf}]
                // Cross-constraint (sensitivity/specificity): fpf + -dfr*{k_ftr} >= {cross_rhs}
                // Non-congestion: {R_b}*dfr + {R_l}*fpf >= {noncong_rhs:.1f}
                [] turn = 2 & time < maxTime & !parameters_selection & ftr_selected & zombies > 0 & vs_step = 0 -U-> p_b_receive_ftr : (packet_status'=1) & (vs_step'=1)
                                                                        + p_b_drop_ftr    : (packet_status'=2) & (vs_step'=1)
                                                                        + p_l_receive_ftr : (packet_status'=3) & (vs_step'=1)
                                                                        + p_l_drop_ftr    : (packet_status'=4) & (vs_step'=1)
                {{ // 2D polytope: dfr x fpf with cross-constraint and non-congestion bound
                    {lb_dfr} <= dfr,
                    dfr <= {ub_dfr},
                    {lb_fpf} <= fpf,
                    fpf <= {ub_fpf},
                    fpf + -dfr * {k_ftr} >= {cross_rhs},
                    dfr * {R_b} + fpf * {R_l} >= {noncong_rhs:.1f},
                    p_b_receive_ftr = 0.5 + -dfr * 0.5,
                    p_b_drop_ftr    = dfr * 0.5,
                    p_l_receive_ftr = 0.5 + -fpf * 0.5,
                    p_l_drop_ftr    = fpf * 0.5
                }};

                [] turn = 2 & time < maxTime & !parameters_selection & vs_step = 1 -> (vs_step'=0) & (packet_status'=0) & (time'=time+1) & (turn'= 0);
            """)
        else:
            # z too large: even max filtering cannot prevent congestion with these bounds.
            # No FTR uncertain transition is generated; the defender should choose
            # a different countermeasure for this zombie count.
            mFile.write(f"""
                // FTR uncertainty: zombies > 0 (z={z}) — polytope EMPTY with current bounds.
                // Non-congestion requires R_b*dfr + R_l*fpf >= {R_b + R_l - BW:.1f}
                // but max achievable is {R_b*ub_dfr + R_l*ub_fpf:.1f} < {R_b + R_l - BW:.1f}.
                // No -U-> transition generated; FTR is infeasible at this load.

                [] turn = 2 & time < maxTime & !parameters_selection & vs_step = 1 -> (vs_step'=0) & (packet_status'=0) & (time'=time+1) & (turn'= 0);
            """)

    mFile.write("""endmodule""")
    mFile.close()


# ── generate model ─────────────────────────────────────────────────────────────
zombiesList = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
fileName    = "baa_ftr.sgm"

gen_main(fileName)
gen_attacker(fileName, zombiesList, 10, 3)
gen_defender(fileName)
gen_mods(fileName)
gen_vs(fileName, zombiesList)

# ── append formulas and rewards ────────────────────────────────────────────────
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

                // R_b_out, R_l_out: rates at which packets are dropped at the Defender
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

                formula no_fix_selected = (countermeasure = 0);
                formula ftr_selected    = (countermeasure = 1);
                formula rnd_selected    = (countermeasure = 2);
                formula agr_selected    = (countermeasure = 3);
                formula rdr_selected    = (countermeasure = 4);
                formula agf_selected    = (countermeasure = 5);

                formula p_b_in  = (zombies > 0) ? R_b_in / (R_b_in + R_b_out) : 0;
                formula p_l_in  = R_l_in / (R_l_in + R_l_out);
                formula p_b_out = (zombies > 0) ? R_b_out / (R_b_in + R_b_out) : 1;
                formula p_l_out = R_l_out / (R_l_in + R_l_out);
                formula R_in    = R_b_in + R_l_in;
                formula p_drop  = (BW >= R_in ? 0 : ((R_in - BW) / R_in));
                formula p_receive = (1 - p_drop);
                formula p_b_receive = (p_b_in * p_receive)/2;
                formula p_l_receive = (p_l_in * p_receive)/2;
                formula p_b_drop    = (p_b_in * p_drop + p_b_out)/2;
                formula p_l_drop    = (p_l_in * p_drop + p_l_out)/2;

                // Discrete parameter values (used by AGF; FTR now uses -U-> transitions)
                const double dfr_values[3] = {{0.85, 0.90, 0.95}};
                const double fpf_values[3] = {{0.05, 0.10, 0.15}};
                const double rdf_values[3] = {{0.75, 0.85, 0.95}};

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

print(f"Generated {fileName}")
print(f"FTR uncertainty: dfr in [{lb_dfr}, {ub_dfr}], fpf in [{lb_fpf}, {ub_fpf}]")
print(f"Cross-constraint slope k_ftr = {k_ftr}  (fpf >= {lb_fpf} + {k_ftr}*(dfr - {lb_dfr}))")
print(f"Polytope shape: {'trapezoid (4 vertices)' if k_ftr < (ub_fpf-lb_fpf)/(ub_dfr-lb_dfr) else 'triangle (3 vertices)'}")
print(f"FTR transitions generated for z in: {[z for z in zombiesList if ftr_polytope_feasible(z)]}")
print(f"FTR infeasible (congested) for z in: {[z for z in zombiesList if not ftr_polytope_feasible(z)]}")
