package java2rsf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * A helper class for {@link DeclarationVisitor}. Creates the Rsf output 
 * for field accesses and method calls.
 * 
 */
class AccessVisitor extends ASTVisitor {

    private String blockName;
    private Collection<String> lines;
    private Set<Statement> writesExpressions = new HashSet<Statement>();

    /*
     * Create a new visitor with a name to print out and a collection to add to.
     */
    AccessVisitor(String blockName, Collection<String> lines) {
        if (blockName == null) {
            throw new NullPointerException("Parameter blockName may not be null");
        }
        if (lines == null) {
            throw new NullPointerException("Parameter lines may not be null");
        }

        this.blockName = blockName;
        this.lines = lines;
    }

    /*
     * Handle a method call.
     */
    @Override
    public boolean visit(MessageSend messageSend, BlockScope scope) {
        if(NameUtil.isUnreachable(messageSend)){
            return false;
        }
        
        //need actualReceiverType here, in case a method is declared in java.lang.Object
        String methodName = NameUtil.getMethodName(messageSend.codegenBinding, messageSend.actualReceiverType);
        lines.add("CALLS\t" + blockName + "\t" + methodName);
        return true;
    }

    @Override
    public boolean visit(AllocationExpression allocationExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(allocationExpression)){
            return false;
        }
        
        visitConstructorCall(allocationExpression.binding);
        return true;
    }

    @Override
    public boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedAllocationExpression)){
            return false;
        }
        
        visitConstructorCall(qualifiedAllocationExpression.binding);
        return true;
    }

    @Override
    public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
        if(NameUtil.isUnreachable(explicitConstructor)){
            return false;
        }
        
        visitConstructorCall(explicitConstructor.binding);
        return true;
    }

    /*
     * Handle a constructor call.
     */
    private void visitConstructorCall(MethodBinding methodBinding) {
        lines.add("CALLS\t" + blockName + "\t" + NameUtil.getMethodName(methodBinding));
    }

    @Override
    public boolean visit(Assignment assignment, BlockScope scope) {
        if(NameUtil.isUnreachable(assignment)){
            return false;
        }
        
        visitAssignment(assignment.lhs);
        return true;
    }

    @Override
    public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
        if(NameUtil.isUnreachable(compoundAssignment)){
            return false;
        }
        
        visitAssignment(compoundAssignment.lhs);
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
        if(NameUtil.isUnreachable(fieldDeclaration)){
            return false;
        }
        
        if(fieldDeclaration.initialization != null){
            visitAssignment(fieldDeclaration);
        }
        return true;
    }

    /*
     * Handle writing to a field.
     */
    private void visitAssignment(Statement assignmentLhs) {
        String fieldName = NameUtil.findFieldName(assignmentLhs);
        if (fieldName != null) {
            writesExpressions.add(assignmentLhs);
            lines.add("WRITES\t" + blockName + "\t" + fieldName);
        }
    }

    @Override
    public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(postfixExpression)){
            return false;
        }
        
        //first read then write so that the write doesn't block the read
        visitFieldAccess(postfixExpression.lhs);
        visitAssignment(postfixExpression.lhs);
        return true;
    }

    @Override
    public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(prefixExpression)){
            return false;
        }
        
        visitFieldAccess(prefixExpression.lhs);
        visitAssignment(prefixExpression.lhs);
        return true;
    }

    //is this ever called?
    @Override
    public boolean visit(FieldReference fieldReference, ClassScope scope) {
        if(NameUtil.isUnreachable(fieldReference)){
            return false;
        }
        
        visitFieldAccess(fieldReference);
        return true;
    }

    @Override
    public boolean visit(FieldReference fieldReference, BlockScope scope) {
        if(NameUtil.isUnreachable(fieldReference)){
            return false;
        }
        
        visitFieldAccess(fieldReference);
        return true;
    }

    @Override
    public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {
        if(NameUtil.isUnreachable(singleNameReference)){
            return false;
        }
        
        visitFieldAccess(singleNameReference);
        return true;
    }
    //is this ever called?
    @Override
    public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedNameReference)){
            return false;
        }
        if (qualifiedNameReference.otherCodegenBindings != null) {
            for (FieldBinding fieldBinding : qualifiedNameReference.otherCodegenBindings) {
                if(!"length".equals(new String(fieldBinding.name)) && (fieldBinding.declaringClass != null)){
                    String fieldName = NameUtil.getTypeName(fieldBinding.declaringClass) + "#" + new String(fieldBinding.shortReadableName());
                    lines.add("READS\t" + blockName + "\t" + fieldName);
                }
            }
        }
        visitFieldAccess(qualifiedNameReference);
        return true;
    }

    @Override
    public boolean visit(QualifiedNameReference qualifiedNameReference, ClassScope scope) {
        if(NameUtil.isUnreachable(qualifiedNameReference)){
            return false;
        }
        
        if (qualifiedNameReference.otherCodegenBindings != null) {
            for (FieldBinding fieldBinding : qualifiedNameReference.otherCodegenBindings) {
                String fieldName = NameUtil.getTypeName(fieldBinding.declaringClass) + "#" + new String(fieldBinding.shortReadableName());
                lines.add("READS\t" + blockName + "\t" + fieldName);
            }
        }
        visitFieldAccess(qualifiedNameReference);
        return true;
    }

    /*
     * Handle reading a field.
     */
    private void visitFieldAccess(Expression expression) {
        String fieldName = NameUtil.findFieldName(expression);
        if ((fieldName != null) && (!writesExpressions.contains(expression))) {
            lines.add("READS\t" + blockName + "\t" + fieldName);
        }
    }

    @Override
    public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
        return false;
    }

    @Override
    public boolean visit(NormalAnnotation annotation, BlockScope scope) {
        return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
        return false;
    }
}
