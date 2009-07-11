package java2rsf;

import java.io.File;
import java.io.IOException;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;

/*
 * Must subclass Compiler, because beginToCompile() is protected.
 */
class CompilerImpl extends Compiler {

    CompilerImpl(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerOptions options, ICompilerRequestor requestor, IProblemFactory problemFactory) {
        super(environment, policy, options, requestor, problemFactory);
    }

    CompilationUnitDeclaration getResolvedUnit(String inputFileName, String encoding) throws IOException {
        //create compilation unit
        char[] contents = Util.getFileCharContent(new File(inputFileName), encoding);
        CompilationUnit[] sourceUnits = {new CompilationUnit(contents, inputFileName, encoding)};
        
        //build AST
        beginToCompile(sourceUnits);
        
        //Use for-loop like this. There might be nulls at the end of the array
        for (int i = 0; i < totalUnits; i++) {
            process(unitsToProcess[i], i);
        }
        
        //return only original file
        CompilationUnitDeclaration resultUnit = null;
        for (int i = 0; i < totalUnits; i++) {
            if (inputFileName.equals(new String(unitsToProcess[i].getFileName()))) {
                resultUnit = unitsToProcess[i];
                break;
            }
        }
        assert (resultUnit != null) : "Didn't find original file in parsed files.";
        
        return resultUnit;
    }
}
