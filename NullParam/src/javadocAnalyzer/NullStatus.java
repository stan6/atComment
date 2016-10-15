package javadocAnalyzer;

import java.util.*;
/*
 * the fields equal to 1 if the parameter can be null, 0 if the parameter cannot be null
 *         or -1 if unknown or 2 if uninitialized 
 */
public class NullStatus {

	private int paramNull;
	private int throwIfNull;
	private String paramName = null;
	private String methodSignature = null;
	private String methodName = null;
	private String paramTypeName = null;
	private String exceptionName = null;
	public static List<String> expectedException = null;
	private int loc = 0;
	/**
	 * message relating null to an exception. This field can be null
	 */
	private String throwImplicationMsg = null;
	private int paramListSize = 0;
	private String className = null;
	
	public NullStatus() {
		setParamNull(null, 2);
		setThrowIfNull(2, null, null);
		expectedException = new LinkedList<String>();
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

	public String getExceptionName() {
		return exceptionName;
	}

	public void setClassName(String fileName) {
		this.className = fileName;

	}

	public String getClassName() {
		return this.className;
	}

	public void setExpectedException(String expectedException) {
		this.expectedException.add(expectedException);
	}

	public List<String> getExpectedException() {
		return expectedException;
	}
}
