package java2rsf;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

/**
 * Takes Java source files  as input and creates the Rsf output for 
 * them.
 * 
 * <p>Example of usage:<br>
 * <code>java -jar rsfparser.jar MyProject\src -r C:\Project\src -e UTF-8</code></p>
 */
public class Main {

    /*
     * Do not instantiate.
     */
    private Main() {
    }

    /**
     * Parses the specified Java source files according to the command line arguments. And prints 
     * the Rsf output to the specified location.
     * 
     * <p><pre>
     * Usage: java -jar rsfparser.jar &lt;inputFile&gt; [-r] [-c &lt;classpath&gt;] 
     * [-l &lt;javaVersion&gt;] [-e &lt;encoding&gt;] [-v]
     *   &lt;inputFile&gt;
     *         Set the .java file or directory to create Rsf output from.
     *         For a directory, all .java files in the directory will be added, but
     *         files in its subdirectories will not be added.
     * 
     *   [-r]
     *         If the input file is a directory, adds all .java files in its
     *         sudirectories.
     * 
     *   [-c &lt;classpath&gt;]
     *         Name a directory or .jar file that contains Java classes that are
     *         referenced in the input files.
     *         Can be used multiple times
     * 
     *   [-l &lt;javaVersion&gt;]
     *         Set the java language version of the .java files, can be 1.1, 1.2, 1.3,
     *         1.4, 1.5 or 1.6, defaults to 1.6
     * 
     *   [-e &lt;encoding&gt;]
     *         Set the encoding of the .java files, defaults to the system default
     *         encoding
     * 
     *   [-v]
     *         Give feedback about which file is parsed at the moment.
     * </pre></p>
     * 
     * <p>For more information about the parameters, see {@link JdtCompiler}</p>
     * 
     * @param args command line arguments
     * @throws java.io.IOException in case of an IO error while reading the input files
     * @throws rsfparser.CompilationException in case parsing the input files fails
     */
    public static void main(String[] args) throws IOException, CompilationException {
        JSAP jsap = getOptions();
        JSAPResult config = jsap.parse(args);
        
        //print help on error
        if (!config.success()) {
            System.err.println("\nUsage: java -jar rsfparser.jar " + jsap.getUsage());
            System.err.println(jsap.getHelp());
            System.err.println();
            System.exit(1);
        }


        List<String> inputFileNames = new ArrayList<String>();
        File initialFile = new File(config.getString("inputFile"));
        boolean searchSubdirectories = config.getBoolean("searchSubdirectories");
        getInputFiles(initialFile, searchSubdirectories, inputFileNames);
        if(inputFileNames.isEmpty()){
            throw new IllegalArgumentException("No .java files found.");
        }

        SortedSet<String> lines = new TreeSet<String>();
        for (String inputFileName : inputFileNames) {
            if(config.getBoolean("verbose")){
                System.err.println(inputFileName);    
            }
            
            JdtCompiler jdtCompiler = new JdtCompiler(inputFileName);

            //don't need to specify folder twice, if parsing whole package
            if(initialFile.isDirectory()){
                jdtCompiler.addClasspath(initialFile.getAbsolutePath());
            }
            
            for (String classpathName : config.getStringArray("classpath")) {
                jdtCompiler.addClasspath(classpathName);
            }

            if (config.contains("javaVersion")) {
                JdtCompiler.SourceLevel sourceLevel = JdtCompiler.SourceLevel.fromString(config.getString("javaVersion"));
                jdtCompiler.setSourceLevel(sourceLevel);
            }

            if (config.contains("encoding")) {
                Charset charset = Charset.forName(config.getString("encoding"));
                jdtCompiler.setEncoding(charset);
            }

            //create output
            CompilationUnitDeclaration unit = jdtCompiler.getResolvedUnit();
            unit.traverse(new DeclarationVisitor(lines), unit.scope);
        
        }

        //print output
        for (String line : lines) {
            System.out.println(line);
        }
        System.out.flush();
    }

    /*
     * Convenience method that creates the JSAP Options object.
     */
    private static JSAP getOptions() {
        JSAP jsap = new JSAP();

        try {
            UnflaggedOption inputOption = new UnflaggedOption("inputFile");
            inputOption.setRequired(true);
            inputOption.setHelp("Set the .java file or directory to create Rsf output from. " +
                    "\nFor a directory, all .java files in the directory will be added, but files " +
                    "in its subdirectories will not be added.");
            jsap.registerParameter(inputOption);

            Switch searchSubdirectories = new Switch("searchSubdirectories");
            searchSubdirectories.setShortFlag('r');
            searchSubdirectories.setHelp("If the input file is a directory, adds all .java files " +
                    "in its sudirectories.");
            jsap.registerParameter(searchSubdirectories);

            FlaggedOption classpathOption = new FlaggedOption("classpath");
            classpathOption.setShortFlag('c');
            classpathOption.setAllowMultipleDeclarations(true);
            classpathOption.setHelp("Name a directory or .jar file that contains Java classes " +
                    "that are referenced in the input files. \nCan be used multiple times");
            jsap.registerParameter(classpathOption);

            FlaggedOption javaVersionOption = new FlaggedOption("javaVersion");
            javaVersionOption.setShortFlag('l');
            javaVersionOption.setHelp("Set the java language version of the .java files, can be " +
                    "1.1, 1.2, 1.3, 1.4, 1.5 or 1.6, defaults to 1.6");
            jsap.registerParameter(javaVersionOption);

            FlaggedOption encodingOption = new FlaggedOption("encoding");
            encodingOption.setShortFlag('e');
            encodingOption.setHelp("Set the encoding of the .java files, defaults to the system " +
                    "default encoding");
            jsap.registerParameter(encodingOption);
            
            Switch verbose = new Switch("verbose");
            verbose.setShortFlag('v');
            verbose.setHelp("Give feedback about which file is parsed at the moment.");
            jsap.registerParameter(verbose);
        } catch (JSAPException e) {
            assert false : "Unexpected error while preparing JSAP";
        }
        return jsap;
    }

    /*
     * Adds .java files to the list of files to be processed.
     */
    private static void getInputFiles(File file, boolean searchSubdirectories, List<String> inputFileNames) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                if (childFile.isFile() || searchSubdirectories) {
                    getInputFiles(childFile, searchSubdirectories, inputFileNames);
                }
            }
        } else if (file.isFile() && file.getName().endsWith(".java")) {
            inputFileNames.add(file.getAbsolutePath());
        }
    }
}
