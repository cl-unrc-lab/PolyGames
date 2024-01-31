package parser.ast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.HashMap;

import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;
import prism.PrismException;

import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Parma_Polyhedra_Library;
import parma_polyhedra_library.Variable;
import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.NNC_Polyhedron;
import parma_polyhedra_library.Generator_Type;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Sum;
import parma_polyhedra_library.Linear_Expression_Times;
import parma_polyhedra_library.Linear_Expression_Coefficient;
import parma_polyhedra_library.Polyhedron;
import parma_polyhedra_library.Constraint_System;


import parma_polyhedra_library.Generator;
import parma_polyhedra_library.Generator_System;
import parma_polyhedra_library.Relation_Symbol;
import explicit.PPLSupport;

public class UncertainUpdates extends Updates{
	enum relOps{
		GE,
		LE
	}
    private ArrayList<Expression> uncertains; // the list of uncertains
    private HashMap<String, HashMap<Integer, Double>> coefficients;  // coefficients contains for each uncertain the corresponding column of coefficients
    																 // for instance, coefficients.get(uncertain).get(i) returns the coefficiente corresponding to row i, null if none
    private ArrayList<Double> constants;
    private ArrayList<relOps> ineqs; // the ineqs in the constraints: Relation_Symbol.LESS_OR_EQUAL, or Relation_Symbol.G
    int div = 1; // the divisor allows us to move the decimal point, PPL only allows for integers.
    //int numberUncertains;
    
    /**
     * Basic constructor, it initializes the object, all the coefficients and constants are initialized to zero,
     * we assume that, when a uncertain is added, the coefficient is set to a number different from 0
     */
    public UncertainUpdates() {
        super();
        this.uncertains   = new ArrayList<Expression>();
        //this.coefficients = new double[10][10]; // we have to do a resize of arrays
        //this.constants = new double[10];
        this.coefficients = new HashMap<String, HashMap<Integer, Double>>();
        this.constants = new ArrayList<Double>();
        this.ineqs = new ArrayList<relOps>();
        
    }

   /**
    * @param Expression
    * @param Up
    */
    public void addUpdate(Expression un, Update up) {
        if (un == null) { throw new IllegalArgumentException(); }
        if (up == null) { throw new IllegalArgumentException(); }
        
        this.uncertains.add(un);
        //this.updates.add(up);
        super.addUpdate(un, up); // we call the super version
        //up.setParent(this);
    }

    /**
     * 
     * @param i
     * @param un
     */
    public void setUncertain(int i, Expression un) {
        if (un == null) { throw new IllegalArgumentException(); }
        this.uncertains.set(i, un);
    }

    /**
     * 
     * @param i
     * @return
     */
    public Expression getUncertain(int i) { return this.uncertains.get(i); }
    

    /**
     * 
     * @return
     */
    public int getNumberUncertains() {
    	return this.uncertains.size();
    }

    /**
     * 
     * @return	The divisor indicating the number of decimals that one needs to shift the numbers
     */
    public int getDivisor() {
    	return div;
    }
    /**
     * @param c
     * @param i			the row of the coefficient
     * @param uncertain	the uncertain to which the coefficient applies
     */
    public void addCoefficient(double c, int i, UncertainExpression uncertain)
    {	
    	// first, we determine the number of decimals in the coefficient
    	// and normalise the number
    	int dec = 0;
    	double newc = c;
    	while ((newc - (int) newc) != 0){
    		newc = newc * 10;
    		dec++;
    	}
    	// we update the divisor
    	div = Math.max(div, dec);
    	
    	
    	String uncertainName = uncertain.getName();
    	
    	// we check if the row for the uncertain was initialised
    	if (this.coefficients.get(uncertainName) == null)
    		this.coefficients.put(uncertainName, new HashMap<Integer, Double>());
    	
    	this.coefficients.get(uncertainName).put(i, c);
    }

    public void addConstant(double c, int i, String rel) {
    	
    	// first, we determine the number of decimals in the coefficient
    	// and normalise the number
    	int dec = 0;
    	double newc = c;
    	while ((newc - (int) newc) != 0){
    		newc = newc * 10;
    		dec++;
    	}
    	// we update the divisor
    	div = Math.max(div, dec);
    	
    	// we add the constants
        this.constants.add(i, c);
        
        switch (rel) {
        	case "<=":
        		this.ineqs.add(relOps.LE);
        		break;
        	case ">=":
        		this.ineqs.add(relOps.GE);
        		break;
        	default:
        		throw new IllegalArgumentException("Invalid inequality: " + rel);
        }
        
    }
    
