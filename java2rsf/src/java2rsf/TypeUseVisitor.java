package java2rsf;

import java.util.Collection;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A helper class for {@link DeclarationVisitor}. Creates the Rsf output for 
 * type use.
 * 
 */
class TypeUseVisitor extends ASTVisitor {

    private String blockName;
    private Collection<String> lines;
    private boolean isClassVisit;

    /*
     * Creates a new visitor with a name to print out and a collection to add to.
     * 
     * isClassVisit indicates whether a type declaration is visited. If true, 
     * the first type declaration is not skipped. If false, all type 
     * declarations are skipped.
     */
    TypeUseVisitor(String blockName, Collection<String> lines, boolean isTypeVisit) {
        if (blockName == null) {
            throw new NullPointerException("Parameter blockName may not be null");
        }
        if (lines == null) {
            throw new NullPointerException("Parameter lines may not be null");
        }

        this.blockName = blockName;
        this.lines = lines;
        this.isClassVisit = isTypeVisit;
    }

    @Override
    public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(arrayTypeReference)){
            return false;
        }
        
        visitTypeReference(arrayTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(arrayTypeReference)){
            return false;
        }
        
        visitTypeReference(arrayTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(arrayQualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(arrayQualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(arrayQualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(arrayQualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(singleTypeReference)){
            return false;
        }
        
        visitTypeReference(singleTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(SingleTypeReference singleTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(singleTypeReference)){
            return false;
        }
        
        visitTypeReference(singleTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(qualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(qualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(qualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(parameterizedSingleTypeReference)){
            return false;
        }
        
        visitTypeReference(parameterizedSingleTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(parameterizedSingleTypeReference)){
            return false;
        }
        
        visitTypeReference(parameterizedSingleTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, BlockScope scope) {
        if(NameUtil.isUnreachable(parameterizedQualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(parameterizedQualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, ClassScope scope) {
        if(NameUtil.isUnreachable(parameterizedQualifiedTypeReference)){
            return false;
        }
        
        visitTypeReference(parameterizedQualifiedTypeReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ThisReference thisReference, BlockScope scope) {
        if(NameUtil.isUnreachable(thisReference)){
            return false;
        }
        
        visitTypeReference(thisReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ThisReference thisReference, ClassScope scope) {
        if(NameUtil.isUnreachable(thisReference)){
            return false;
        }
        
        visitTypeReference(thisReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedThisReference)){
            return false;
        }
        
        visitTypeReference(qualifiedThisReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedThisReference qualifiedThisReference, ClassScope scope) {
        if(NameUtil.isUnreachable(qualifiedThisReference)){
            return false;
        }
        
        visitTypeReference(qualifiedThisReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(SuperReference superReference, BlockScope scope) {
        if(NameUtil.isUnreachable(superReference)){
            return false;
        }
        
        visitTypeReference(superReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedSuperReference)){
            return false;
        }
        
        visitTypeReference(qualifiedSuperReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedSuperReference qualifiedSuperReference, ClassScope scope) {
        if(NameUtil.isUnreachable(qualifiedSuperReference)){
            return false;
        }
        
        visitTypeReference(qualifiedSuperReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {
        if(NameUtil.isUnreachable(singleNameReference)){
            return false;
        }
        
        if (singleNameReference.codegenBinding instanceof FieldBinding) {
            FieldBinding fieldBinding = (FieldBinding) singleNameReference.codegenBinding;
            visitTypeReference(fieldBinding.declaringClass);
        }
        visitTypeReference(singleNameReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(SingleNameReference singleNameReference, ClassScope scope) {
        if(NameUtil.isUnreachable(singleNameReference)){
            return false;
        }
        
        if (singleNameReference.codegenBinding instanceof FieldBinding) {
            FieldBinding fieldBinding = (FieldBinding) singleNameReference.codegenBinding;
            visitTypeReference(fieldBinding.declaringClass);
        }
        visitTypeReference(singleNameReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedNameReference)){
            return false;
        }
        
        if (qualifiedNameReference.otherCodegenBindings != null) {
            for (FieldBinding fieldBinding : qualifiedNameReference.otherCodegenBindings) {
                if(!"length".equals(new String(fieldBinding.name)) && (fieldBinding.declaringClass != null)){
                    visitTypeReference(fieldBinding.declaringClass);
                }
            }
        }
        visitTypeReference(qualifiedNameReference.actualReceiverType);
        visitTypeReference(qualifiedNameReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(QualifiedNameReference qualifiedNameReference, ClassScope scope) {
        if(NameUtil.isUnreachable(qualifiedNameReference)){
            return false;
        }
        
        if (qualifiedNameReference.otherCodegenBindings != null) {
            for (FieldBinding fieldBinding : qualifiedNameReference.otherCodegenBindings) {
                visitTypeReference(fieldBinding.declaringClass);
            }
        }
        visitTypeReference(qualifiedNameReference.actualReceiverType);
        visitTypeReference(qualifiedNameReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(ArrayReference arrayReference, BlockScope scope) {
        if(NameUtil.isUnreachable(arrayReference)){
            return false;
        }
        
        visitTypeReference(arrayReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(MessageSend messageSend, BlockScope scope) {
        if(NameUtil.isUnreachable(messageSend)){
            return false;
        }
        
        visitTypeReference(messageSend.resolvedType);
        return true;
    }

    @Override
    public boolean visit(FieldReference fieldReference, BlockScope scope) {
        if(NameUtil.isUnreachable(fieldReference)){
            return false;
        }
        
        visitTypeReference(fieldReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(FieldReference fieldReference, ClassScope scope) {
        if(NameUtil.isUnreachable(fieldReference)){
            return false;
        }
        
        visitTypeReference(fieldReference.resolvedType);
        return true;
    }

    @Override
    public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
        if(NameUtil.isUnreachable(stringLiteral)){
            return false;
        }
        
        visitTypeReference(stringLiteral.resolvedType);
        return true;
    }

    @Override
    public boolean visit(AllocationExpression allocationExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(allocationExpression)){
            return false;
        }
        
        if (allocationExpression.binding.declaringClass.isAnonymousType()) {
            visitTypeReference(allocationExpression.binding.declaringClass);
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
        if(NameUtil.isUnreachable(qualifiedAllocationExpression)){
            return false;
        }
        
        if (qualifiedAllocationExpression.binding.declaringClass.isAnonymousType()) {
            visitTypeReference(qualifiedAllocationExpression.binding.declaringClass);
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(ClassLiteralAccess classLiteral, BlockScope scope) {
        if(NameUtil.isUnreachable(classLiteral)){
            return false;
        }
        
        visitTypeReference(classLiteral.resolvedType);
        return true;
    }

    /*
     * Handle type use.
     */
    private void visitTypeReference(TypeBinding typeBinding) {
        if (typeBinding.isArrayType()) {
            visitTypeReference(typeBinding.leafComponentType());
        } else if (!typeBinding.isBaseType()) {
            lines.add("USES\t" + blockName + "\t" + NameUtil.getTypeName(typeBinding));
        }
    }

    @Override
    public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
        if(NameUtil.isUnreachable(localTypeDeclaration)){
            return false;
        }
        
        if (isClassVisit) {
            isClassVisit = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
        if(NameUtil.isUnreachable(memberTypeDeclaration)){
            return false;
        }
        
        if (isClassVisit) {
            isClassVisit = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
        if(NameUtil.isUnreachable(typeDeclaration)){
            return false;
        }
        
        if (isClassVisit) {
            isClassVisit = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(NormalAnnotation annotation, BlockScope scope) {
        if(NameUtil.isUnreachable(annotation)){
            return false;
        }
        
        visitTypeReference(annotation.resolvedType);
        return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
        if(NameUtil.isUnreachable(annotation)){
            return false;
        }
        
        visitTypeReference(annotation.resolvedType);
        return false;
    }

    public boolean visit(Statement statement, BlockScope scope) {
        return false;
    }
}
