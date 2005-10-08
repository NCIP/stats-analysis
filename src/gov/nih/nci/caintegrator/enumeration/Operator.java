package gov.nih.nci.caintegrator.enumeration;


/**
 * @author sahnih
 * This enum describes various operators that
 * are used through out the caIntegrator to formulate criteria.
 *
 */
public enum Operator {
	GT, //greater than
	LT, //less than
	EQ, //equals
	NE, //not equal to
	GE, //greater than or equal to
	LE, //less than or equal to
    AND,// Intersection 
    OR ,//Union
    NOT //Difference
}
