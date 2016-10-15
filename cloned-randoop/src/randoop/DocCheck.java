package randoop;

import java.util.ArrayList;
import java.util.List;
import randoop.util.*;
import java.util.HashMap;
import java.util.Map;
/**
 * This class implements the check for Javadoc.A sequence containing
 * the check will be removed if the null information in Javadoc
 * matches with the outcome of the execution for each sequence  
 * 
 * @author shinhwei
 *
 */
public class DocCheck implements Check {
	private static final long serialVersionUID = 6915136819752903798L;
	  
	  // Indicates which statement results in the given exception.
	  private final int statementIdx;
	  private List<ExecutableSequence> sequences = new ArrayList<ExecutableSequence>();
	  public List<String> expectedException = new ArrayList<String>();
	  private String actualException;
	  public String methodSign;
	  public String className;
	  public String annoType;
	  //indicate whether the Javadoc matches the execution outcome
	  public boolean match=false;
	  
	 
	  public DocCheck(int statementIdx, ExecutableSequence seq,String actualException,NullStatus status, boolean match) {
	    this.className = status.className;
	    this.methodSign = status.getMethodSignature();
	    this.annoType = status.excepType;
	    this.actualException = actualException;
  	    this.statementIdx = statementIdx;
  	    this.match = match;
  	    this.sequences.add(seq);
	    for(String exception : status.exceptionMap.keySet()){
			String nullRelated=":null(";
			if(!exception.startsWith(nullRelated))
	   		 	this.expectedException.add(exception);
			else
				this.expectedException.add(exception.substring(nullRelated.length()));
		}
	  }


	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (o == this) return true;
	    if (!(o instanceof DocCheck)) return false;
	    DocCheck other = (DocCheck)o;
	    return statementIdx == other.statementIdx && sequences.equals(other.sequences);
	  }
	  
	  @Override
	  public int hashCode() {
		  int h = 7;
		    h = h * 31 + expectedException.hashCode();
		    h = h * 31 + new Integer(statementIdx).hashCode();
		    return h;
	  }

	  @Override
	  public String get_value() {
	      return "DocError"+statementIdx;
	  }

	  public String toString() {
		    return "// expect exception of type " + expectedException + Globals.lineSep;
	  }
	  
	  @Override
	  public int get_stmt_no() {
	    return statementIdx;
	  }

	  @Override
	  public String get_id() {
	    return "DocCheck @" + statementIdx;
	  }

	  @Override
	  public String toCodeStringPostStatement() {
		    StringBuilder b = new StringBuilder();
		    String exceptionClassName = getExcepted();
		    if(annoType.equals("ExpNormal")){
				System.out.println("\nSkipping Expected Normal "+methodSign+ " for ExpNormal "+ getExcepted() +";");
		    }
		    else{
			    b.append("  fail(\"Expected exception of type " + exceptionClassName 
		                + " but got "+ actualException + "\");" + Globals.lineSep);
			    if(!exceptionClassName.equals("Normal")){
				for(String exception:this.expectedException){
				    b.append("} catch (");
				    b.append(exception);
				    b.append(" e) {" + Globals.lineSep);
				    b.append("  // Expected exception." + Globals.lineSep);
				    b.append("}" + Globals.lineSep);
				}
			    }
		}
		    return b.toString();
	  }

	  @Override
	  public String toCodeStringPreStatement() {
		  	StringBuilder b = new StringBuilder();
			String exceptionClassName = getExcepted();
			if(annoType.equals("ExpNormal")){
				System.out.println("\nSkipping Expected Normal "+methodSign+ " for ExpNormal "+ getExcepted() +";");
				return b.toString();
			}
			else if(exceptionClassName.equals("Normal"))
				b.append("// Expected runs normally but got "+ actualException+ " according to the Javadoc comments for " + className + "#"+ methodSign + " because "+annoType+ Globals.lineSep);
                        //check for both SpecifiException and AnyException
			else
		    		b.append("// Expected exception of type "+ exceptionClassName +" but got "+ actualException+ " according to the Javadoc comments for " + className + "#"+ methodSign +  " because "+annoType + Globals.lineSep);
		   if(!exceptionClassName.equals("Normal"))
		    	b.append("try {" + Globals.lineSep);
		    return b.toString();
	  }

	@Override
	public boolean evaluate(Execution execution) {
		InferNullUtils infer=new InferNullUtils();
		ExecutionOutcome result = execution.get(statementIdx);
		if (result instanceof ExceptionalExecution) {

			ExceptionalExecution e = (ExceptionalExecution) result;

			Throwable exception = e.getException();
			if (!(exception instanceof TimeOutException)) {
				if (!this.expectedException.contains(exception
						.getClass().getSimpleName())) {
					if(!infer.checkLocSeqContainsNull(sequences.get(statementIdx), statementIdx).isEmpty()){
						return false;
					}
				}
			}
		} else if (result instanceof NormalExecution) {
			NormalExecution e = (NormalExecution) result;
				if (!this.expectedException.isEmpty() && !infer.checkLocSeqContainsNull(sequences.get(statementIdx), statementIdx).isEmpty() ) {
					return false;
				}
		}
		
		return true;
	}
	
	public String getExcepted(){
		String temp="";
		int pos=0;
		for(String exception:this.expectedException){
			temp+=exception;
			if(pos<this.expectedException.size()-1)
				temp+=":";
			pos++;
		}
		return temp;
	}
}
