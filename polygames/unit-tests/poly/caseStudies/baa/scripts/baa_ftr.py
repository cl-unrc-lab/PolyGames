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
# --- Zombie pool / depletion mechanic (new) ---
#   global pool : [0..10] init 10  (pool*10 = zombies currently available)
#
#   Attacker can only deploy z <= pool*10 each round.
#
#   After each round (vs_step=1), player 3 applies depletion:
#     FTR:  adversarial dep in [floor(z*lb_dfr/10), floor(z*ub_dfr/10)]
#     AGF:  deterministic dep = floor(z * dfr_val[dfr_index] / 10)
#     others (no_fix, RND, AGR, RDR): no depletion
#
#   Detected zombies are blocked permanently (no recovery).
#
# FTR probability expressions (non-congested case, zombies > 0):
#   p_b_in  = (1 - dfr)   →  p_b_receive = (1-dfr)/2,  p_b_drop = dfr/2
#   p_l_in  = (1 - fpf)   →  p_l_receive = (1-fpf)/2,  p_l_drop = fpf/2

# ── global parameters ──────────────────────────────────────────────────────────
AF         = 15.31
bogus_rate = 10 * AF
R_l        = 100.0
BW         = 458.0

# RND bounds
lb = 0.85
ub = 0.95

# FTR uncertainty bounds
lb_dfr = 0.85
ub_dfr = 0.95
lb_fpf = 0.05
ub_fpf = 0.15

# Cross-constraint slope: fpf >= lb_fpf + k_ftr*(dfr - lb_dfr)
k_ftr     = 0.5
cross_rhs = lb_fpf - k_ftr * lb_dfr   # = -0.375

# AGF discrete dfr values (indexed 0..2)
AGF_DFR = {0: 0.85, 1: 0.90, 2: 0.95}


def R_b_for_z(z):
    return 153.1 * z


def zeros(all_vars, present_vars):
    missing = [v for v in all_vars if v not in present_vars]
    return ''.join(f' + &{v} * 0' for v in missing)


def ftr_polytope_feasible(z):
    R_b      = R_b_for_z(z)
    required = R_b + R_l - BW
    best     = R_b * ub_dfr + R_l * ub_fpf
    return best >= required


def ftr_cong_feasible(z):
    R_b       = R_b_for_z(z)
    threshold = R_b + R_l - BW
    worst     = R_b * lb_dfr + R_l * lb_fpf
    return worst <= threshold


