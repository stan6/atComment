package randoop;


import java.util.*;
import randoop.util.NullStatus;


public final class NullCheckingVisitor implements ExecutionVisitor {
  
private LinkedList<NullStatus> nullList;
  
  
  /**
   * Create a new visitor that checks the given contracts after the last
   * statement in a sequence is executed.
   *
   * @param nullList
   */
  public NullCheckingVisitor(List<NullStatus> statuses) {
    this.nullList = new LinkedList<NullStatus>();
    for (NullStatus status : statuses) {
      this.nullList.add(status);
    }
  }

  /**
   * Invoked by ExecutableSequence.execute before
   * the i-th statement executes.
   *
   * Precondition: statements 0..i-1 have been executed.
   */
  @Override
  public void visitBefore(ExecutableSequence s, int idx){
    //no body
  }
  
  /** 
   * Check if any violation of null annotation occurs
   */
  @Override
	public void visitAfter(ExecutableSequence s, int idx) {

		// We're only interested in statements at the end.
		if (idx < (s.sequence.size() - 1))
			return;

		if (s.hasFailure(idx)) {
			return;
		}
		if (s.hasNonExecutedStatements()) {
			return;
		}
			
		ExecutionOutcome result = s.getResult(idx);
		InferNullUtils infer = new InferNullUtils();
		NullStatus status = infer.matchStatementWithNull(s, idx,nullList);
		if (result instanceof ExceptionalExecution) {

			ExceptionalExecution e = (ExceptionalExecution) result;

			Throwable exception = e.getException();
			if (!(exception instanceof TimeOutException)) {
		
				if(status.getMethodSignature()!=null && (status.exceptionMap.size()>0))
				{	
					
					HashMap<String, String> exceptMap=status.exceptionMap; 
					String actualException = exception.getClass().getSimpleName();
					List<String> otherExcepts=status.getOtherexceptionNames();
						
					boolean matchCl = false;
					Class<?> actualClass = exception.getClass();
					for(String exceptStr:exceptMap.keySet()){
						try{
							Class<?> excepClss=Class.forName("java.lang."+exceptStr);
							if(actualClass.isAssignableFrom(excepClss) || excepClss.isAssignableFrom((Class<?>)actualClass))
								matchCl=true;
						}catch(ClassNotFoundException classnotfound){
							//ignore when class not found
						}
					}
					
					// comments say that an exception will be thrown and that exception is thrown
					if(exceptMap.containsKey("Exception") || otherExcepts.contains(actualException) || matchCl ){
						if(exceptMap.containsValue("NullSpecific") || exceptMap.containsValue("NonNull") || exceptMap.containsValue("YesNull")){
							if(otherExcepts.size()>1)
								status.excepType="Oth";
							else
								status.excepType="Exp";
							s.addCheck(idx, new DocCheck(idx, s, actualException,status, true), true);
						}
					}
					// comments say that normal execution but an exception is thrown
					else if(exceptMap.size()==1 && exceptMap.containsKey("Normal") && !otherExcepts.contains(actualException)){
						 if(exceptMap.containsValue("NullSpecific") || exceptMap.containsValue("NonNull") || exceptMap.containsValue("YesNull") || exceptMap.containsValue("OtherException")){
						status.excepType="Une";
					
						//if(status.getMethodSignature().contains("instantiateTransformer"))	{
						//		System.out.println("size:"+status.getMethodSignature()+":"+otherExcepts.size());
						//}
						//if(!infer.checkReceiverContainsNull(s,idx))
							s.addCheck(idx, new DocCheck(idx, s, actualException,status, false), false);
						//else{
						//	status.excepType="PonUne";
						//	s.addCheck(idx, new DocCheck(idx, s, actualException,status, false), false);
						//}
						}
					}
					
					// comments say that an exception will be thrown but another exception is thrown
					else if(!exceptMap.containsKey(actualException)){
					 if(exceptMap.containsValue("NullSpecific") || exceptMap.containsValue("NonNull") || exceptMap.containsValue("YesNull") || exceptMap.containsValue("OtherException")){
						status.excepType="Diff";
						if(!matchCl){
							//if(!infer.checkReceiverContainsNull(s,idx))
								s.addCheck(idx, new DocCheck(idx, s, actualException,status, false), false);
							//else{
							//	status.excepType="PonDiff";
							//	s.addCheck(idx, new DocCheck(idx, s, actualException,status, false), false);
							//}
						}
						} 
					}
					
				}
				return;
			}
		}

		else if (result instanceof NormalExecution) {
				HashMap<String, String> exceptMap=status.exceptionMap;	
				if(status.getMethodSignature()!=null && status.exceptionMap.size()!=0)
				{	
					
					String actualException = "Normal";
					// commments says normal execution and we got normal execution 
					if(exceptMap.containsKey("Normal")){
						status.excepType="ExpNormal";
						s.addCheck(idx, new DocCheck(idx, s, actualException,status, true), true);
					}
					// commments says exception should be thrown but got normal execution 
					else if(!exceptMap.containsKey("Normal")|| exceptMap.containsKey("Exception")){
						if(exceptMap.containsValue("NullSpecific") || exceptMap.containsValue("NonNull") || exceptMap.containsValue("YesNull") || exceptMap.containsValue("OtherException")){		
								status.excepType="Miss";
								s.addCheck(idx, new DocCheck(idx, s, actualException,status, false), false);
						}
					}
				}
		}
	}
  
	@Override
	public void initialize(ExecutableSequence sequence) {
		sequence.checks.clear();
		sequence.checksResults.clear();
		for (int i = 0; i < sequence.sequence.size(); i++) {
			sequence.checks.add(new LinkedList<Check>());
			sequence.checksResults.add(new LinkedList<Boolean>());
		}
	}

} 
