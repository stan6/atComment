package randoop.util;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
/*
 * the fields equal to 1 if the parameter can be null, 0 if the parameter cannot be null
 *         or -1 if unknown or 2 if uninitialized 
 *         
 * if (paramNull == 1)
 *    Normal
 * else if (paramNull == 0 && throwIfNull == 2)
 *    AnyException
 * else if (paramNull == 0 && throwIfNull == 1)
 *    SpecificException
 *    
 */
public class NullStatus {

	private int paramNull;
	private int throwIfNull;
	private String paramName = null;
	private String methodSignature = null;
	private String methodName = null;
	private String paramTypeName = null;
	private String exceptionName = null;
	public String className = null;
	private int loc = 0;
  public HashMap<String,String> exceptionMap=null;
  public String excepType=null;
  // Added for storing OtherExceptions in order   
       private List<String> otherExceptionList = null;
	/**
	 * message relating null to an exception. This field can be null
	 */
	private String throwImplicationMsg = null;
	private int paramListSize = 0;
	
	public NullStatus() {
		setParamNull(null, 2);
		setThrowIfNull(2, null, null);
		otherExceptionList = new LinkedList<String>();
	}

	public NullStatus(String paramName) {
		setParamNull(null, 2);
		setThrowIfNull(2, null, null);
		setParamName(paramName);
	}

	public NullStatus(int paramNull, int throwNull) {
		setParamNull(getParamName(), paramNull);
		setThrowIfNull(throwNull, null, null);
	}

	public NullStatus(String metName, String signature, String className,
			int argSize) {
		setMethodName(metName);
		setMethodSignature(signature);
		setParamListSize(argSize);
	}

	public void setParamNull(String paramName, int paramNull) {
		setParamName(paramName);
		this.paramNull = paramNull;
	}

	public void setParamNull(int paramNull) {
		this.paramNull = paramNull;
	}

	public int getParamNull() {
		return paramNull;
	}

	public void setThrowIfNull(int throwIfNull, String throwMsg,
			String exceptionName) {
		this.throwIfNull = throwIfNull;
		setThrowImplication(throwMsg);
		setExceptionName(exceptionName);
	}

	public void setThrowIfNull(int throwIfNull, String throwMsg) {
		this.throwIfNull = throwIfNull;
		setThrowImplication(throwMsg);
	}

	public int getThrowIfNull() {
		return throwIfNull;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamName() {
		return paramName;
	}

	public void setThrowImplication(String nullComment) {
		this.throwImplicationMsg = nullComment;
	}

	public String getThrowImplication() {
		return throwImplicationMsg;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setTypeName(String paramTypeName, int locInList) {
		this.paramTypeName = paramTypeName;
		this.loc = locInList;
	}

	public String getTypeName() {
		return paramTypeName;
	}

	public int getLoc() {
		return loc;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setParamListSize(int size) {
		this.paramListSize = size;
	}

	public int getParamListSize() {
		return paramListSize;
	}

	public void setExceptionName(String exceptionName) {
		this.exceptionName = exceptionName;
	}
	// Sets OtherExceptions 
	public void setOtherexceptionNames(String exceptionName)
	{
	  	this.otherExceptionList.add(exceptionName);
	}
	// Sets OtherExceptions 
	public List<String> getOtherexceptionNames()
	{
	  	return this.otherExceptionList;
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public void setClassName(String fileName) {
		this.className = fileName;

	}

	public String getClassName() {
		return this.className;
	}

  public String setTypeNameAndPos() {
  
    // Added for OtherException
    if (this != null && getOtherexceptionNames() != null & getOtherexceptionNames().size() > 0)
      return "";
    String paramName = getParamName();
    int openParen = getMethodSignature().indexOf("(");
    int closeParen = getMethodSignature().indexOf(")");
    String paramList = "";
    String paramTypeName = "";
    if (openParen != -1 && closeParen != -1 && (openParen + 1) != closeParen && (openParen + 1) <= getMethodSignature().length() && (closeParen - 1) > 0) {
      paramList = getMethodSignature().substring(openParen + 1, closeParen);
  
      if (getParamName() == null) {
        return null;
      }
      int beforeType = paramList.indexOf(" " + paramName);
      if (beforeType != -1)
        beforeType = beforeType + 1;
      if (beforeType != -1 && beforeType - 1 > 0) {
        int start = 0;
        int end = 0;
        int countPos = 0;
  
        for (int index = 0; index < paramList.length(); index++) {
          if (paramList.charAt(index) == ' ' && (index + 1) == beforeType) {
            end = index;
            paramTypeName = paramList.substring(start, end);
            setTypeName(paramTypeName, countPos);
          } else if (paramList.charAt(index) == ',') {
            start = index + 1;
            countPos++;
          }
        }
        if (closeParen == openParen + 1) {
          setParamListSize(0);
        } else {
          setParamListSize(countPos + 1);
        }
      }
    }
  
    return paramTypeName;
  }
}
