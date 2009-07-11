package java2rsf;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A helper class for the visitors. Used to create unique names for
 * types, methods and fields.
 * 
 */
class NameUtil {

    /*
     * Do not instantiate.
     */
    private NameUtil() {
    }

    /**
     * Returns a unique name for a type.
     * 
     * <p>For anonymous and local types the returned name may not be the 
     * same as the name of the <code>.class</code> files of these classes.</p>
     * 
     * @param typeBinding the binding to be named
     * @return a unique name for <code>typeBinding</code>
     */
    static String getTypeName(TypeBinding typeBinding) {
        //int, boolean, etc.
        if (typeBinding.isBaseType()) {
            return new String(typeBinding.readableName());
        }

        if (typeBinding.isArrayType()) {
            return getTypeName(((ArrayBinding) typeBinding).elementsType()) + "[]";
        }

        return new String(typeBinding.constantPoolName()).replace('/', '.');
    }

    /**
     * Search for a field access and returns a unique name for the accessed field.
     * 
     * <p>If no field access is found, <code>null</code> is returned.</p>
     * 
     * @param statement the expression to test for a field access
     * @return a unique name for the field or <code>null</code> if no field access is found
     */
    static String findFieldName(Statement statement) {
        if (statement instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) statement;
            String shortName = new String(fieldDeclaration.name);
            return getTypeName(fieldDeclaration.binding.declaringClass) + "#" + shortName;
        } else if (statement instanceof ArrayReference) {
            return findFieldName(((ArrayReference) statement).receiver);
        } else if (statement instanceof FieldReference) {
            FieldReference fieldReference = (FieldReference) statement;
            String shortName = new String(fieldReference.token);
            return getTypeName(fieldReference.receiverType) + "#" + shortName;
        } else if (statement instanceof NameReference) {
            NameReference nameReference = (NameReference) statement;
            if (nameReference.binding.kind() == Binding.FIELD) {
                String shortName = new String(nameReference.binding.readableName());
                return getTypeName(nameReference.actualReceiverType) + "#" + shortName;
            }
        }
        return null;
    }

    /**
     * Returns a unique name for this method.
     * 
     * @param methodBinding the method
     * @return a unique name for the method
     */
    static String getMethodName(MethodBinding methodBinding) {
        return getMethodName(methodBinding, methodBinding.declaringClass);
    }

    /**
     * Returns a unique name for this method, assuming that it is declared in 
     * the type <code>typeBinding</code>.
     * 
     * @param methodBinding the method to be named
     * @param typeBinding the declaring type of the method
     * @return a unique name for <code>methodBinding</code>
     */
    static String getMethodName(MethodBinding methodBinding, TypeBinding typeBinding) {
        String typeName = getTypeName(typeBinding);
        String methodName = typeName + "#";
        if (methodBinding.isConstructor()) {
            methodName += "<init>(";
        } else {
            methodName += new String(methodBinding.selector) + "(";
        }
        if (methodBinding.parameters != null) {
            boolean isFirstArgument = true;
            for (TypeBinding parameterBinding : methodBinding.parameters) {
                if (!isFirstArgument) {
                    methodName += ",";
                }
                isFirstArgument = false;
                methodName += getTypeName(parameterBinding);
            }
        }
        methodName += ")";

        return methodName;
    }

    /**
     * Returns <code>true</code>, if a node in the parse tree is unreachable.
     * 
     * @param astNode a node in the parse tree
     * @return whether <code>astNode</code> isun reachable
     */
    static boolean isUnreachable(ASTNode astNode) {
        //an unresolved message invocation, only happens in unreachable code
        if ((astNode instanceof MessageSend) && (((MessageSend) astNode).codegenBinding == null)) {
            return true;
        }

        return (astNode.bits & ASTNode.IsReachable) == 0;
    }
}
