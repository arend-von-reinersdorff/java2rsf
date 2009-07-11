package java2rsf;

import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * An exception that is thrown when parsing fails.
 * 
 */
public class CompilationException extends Exception {

    private static final long serialVersionUID = 23L;
    private String fileName = null;
    private int lineNumber = -1;

    /*
     * Create a new compilationException from the underlying compiler exception
     */
    CompilationException(AbortCompilation cause) {
        super(
                (cause.problem != null) ? cause.problem.getMessage() 
                                        : "Failed to create abstract syntax tree",
                cause
                );
        char[] originatingFileName = cause.problem.getOriginatingFileName();
        if ((cause.problem != null) && (originatingFileName != null)){
            this.fileName = new String(originatingFileName);
            this.lineNumber = cause.problem.getSourceLineNumber();
        }
    }

    /**
     * Returns the error message given by the compiler.
     * 
     * @return the error message 
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    /**
     * Returns the name of the source file that caused the error. Or 
     * <code>null</code> if the file is not known.
     * 
     * @return name of the source file with error
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the line number in the source file that caused the error. Or 
     * <code>-1</code> if the line number is not known.
     * 
     * @return line number of the error
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns a readable description of this CompilationException.
     * 
     * @return readable description of this CompilationException.
     */
    @Override
    public String toString() {
        String output = super.toString();
        if(this.fileName != null){
            output += "\nFile: " + this.fileName + "\nLine: " + this.lineNumber;
        }
        return output;
    }

    /**
     * Returns the original exception thrown by the compiler.
     * 
     * @return the original exception
     */
    @Override
    public AbortCompilation getCause() {
        return (AbortCompilation) super.getCause();
    }
}
