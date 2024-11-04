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
    private ArrayList<Double> constants;							 // constains the columns of constants in the equations
    private ArrayList<relOps> ineqs;								 // the ineqs in the constraints: Relation_Symbol.LESS_OR_EQUAL, or Relation_Symbol.G
    int div = 1; 													 // the divisor allows us to move the decimal point, PPL only allows for integers.
    int precision = 6; 												 // this is the precision, after that we truncate the number	

    /**
     * Basic constructor, it initializes the object, all the coefficients and constants are initialized to zero,
     * we assume that, when a uncertain is added, the coefficient is set to a number different from 0
     */
    public UncertainUpdates() {
        super();
        this.uncertains   = new ArrayList<Expression>();
        this.coefficients = new HashMap<String, HashMap<Integer, Double>>();
        this.constants    = new ArrayList<Double>();
        this.ineqs        = new ArrayList<relOps>();
        
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
     * 
     * @return the precision of the constants and coefficients
     */
    public int getPrecision() {
    	return precision;
    }
    /**
     * @param c
     * @param i			the row of the coefficient
     * @param uncertain	the uncertain to which the coefficient applies
     */
    public void addCoefficient(double c, int i, UncertainExpression uncertain, boolean isInLeftSide)
    {	
    	// first, we determine the number of decimals in the coefficient
    	// and normalise the number
    	//int dec = 0;
    	//double newc = c;
    	//while ((newc - (int) newc) != 0 && dec <= precision){
    	//	System.out.println("double:"+newc);
    	//	System.out.println("int part:"+(int) newc);
    		
    	//	newc = newc * 10;
    	//	dec++;
    	//}
    	// we update the divisor
    	//div = Math.max(div, dec);
    	
    	
    	String uncertainName = uncertain.getName();
    	
    	// we check if the row for the uncertain was initialised
    	if (this.coefficients.get(uncertainName) == null)
    		this.coefficients.put(uncertainName, new HashMap<Integer, Double>());
    	
    	if (!isInLeftSide) {
				c = -c;
			}

			if (this.coefficients.get(uncertainName).get(i) == null) { // if the uncertain hasn't a mapped coefficient
				this.coefficients.get(uncertainName).put(i, c);
			} else { // if the uncertain has a mapped coefficient
				this.coefficients.get(uncertainName).put(i, c + this.coefficients.get(uncertainName).get(i));
			}
    }

    public void addConstant(double c, int i, boolean isInLeftSide) {
    	// first, we determine the number of decimals in the coefficient
    	// and normalise the number
    	//int dec = 0;
    	//double newc = c;
    	//while ((newc - (int) newc) != 0){
    	//	newc = newc * 10;
    	//	dec++;
    	//}
    	// we update the divisor
    	//div = Math.max(div, dec);

			if (isInLeftSide)
				c = -c;
    	
			if ( i < this.constants.size() ) { // if there is already a constant for the i-th row then
				this.constants.set(i, c + this.constants.get(i));
			} else {
				this.constants.add(i, c);
			}
    }

		public void setInequalitySymbol(String inequalitySymbol) {
			switch (inequalitySymbol) {
				case "<=":
					this.ineqs.add(relOps.LE);
					break;
				case ">=":
					this.ineqs.add(relOps.GE);
					break;
				default:
					throw new IllegalArgumentException("Invalid inequality symbol: " + inequalitySymbol);
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
		
		// An array of expressions, exprs[i] corresponds to the left expression in the ith row  of the equation system
		Linear_Expression[] exprs = new Linear_Expression[this.constants.size()];
		
		// We build the equation system, one equation for each constant: expr <> constants[i]
		for (int i = 0; i < this.constants.size(); i++) {
			Linear_Expression lexp = new Linear_Expression_Coefficient(new Coefficient(0)); // we start with 0
			for (int j = 0; j < this.uncertains.size(); j++){ // for each uncertain we construct the rows
				String uncertainName = ((UncertainExpression) this.uncertains.get(j)).getName();
				if (this.coefficients.get(((UncertainExpression) this.uncertains.get(j)).getName()).get(i) != null) { // we get the corresponding coefficient to the uncertain
					Integer coef = this.coefficients.get(uncertainName).get(i).intValue();
					Linear_Expression times = new Linear_Expression_Times(new Coefficient(coef), vars.get(j));
					lexp = new Linear_Expression_Sum(times, lexp); // we sum the expression to create the corresponding expression for the row
				}
			}
			exprs[i] = lexp;
		}
		
		// TO DO: Add other relations:  Equality for example
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
		Linear_Expression lexp = new Linear_Expression_Coefficient(new Coefficient(0));
		for (int i = 0; i < vars.size(); i++) {
			lexp = new Linear_Expression_Sum(new Linear_Expression_Variable(vars.get(i)), lexp);
		}
		Constraint c = new Constraint(lexp, Relation_Symbol.EQUAL, new Linear_Expression_Coefficient(new Coefficient(1)));
		result.add(c);
		
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
					this.coefficients.get(uncertainName).put(j, Math.floor(this.coefficients.get(uncertainName).get(j) * Math.pow(10, precision)));
				}
			}
		}
		
		for (int i = 0; i < this.constants.size(); i++) {
			this.constants.set(i, Math.floor(this.constants.get(i) * Math.pow(10, precision)));
		}
	}
	
	
}
