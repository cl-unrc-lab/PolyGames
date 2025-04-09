-- countermeasure = 0 : no_fix
-- countermeasure = 1 : ftr
-- countermeasure = 2 : rnd
-- countermeasure = 3 : agr
-- countermeasure = 4 : rdr
-- countermeasure = 5 : agf

r_b_in zombies countermeasure df rdf | countermeasure == 0 || countermeasure == 3 = (r_b zombies)
                                     | countermeasure == 1 || countermeasure == 5 = (r_b zombies) * (1 - df)
                                     | countermeasure == 2 || countermeasure == 4 = (r_b zombies) * (1 - rdf)

r_b zombies = 10 * 15.31 * zombies

r_l_in zombies countermeasure fpf rdf retries | countermeasure == 0 = r_l
                                              | countermeasure == 1 = r_l * (1 - fpf)
                                              | countermeasure == 2 = r_l * (1 - rdf)
                                              | countermeasure == 3 = r_l * 2**retries
                                              | countermeasure == 4 = r_l * 2**retries * (1 - rdf)
                                              | countermeasure == 5 = r_l * 2**retries * (1 - fpf)

r_l = 100

r_b_out zombies countermeasure df rdf | countermeasure == 0 || countermeasure == 3 = 0
                                      | countermeasure == 1 || countermeasure == 5 = (r_b zombies) * df
                                      | countermeasure == 2 || countermeasure == 4 = (r_b zombies) * rdf

r_l_out zombies countermeasure fpf rdf retries | countermeasure == 0 || countermeasure == 3 = 0
                                               | countermeasure == 1                        = r_l * fpf
                                               | countermeasure == 2                        = r_l * rdf
                                               | countermeasure == 4                        = r_l * 2**retries * rdf
                                               | countermeasure == 5                        = r_l * 2**retries * fpf

p_b_in zombies countermeasure df rdf  = if zombies == 0 then 0 else (r_b_in zombies countermeasure df rdf) / ((r_b_in zombies countermeasure df rdf) + (r_b_out zombies countermeasure df rdf))
p_l_in zombies countermeasure fpf rdf retries  = (r_l_in zombies countermeasure fpf rdf retries) / ((r_l_in zombies countermeasure fpf rdf retries) + (r_l_out zombies countermeasure fpf rdf retries))
p_b_out zombies countermeasure df rdf = if zombies == 0 then 1 else (r_b_out zombies countermeasure df rdf) / ((r_b_in zombies countermeasure df rdf) + (r_b_out zombies countermeasure df rdf))
p_l_out zombies countermeasure fpf rdf retries = (r_l_out zombies countermeasure fpf rdf retries) / ((r_l_in zombies countermeasure fpf rdf retries) + (r_l_out zombies countermeasure fpf rdf retries))

r_in zombies countermeasure df fpf rdf retries   = (r_b_in zombies countermeasure df rdf) + (r_l_in zombies countermeasure fpf rdf retries)
p_drop zombies countermeasure df fpf rdf retries = if bw >= (r_in zombies countermeasure df fpf rdf retries) then 0 else (((r_in zombies countermeasure df fpf rdf retries) - bw) / (r_in zombies countermeasure df fpf rdf retries))
p_receive zombies countermeasure df fpf rdf retries = 1 - (p_drop zombies countermeasure df fpf rdf retries)

bw = 458

p_b_receive zombies countermeasure df fpf rdf retries = (p_b_in zombies countermeasure df rdf) * (p_receive zombies countermeasure df fpf rdf retries)
p_l_receive zombies countermeasure df fpf rdf retries = (p_l_in zombies countermeasure fpf rdf retries) * (p_receive zombies countermeasure df fpf rdf retries)
p_b_drop zombies countermeasure df fpf rdf retries    = ((p_b_in zombies countermeasure df rdf) * (p_drop zombies countermeasure df fpf rdf retries)) + (p_b_out zombies countermeasure df rdf)
p_l_drop zombies countermeasure df fpf rdf retries    = ((p_l_in zombies countermeasure fpf rdf retries) * (p_drop zombies countermeasure df fpf rdf retries)) + (p_l_out zombies countermeasure fpf rdf retries)

bogus_probabilities = [ sum [p_b_receive z cm df fpf rdf r, p_b_drop z cm df fpf rdf r] | z <- zombies, cm <- countermeasures, df <- dfs, fpf <- fpfs, rdf <- rdfs, r <- retries]
legit_probabilities = [ sum [p_l_receive z cm df fpf rdf r, p_l_drop z cm df fpf rdf r] | z <- zombies, cm <- countermeasures, df <- dfs, fpf <- fpfs, rdf <- rdfs, r <- retries]

zombies = [0, 20, 40, 60, 80, 100]
countermeasures = [0..5]
dfs  = [0.85, 0.90, 0.95]
fpfs = [0.05, 0.10, 0.15]
rdfs = [0.75, 0.85, 0.95]
retries = [2, 4, 6]

-- Get the set of parameters that make the sum (r_b_in z c df rdf) + (r_b_out z c df rdf) equal to 0, making p_b_in and p_b_out infeasible. 
-- filter (\(z, c, df, rdf) -> ((r_b_in z c df rdf) + (r_b_out z c df rdf)) == 0) [ (z, c, df, rdf) | z <- zombies, c <- countermeasures, df <- dfs, rdf <- rdfs ]
