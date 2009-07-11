package java2rsf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * Used to parse a Java source file. Return an object 
 * with resolved abstract syntax trees that can accept a visitor.
 * 
 * <p>All methods return <code>this</code> to allow chained method calls.</p>
 * 
 * <p>Example of usage:<br>
 * <pre>
 * Charset charset = Charset.forname("UTF-8");
 * JdtCompiler jdtCompiler = new JdtCompiler("C:\Project\src\HelloWorld.java")
 *                         .addClasspath("C:\Project\src")
 *                         .setCharset(charset);
 * CompilationUnitDeclaration unit = jdtCompiler.getResolvedUnit();
 * ...</pre>
 * </p>
 */
public class JdtCompiler {

    String inputFileName;
    List<String> classpathNames = new ArrayList<String>();
    String encoding = Charset.defaultCharset().displayName();
    private SourceLevel sourceLevel = SourceLevel.JDK1_6;

    /**
     * Creates a new instance from a file that shall be parsed.
     * 
     * <p><code>inputFileName</code> must not be <code>null</code> and must end
     * in <code>.java</code.</p>
     * 
     * @param inputFileName names of the file that shall be parsed
     */
    public JdtCompiler(String inputFileName) {
        if (inputFileName == null) {
            throw new NullPointerException("Parameter initialFileName may not be null.");
        }
        if (!inputFileName.endsWith(".java")) {
            throw new IllegalArgumentException("Not a .java file.");
        }
        this.inputFileName = inputFileName;

        //guess the path to rt.jar
        String baseClassesPath = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
        classpathNames.add(baseClassesPath);
    }

    /**
     * Add a location of classes that are referenced in the input files. 
     * 
     * <p>Can be either a directory containing Java source files or a 
     * <code>.jar</code> file.</p>
     * 
     * <p>When the class <code>HelloWorld</code> in package <code>hworld</code> 
     * is declared in file <code>C:\Project\src\hworld\HelloWorld.java</code>,
     * <code>classpathName</code> must point to <code>C:\Project\src</code></p>
     * 
     * <p>This class automatically tries to add the standard Java API 
     * (usually located in <code>java.home\lib\rt.jar</code>) to the 
     * classpath.</p>
     * 
     * @param classpathName a location 
     * @return <code>this</code>
     */
    public JdtCompiler addClasspath(String classpathName) {
        classpathNames.add(classpathName);
        return this;
    }

    /**
     * Sets the encoding of the Java source files. 
     * Default is the system default encoding as returned by {@link Charset#defaultCharset()}
     * 
     * @param charset encoding of the Java source files
     * @return <code>this</code>
     * @see Charset
     */
    public JdtCompiler setEncoding(Charset charset) {
        encoding = charset.displayName();
        return this;
    }

    /**
     * Set the Java version used in the input files.
     * <p>Default is Java 1.6</p>
     * 
     * @param sourceLevel the Java version of the input files
     * @return <code>this</code>
     */
    public JdtCompiler setSourceLevel(SourceLevel sourceLevel) {
        this.sourceLevel = sourceLevel;
        return this;
    }

    /**
     * Parses the input file. Returns an Object that is ready to accept a visitor. 
     * 
     * @return the <code>CompilationUnitDeclaration</code> created from the input file, parsed and ready to accept a visitor
     * @throws rsfparser.CompilationException in case parsing the input file fails
     * @throws java.io.IOException in case of an IO error while reading the input file
     */
    public CompilationUnitDeclaration getResolvedUnit() throws CompilationException, IOException {
        CompilationUnitDeclaration unit = null;
        try {
            unit = getCompiler().getResolvedUnit(inputFileName, encoding);
        } catch (AbortCompilation e) {
            throw new CompilationException(e);
        }
        return unit;
    }

    /* 
     * Convenience method that creates a compiler instance
     */
    private CompilerImpl getCompiler() {
        String[] inputFileArray = {inputFileName};
        INameEnvironment environment = new FileSystem(classpathNames.toArray(new String[0]),
                inputFileArray, encoding);
        CompilerOptions options = new CompilerOptions();
        options.sourceLevel = this.sourceLevel.getSourceLevelLong();
        options.complianceLevel = this.sourceLevel.getSourceLevelLong();
        //options.targetJDK = this.sourceLevel.getSourceLevelLong();
        IProblemFactory problemFactory = new DefaultProblemFactory();
        IErrorHandlingPolicy policy = new IErrorHandlingPolicy() {

            public boolean proceedOnErrors() {
                return false;
            }

            public boolean stopOnFirstError() {
                return true;
            }
        };
        ICompilerRequestor requestor = new ICompilerRequestor() {

            public void acceptResult(CompilationResult result) {
            }
        };
        return new CompilerImpl(environment, policy, options, requestor, problemFactory);
    }

    /**
     * Enum identifying versions of the Java language.
     * 
     */
    public static enum SourceLevel {

        JDK1_1(ClassFileConstants.JDK1_1, "1.1"),
        JDK1_2(ClassFileConstants.JDK1_2, "1.2"),
        JDK1_3(ClassFileConstants.JDK1_3, "1.3"),
        JDK1_4(ClassFileConstants.JDK1_4, "1.4"),
        JDK1_5(ClassFileConstants.JDK1_5, "1.5"),
        JDK1_6(ClassFileConstants.JDK1_6, "1.6");
        private long sourceLevelLong;
        private String sourceLevelString;

        private SourceLevel(long sourceLevelLong, String sourceLevelString) {
            this.sourceLevelLong = sourceLevelLong;
            this.sourceLevelString = sourceLevelString;
        }

        /**
         * Turns a String identifier into a <code>SourceLevel</code>.
         * 
         * <p>Accepts <code>1.1<code>, <code>1.2<code>, ..., <code>1.6<code></p>
         * 
         * <p>If the <code>sourceLevelString</code> does not match the accepted
         * format, {@link #JDK1_6} is returned.
         * 
         * @param sourceLevelString a String identifying a <code>SourceLevel</code>
         * @return the <code>SourceLevel</code> identified by <code>sourceLevelString</code>
         */
        public static SourceLevel fromString(String sourceLevelString) {
            if (JDK1_1.sourceLevelString.equals(sourceLevelString)) {
                return JDK1_1;
            }
            if (JDK1_2.sourceLevelString.equals(sourceLevelString)) {
                return JDK1_2;
            }
            if (JDK1_3.sourceLevelString.equals(sourceLevelString)) {
                return JDK1_3;
            }
            if (JDK1_4.sourceLevelString.equals(sourceLevelString)) {
                return JDK1_4;
            }
            if (JDK1_5.sourceLevelString.equals(sourceLevelString)) {
                return JDK1_5;
            }
            return JDK1_6;
        }

        /*
         * Return this source level for the Jdt compiler.
         */
        long getSourceLevelLong() {
            return sourceLevelLong;
        }
    }
}