def ftr_depletion_range(z):
    """Return (lb_dep, ub_dep) for FTR zombie depletion at zombie count z.

    dep is in pool units (1 unit = 10 zombies).
    lb_dep = floor(z * lb_dfr / 10)
    ub_dep = floor(z * ub_dfr / 10)
    """
    return int(z * lb_dfr / 10), int(z * ub_dfr / 10)


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
            vs, ftr, rnd, agr, rdr, agf, [ftr], [rnd], [agr], [rdr], [agf]
        endplayer

        const int maxTime;
        const int max_succ_attacks;
        const int attacker_latency;

        global turn : [0..2] init 0;
        global time : [0..maxTime] init 0;
        global parameters_selection : bool init false;

        global retries : [2..6] init 2;

        global dfr_index : [0..2] init 0;
        global fpf_index : [0..2] init 0;
        global rdf_index : [0..2] init 0;

        // Zombie pool: pool*10 = zombies available to the attacker.
        // Depleted each round by FTR/AGF detection; no recovery.
        global pool : [0..10] init 10;
    """
    )
    mFile.close()


def gen_attacker(fileName, zombiesList):
    """Attacker module with pool-bounded deployment.

    The attacker can deploy z zombies only if pool >= z/10.
    When pool=0 the attacker is forced to pass (zombies=0).
    current_succ_attacks / wait_time mechanics removed (were unused).
    """
    mFile = open(fileName, "a")
    mFile.write(f"""
        module attacker
            zombies : [0..{zombiesList[-1]}] init 0;

            // Forced pass when pool is exhausted
            [] turn = 0 & time < maxTime & pool = 0 -> (zombies' = 0) & (turn' = 1);
    """)
    for z in zombiesList:
        k = z // 10  # pool units required
        mFile.write(f"""
            [] turn = 0 & time < maxTime & pool >= {k} -> (zombies' = {z}) & (turn' = 1);
        """)
    mFile.write("        endmodule\n")
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
    mFile = open(fileName, "a")
    mFile.write("""
        module ftr
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
    """Generate the vs module with packet-outcome transitions AND depletion transitions.

    Flow per round:
      vs_step=0  packet outcome (-U-> for FTR/RND, formula-based otherwise)
      vs_step=1  depletion step (player 3 adversarially removes zombies from pool)
                 then: time++, turn=0, vs_step=0
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

    # ── Packet-outcome transitions (vs_step=0) ────────────────────────────────
    for z in zombiesList:
        R_b = R_b_for_z(z)

        # RND uncertain transitions
        R_total          = R_b + R_l
        rnd_threshold    = R_total - BW
        p_b_cong_rnd     = BW / (2 * R_total)
        p_dr_cong_rnd    = (R_total - BW) / (2 * R_total)

        mFile.write(f"""
                // RND: zombies = {z}
                [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies = {z} & vs_step = 0 -U-> &p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + &p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + &p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + &p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1)
                {{ // non-congested: rnd*(R_b+R_l) >= (R_b+R_l-BW)
                    &rnd * {R_total} + (-{rnd_threshold}) >= 0,
                    {lb} <= &rnd,
                    &rnd <= {ub},
                    &p_b_receive_rnd = 0.5 + - &rnd * 0.5,
                    &p_l_receive_rnd = 0.5 + - &rnd * 0.5,
                    &p_b_drop_rnd = &rnd * 0.5,
                    &p_l_drop_rnd = &rnd * 0.5
                }};
                [] turn = 2 & time < maxTime & !parameters_selection & rnd_selected & zombies = {z} & vs_step = 0 -U-> &p_b_receive_rnd : (packet_status'=1) & (vs_step'=1)
                                                                        + &p_b_drop_rnd    : (packet_status'=2) & (vs_step'=1)
                                                                        + &p_l_receive_rnd : (packet_status'=3) & (vs_step'=1)
                                                                        + &p_l_drop_rnd    : (packet_status'=4) & (vs_step'=1)
                {{ // congested: rnd*(R_b+R_l) < (R_b+R_l-BW)
                    {rnd_threshold} >= &rnd * {R_total},
                    {lb} <= &rnd,
                    &rnd <= {ub},
                    &p_b_receive_rnd = {p_b_cong_rnd} + &rnd * 0,
                    &p_l_receive_rnd = {p_b_cong_rnd} + &rnd * 0,
                    &p_b_drop_rnd = {p_dr_cong_rnd} + &rnd * 0,
                    &p_l_drop_rnd = {p_dr_cong_rnd} + &rnd * 0
                }};
        """)

        # FTR uncertain transitions
        if ftr_polytope_feasible(z):
            noncong_rhs = R_b + R_l - BW
            mFile.write(f"""
                // FTR uncertainty: zombies = {z} (non-congested regime)
                [] turn = 2 & time < maxTime & !parameters_selection & ftr_selected & zombies = {z} & vs_step = 0 -U-> &p_b_receive_ftr : (packet_status'=1) & (vs_step'=1)
                                                                        + &p_b_drop_ftr    : (packet_status'=2) & (vs_step'=1)
                                                                        + &p_l_receive_ftr : (packet_status'=3) & (vs_step'=1)
                                                                        + &p_l_drop_ftr    : (packet_status'=4) & (vs_step'=1)
                {{ // non-congested: dfr x fpf with cross-constraint and non-congestion bound
                    {lb_dfr} <= &dfr,
                    &dfr <= {ub_dfr},
                    {lb_fpf} <= &fpf,
                    &fpf <= {ub_fpf},
                    &fpf + -&dfr * {k_ftr} >= {cross_rhs},
                    &dfr * {R_b} + &fpf * {R_l} >= {noncong_rhs:.1f},
                    &p_b_receive_ftr = 0.5 + -&dfr * 0.5,
                    &p_b_drop_ftr    = &dfr * 0.5,
                    &p_l_receive_ftr = 0.5 + -&fpf * 0.5,
                    &p_l_drop_ftr    = &fpf * 0.5
                }};
            """)

        if ftr_cong_feasible(z):
            R_in_base      = R_b + R_l
            p_receive_base = BW / R_in_base
            p_drop_base    = (R_in_base - BW) / R_in_base
            cong_rhs       = BW - R_b - R_l
            mFile.write(f"""
                // FTR uncertainty: zombies = {z} (congested regime)
                [] turn = 2 & time < maxTime & !parameters_selection & ftr_selected & zombies = {z} & vs_step = 0 -U-> &p_b_receive_ftr : (packet_status'=1) & (vs_step'=1)
                                                                        + &p_b_drop_ftr    : (packet_status'=2) & (vs_step'=1)
                                                                        + &p_l_receive_ftr : (packet_status'=3) & (vs_step'=1)
                                                                        + &p_l_drop_ftr    : (packet_status'=4) & (vs_step'=1)
                {{ // congested: dfr x fpf with cross-constraint and congestion bound
                    {lb_dfr} <= &dfr,
                    &dfr <= {ub_dfr},
                    {lb_fpf} <= &fpf,
                    &fpf <= {ub_fpf},
                    &fpf + -&dfr * {k_ftr} >= {cross_rhs},
                    -&dfr * {R_b} + -&fpf * {R_l} >= {cong_rhs:.1f},
                    &p_b_receive_ftr = {p_receive_base * 0.5:.10f} + -&dfr * {p_receive_base * 0.5:.10f},
                    &p_b_drop_ftr    = {p_drop_base * 0.5:.10f}    +  &dfr * {p_receive_base * 0.5:.10f},
                    &p_l_receive_ftr = {p_receive_base * 0.5:.10f} + -&fpf * {p_receive_base * 0.5:.10f},
                    &p_l_drop_ftr    = {p_drop_base * 0.5:.10f}    +  &fpf * {p_receive_base * 0.5:.10f}
                }};
            """)

    # ── Depletion transitions (vs_step=1) ─────────────────────────────────────
    #
    # Player 3 adversarially selects how many pool units are removed.
    # FTR: range [lb_dep, ub_dep] gives player 3 a genuine nondeterministic choice
    #      for z=70,80,90,100; deterministic for smaller z.
    # AGF: deterministic per dfr_index (already chosen during parameter selection).
    # Others: no depletion.
    # z=0 (pool exhausted): all dep=0 regardless of CM.

    all_z = [0] + zombiesList   # include 0 for pool=0 forced-pass case

    mFile.write("""
        // ── FTR depletion (adversarial within dfr detection range) ──────────
    """)
    for z in all_z:
        lb_dep, ub_dep = ftr_depletion_range(z)
        for dep in range(lb_dep, ub_dep + 1):
            mFile.write(f"""
        [] turn = 2 & time < maxTime & !parameters_selection & ftr_selected & zombies = {z} & vs_step = 1 -> (pool' = pool - {dep}) & (vs_step' = 0) & (packet_status' = 0) & (time' = time + 1) & (turn' = 0);""")
    mFile.write("\n")

    mFile.write("""
        // ── AGF depletion (deterministic, based on dfr_index chosen by player 3) ──
    """)
    for z in all_z:
        for dfr_i, dfr_val in AGF_DFR.items():
            dep = int(z * dfr_val / 10)
            mFile.write(f"""
        [] turn = 2 & time < maxTime & !parameters_selection & agf_selected & zombies = {z} & dfr_index = {dfr_i} & vs_step = 1 -> (pool' = pool - {dep}) & (vs_step' = 0) & (packet_status' = 0) & (time' = time + 1) & (turn' = 0);""")
    mFile.write("\n")

    mFile.write("""
        // ── No depletion: no_fix, RND, AGR, RDR ────────────────────────────
        [] turn = 2 & time < maxTime & !parameters_selection & !ftr_selected & !agf_selected & vs_step = 1 -> (vs_step' = 0) & (packet_status' = 0) & (time' = time + 1) & (turn' = 0);
    """)

    mFile.write("    endmodule\n")
    mFile.close()


# ── generate model ─────────────────────────────────────────────────────────────
zombiesList = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
fileName    = "baa_ftr.sgm"

gen_main(fileName)
gen_attacker(fileName, zombiesList)
gen_defender(fileName)
gen_mods(fileName)
gen_vs(fileName, zombiesList)

# ── append formulas and rewards ────────────────────────────────────────────────
mFile = open(fileName, "a")
mFile.write(f"""
                formula AF = 15.31;
                formula bogus_rate = 10 * AF;

                formula R_b = bogus_rate * zombies;
                formula R_l = 100.0;
                formula BW  = 458.0;

                formula R_b_in = {{
                    [] no_fix_selected | agr_selected : R_b,
                    [] ftr_selected | agf_selected    : R_b * (1 - dfr),
                    [] otherwise                      : R_b
                }};

                formula retries_multiplier = (retries=2 ? 4 : retries=4 ? 16 : 64);

                formula R_l_in = {{
                    [] no_fix_selected : R_l,
                    [] ftr_selected    : R_l * (1 - fpf),
                    [] rnd_selected    : R_l,
                    [] agr_selected    : R_l * retries_multiplier,
                    [] rdr_selected    : R_l * retries_multiplier,
                    [] otherwise       : R_l * retries_multiplier * (1 - fpf)
                }};

                formula R_b_out = {{
                    [] no_fix_selected | agr_selected : 0,
                    [] ftr_selected | agf_selected    : R_b * dfr,
                    [] otherwise                      : R_b
                }};

                formula R_l_out = {{
                    [] no_fix_selected | agr_selected : 0,
                    [] ftr_selected                   : R_l * fpf,
                    [] rnd_selected                   : R_l,
                    [] rdr_selected                   : R_l * retries_multiplier * rdf,
                    [] otherwise                      : R_l * retries_multiplier * fpf
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

                formula dfr = (dfr_index=0) ? 0.85 : (dfr_index=1) ? 0.90 : 0.95;
                formula fpf = (fpf_index=0) ? 0.05 : (fpf_index=1) ? 0.10 : 0.15;
                formula rdf = (rdf_index=0) ? 0.75 : (rdf_index=1) ? 0.85 : 0.95;

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
print(f"Cross-constraint slope k_ftr = {k_ftr}")
print()
print("FTR depletion per zombie count (pool units lost, in dfr range):")
for z in zombiesList:
    lb_dep, ub_dep = ftr_depletion_range(z)
    adv = " (adversarial)" if lb_dep < ub_dep else ""
    print(f"  z={z:>3}: dep in [{lb_dep}, {ub_dep}]{adv}")
print()
print("AGF depletion per zombie count (pool units lost, by dfr_index):")
for z in zombiesList:
    deps = [int(z * v / 10) for v in AGF_DFR.values()]
    print(f"  z={z:>3}: dep in {deps}  (dfr_index 0/1/2)")
