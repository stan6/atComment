# @tComment: Testing Javadoc Comments to Detect Comment-Code Inconsistencies
@tComment consists of two components: (1) NullParam, and (2) cloned-randoop

NullParam:
- uses the default Doclet provided with the Javadoc tool to parse the Javadoc comments in a given project
- Automatically generates null related properties inferred from the Javadoc comments

cloned-randoop:
- modified Randoop
- Automatically generates tests that checks for comment-code inconsistencies automatically:


Below are the instructions for compiling the components in @tComment and to run one of the subject programs in the paper: ApacheCommonsCollections:
1. To compile the NullParam project:

./run build_doclet

2. To compile cloned-randoop project:

./run build_randoop 

3. To download ApacheCommonsCollections project, this will download the project into a subdirectory "programs/collections":

./compile_proj collections

4. Now, ready to run the script for Apache Commons Collections:

./run collections -n 0.6 -t 50 -o

USAGE: 

  run [-?dnti]

OPTIONS:

  -d  directory name. Default is Null[$NULLRATIO]Time[$TIMELIMIT]Input[$INPUTLIMIT]_WITH_COMMENTS

  -n  null ratio. Default is 0.5 (double)

  -t  time limit in seconds. Default is 100 (int)

  -i  input limit. Default is 100000000 (int)

  -o  Runs randoop only once

  -r  Runs doclet only

  -?  this usage information

EXAMPLE:

  run jodatime -n 0.4 -t 200 -r true


Output:

There are two kinds of output generated:

1. Inferred null properties. You can refer to all the infer properties for each Java file by using the command: find . -name "*.infer"

2. JUnit Test. At the end of the execution for run command (e.g., ./run collections -n 0.6 -t 50 -o), you will get a message indicating the location for the generated junit test like below:

Created file: $DIR/RandoopTest0.java

...

Created file: $DIR/RandoopTest.java

File RandoopTest[0-9]+.java are the file with specific tests while RandoopTest.java is the test class that include all the generated RandoopTest[0-9]+.java.