    /**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}
	
	@Override
	public String toString() {
		String result = "";
		
		for (int j = 0; j < this.constants.size(); j++) {
			for (int i = 0; i < this.uncertains.size(); i++) {
				String uncertain = ((UncertainExpression) this.uncertains.get(i)).getName();
				result += this.coefficients.get(uncertain).get(j) + uncertain;
			}
			result += " <=" + constants.get(j); 
		}
		
		return result;
	}
	
	@Override
	public UncertainUpdates deepCopy(DeepCopy copier) throws PrismLangException
	{
		super.deepCopy(copier);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UncertainUpdates clone()
	{
		UncertainUpdates clone = (UncertainUpdates) super.clone();

		// clone.probs   = (ArrayList<Expression>) probs.clone();
		// clone.updates = (ArrayList<Update>) updates.clone();
		clone.uncertains = (ArrayList<Expression>) uncertains.clone();
		clone.coefficients = (HashMap<String, HashMap<Integer, Double>>) coefficients.clone();
		clone.constants = (ArrayList<Double>) constants.clone();
		return clone;
	}
	
	/**
	 * @return The PPL constrain system corresponding to the equations in the uncertain system
	 */
	public Constraint_System getPPLConstraintSystem(){
		
		Constraint_System result = new Constraint_System(); // the result
		
		// We init PPL
		try {
			PPLSupport.initPPL();
		}
		catch(PrismException e) {
			System.out.println("Error Loading PPL Library");
		}
		
		// we define an array of variables for the uncertains
		// the uncertains are mapped using their index in {@code uncertains}
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for (int i = 0; i < this.uncertains.size() ; i++) {
			vars.add(new Variable(i));
		}
		
		// we check if the columns of coefficients has the correct size for each constant
		//for (int i = 0; i < uncertains.size(); i++) {
		//	String ue = ((UncertainExpression) uncertains.get(i)).getName();
		//	if (coefficients.get(ue).size() < constants.size()) {
		//		for (int j = coefficients.get(ue).size() - 1; j < constants.size(); j++ ) {
		//			coefficients.get(ue).add(j, 0.0); // we complete the uncertains with 0.0
		//		}
		//	}
		//}
		
		
		Linear_Expression[] exprs = new Linear_Expression[this.constants.size()];
		
		
		// check if there is no uncertains, an error must be thrown
		
		// we build the equation system, one equation for each constant
		for (int i = 0; i < this.constants.size(); i++) {
			Linear_Expression lexp = new Linear_Expression_Coefficient(new Coefficient(0)); // we start with 0
			for (int j = 0; j < this.uncertains.size(); j++){
				String uncertainName = ((UncertainExpression) this.uncertains.get(j)).getName();
				if (this.coefficients.get(((UncertainExpression) this.uncertains.get(j)).getName()).get(i) != null) {
					Integer coef = this.coefficients.get(uncertainName).get(i).intValue();
					Linear_Expression times = new Linear_Expression_Times(new Coefficient(coef), vars.get(j));
					lexp = new Linear_Expression_Sum(times, lexp); // we sum the expression
				}
			}
			exprs[i] = lexp;
		}
		
		// TO DO: Add other relations
		// we build the constrain system
		for (int i = 0; i < this.constants.size(); i++) {
			Constraint c = new Constraint(exprs[i], this.ineqs.get(i) == relOps.LE ? Relation_Symbol.LESS_OR_EQUAL : Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(constants.get(i).intValue())));
			result.add(c);
		}
		// we add restrictions for the variables, every var is between 0 and 1
		for (int i = 0; i < vars.size(); i++) {
			Constraint c1 = new Constraint(new Linear_Expression_Variable(vars.get(i)), Relation_Symbol.LESS_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(1)));
			Constraint c2 = new Constraint(new Linear_Expression_Variable(vars.get(i)), Relation_Symbol.GREATER_OR_EQUAL, new Linear_Expression_Coefficient(new Coefficient(0)));
			result.add(c1);
			result.add(c2);
		}
		
		// Furthermore, the sum of all the vertices has to be 1
		
		
		return result;
	}
	
	/**
	 * It converts all the coefficients to int, this is needed for PPL
	 */
	public void convertToInt(){
		
		for (int i = 0; i < this.uncertains.size(); i++) {
			String uncertainName = ((UncertainExpression) this.uncertains.get(i)).getName();
			for (int j = 0; j < this.constants.size(); j++) {
				if (this.coefficients.get(uncertainName).get(j) != null) {
					this.coefficients.get(uncertainName).put(j, this.coefficients.get(uncertainName).get(j) * Math.pow(10, div));
				}
			}
		}
		
		for (int i = 0; i < this.constants.size(); i++) {
			this.constants.set(i, this.constants.get(i) * Math.pow(10, div));
		}
	}
	
	
}
