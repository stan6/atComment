package randoop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import randoop.Sequence.RelativeNegativeIndex;
import randoop.util.ArrayListSimpleList;
import randoop.util.SimpleList;

public class NullForcer {
  /** Contains flags for different ways of forcing null */
  private static enum FORCE_TYPE {ONCE_EACH, ALL_COMBS};
  
  /** Flag to determine how we want to force null */
  private static FORCE_TYPE forceType = FORCE_TYPE.ONCE_EACH;
  
  /** Used for debugging -- will display info every time null is forced */
  private static boolean PRINT_FORCE_INFO = false;
  
  /** Flag for determining whether we're still forcing null for the previous sequence */
  public static boolean forceNullFromPrev = false;
   
  /** The original sequence that we started forcing, has all non-null inputs */
  private static Sequence origSequence;
  
  /** The most recently generated sequenced with forced null inputs */
  private static Sequence currentSequence;
  
  /** The number of times to repeat the last statement, for repeat heuristic */
  public static int repeatLast;
  
  /** The list of all combinations of input indexes that we'll be forcing null for the originalSequence */
  private static List<Set<Integer>> indexCombinations;
  
  public static void startForcingNull(Sequence sequence) {
    // Keep track of the original sequence, which should have no null declarations
    NullForcer.origSequence = sequence;
    
    // Set the flag to start forcing null
    forceNullFromPrev = true;
    
    // Generate all possible combinations of null indexes
    Set<Integer> knownInferenceIndexes = InferNullUtils.getKnownInferences(sequence.getLastStatement()).keySet();
    Set<Integer> indexesWithInference = new HashSet<Integer>(knownInferenceIndexes);
    
    indexCombinations = generateAllCombinations(indexesWithInference);
    
    if (PRINT_FORCE_INFO) {
      System.out.println("Start forcing for:\n" + sequence.toCodeString());
      System.out.println("Doing it " + indexCombinations.size() + " times, for indexes: ");
      for (Integer i : indexesWithInference) {
        System.out.println(i);
      }
      System.out.println("\n");
    }
  }

  public static void moveToNextNullForcedSequence(MultiVisitor executionVisitor) {
    Set<Integer> indexesToForce = indexCombinations.get(0);
    ExecutableSequence eSeq= new ExecutableSequence(origSequence);
    eSeq.execute(executionVisitor);
     if(!eSeq.isNormalExecution()){
	System.out.println("Execution already thrown for eSeq:"+eSeq);
	forceNullFromPrev=false;
        return;
     }    
 
    if (PRINT_FORCE_INFO) {
      String indexString = "";
      for (Integer i : indexesToForce) { 
        indexString += i + " ";
      }
      System.out.println("Forcing: " + indexString + " ... (" + (indexCombinations.size()-1) + " to go...)");
    }
    
    // Copy the list of statements from the very first, original sequence into a new ArrayList
    List<Statement> origStatements = new ArrayList<Statement>();
    SimpleList<Statement> origSimple = origSequence.getStatementsWithInputs();
    
    for (int i = 0; i < origSimple.size(); i++) {
      origStatements.add(origSimple.get(i));
    }
    
    for (int i = 0; i <= repeatLast; i++) {
      // Remove the last statement(s) from the new ArrayList -- we will replace it
      origStatements.remove(origStatements.size()-1);
    }

    for (Integer indexToForce : indexesToForce) {
      // Add 1 to the index since the list of variables has the receiver as the first element
      indexToForce++;
      
      // Create a null variable declaration for the param of the last statement that we're forcing null
      StatementKind newNullStatementKind = PrimitiveOrStringOrNullDecl.nullOrZeroDecl(origSequence.getLastStatementTypes().get(indexToForce));
      
      if (PRINT_FORCE_INFO) {
        System.out.println("Stmt for var at index: " + indexToForce + ": " + origSequence.getLastStatementVariables().get(indexToForce).getDeclaringStatement().toParseableString());
      }
        
      // Get the index of the statement that created the variable which we're forcing null
      int stmtIndexToReplace = origSequence.getLastStatementVariables().get(indexToForce).getDeclIndex();
  
      if (PRINT_FORCE_INFO) {
        System.out.println("Replacing variable at index: " + stmtIndexToReplace);
      }
      
      // Re-create that statement, but make it null this time
      Statement newNullStatement = new Statement(newNullStatementKind, new ArrayList<RelativeNegativeIndex>());
      
      // Replace the original statement with the new null statement
      origStatements.set(stmtIndexToReplace, newNullStatement);
    }
    
    // Copy all the statements from the ArrayList into a SimpleList so we can use it in the constructor
    ArrayListSimpleList<Statement> newSimple = new ArrayListSimpleList<Statement>();
    
    for (Statement s : origStatements) {
      newSimple.add(s);
    }
    
    // This will be the new sequence
    currentSequence = new Sequence(newSimple);    
    
    // Create a new list of Variables for the new last statement and fill it in with all the same variable
    // indices from the original
    List<Variable> newInputs = new ArrayList<Variable>();
    
    for (int i = 1; i < origSequence.getLastStatementVariables().size(); i++) {
      newInputs.add(new Variable(currentSequence, origSequence.getLastStatementVariables().get(i).getDeclIndex()));
    }
    
    // Put the last statement back on with the new inputs
    currentSequence = currentSequence.extend(origSequence.getLastStatement(), newInputs);
     
    // Repeat last statement if heuristic was used for originals
    if (repeatLast > 0) {
      currentSequence.repeatLast(repeatLast);
    }
    
    if (PRINT_FORCE_INFO) {
      System.out.println("\nORIG:\n" + origSequence.toCodeString());
      System.out.println("\nNEW:\n" + currentSequence.toCodeString());
    }

    // If we've gone through all combinations, stop forcing null for this statement.
    indexCombinations.remove(0);

    // If we've already forced them all, we're done forcing for this sequence
    if (indexCombinations.size() == 0) {
      forceNullFromPrev = false;
    }
  }

  public static Sequence getCurrentSequence() {
    return currentSequence;
  }
  public static Sequence getOrigSequence() {
    return origSequence;
  } 
  private static List<Set<Integer>> generateAllCombinations(Set<Integer> indexes) {
    List<Set<Integer>> combinations = new ArrayList<Set<Integer>>();
    
    switch(forceType) {
      // Generate a new sequence only once per inference-related parameter
      case ONCE_EACH:
        for (Integer index : indexes) {
          Set<Integer> indexSet = new HashSet<Integer>();
          indexSet.add(index);
          combinations.add(indexSet);
        }
        break;
      // Generate all possible combinations of null for every inference-related parameter
      case ALL_COMBS:
        combinations.add(indexes);
        generateCombinations(combinations, indexes, 0);
        break;
    }
    
    return combinations;
  }
  
  private static void generateCombinations(List<Set<Integer>> combList, Set<Integer> set, Integer itemIndexToRemove) {
    if (itemIndexToRemove == set.size())
      return;
    
    Set<Integer> newSet = new HashSet<Integer>(set);
    Iterator<Integer> iter = newSet.iterator();
    Integer intToRemove = null;
    
    for (int i = 0; i <= itemIndexToRemove; i++) {
      intToRemove = iter.next();
    }
    
    newSet.remove(intToRemove);
    
    if (newSet.size() > 0) {
      combList.add(newSet);
    }
    
    generateCombinations(combList, newSet, itemIndexToRemove);
    generateCombinations(combList, set, itemIndexToRemove + 1);
  }
}
