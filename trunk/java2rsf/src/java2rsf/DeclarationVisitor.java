package java2rsf;

import java.util.Collection;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Creates the Rsf output for a list of parsed Java source files.
 * 
 */
public class DeclarationVisitor extends ASTVisitor {

    private Collection<String> lines;

    /**
     * Create a new visitor that generates Rsf output.
     * 
     * @param lines collection to fill with Rsf output.
     */
    public DeclarationVisitor(Collection<String> lines) {
        if(lines == null){
            throw new NullPointerException("Parameter lines may not be null.");
        }
        this.lines = lines;
    }

    /*
     * Handle a package and its annotation.
     */
    @Override
    public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
        if(NameUtil.isUnreachable(compilationUnitDeclaration)){
            return false;
        }
        
        String packageName = "";
        if (compilationUnitDeclaration.currentPackage != null) {
            boolean isFirstArgument = true;
            for (char[] namePart : compilationUnitDeclaration.currentPackage.getImportName()) {
                if (!isFirstArgument) {
                    packageName += ".";
                }
                packageName += new String(namePart);
                isFirstArgument = false;
            }
        } else {
            packageName = "<defaultPackage>";
        }
        lines.add("PACKAGE\t" + packageName);

        if (compilationUnitDeclaration.currentPackage != null) {
            handleModifiers(packageName, 0, compilationUnitDeclaration.currentPackage.annotations);
        }

        if (compilationUnitDeclaration.types != null) {
            for (TypeDeclaration typeDeclaration : compilationUnitDeclaration.types) {
                if (!typeDeclaration.name.equals(TypeDeclaration.PACKAGE_INFO_NAME)) {
                    String typeName = NameUtil.getTypeName(typeDeclaration.binding);
                    lines.add("CONTAINS\t" + packageName + "\t" + typeName);
                }
            }
        }

        return true;
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
        if(NameUtil.isUnreachable(typeDeclaration)){
            return false;
        }
        
        visitTypeDeclaration(typeDeclaration);
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
        if(NameUtil.isUnreachable(memberTypeDeclaration)){
            return false;
        }
        
        visitTypeDeclaration(memberTypeDeclaration);
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
        if(NameUtil.isUnreachable(localTypeDeclaration)){
            return false;
        }
        
        visitTypeDeclaration(localTypeDeclaration);
        return true;
    }

    /*
     * Handle a type declaration.
     */
    private void visitTypeDeclaration(TypeDeclaration typeDeclaration) {
        if (typeDeclaration.name.equals(TypeDeclaration.PACKAGE_INFO_NAME)) {
            return;
        }

        String typeName = NameUtil.getTypeName(typeDeclaration.binding);
        switch (TypeDeclaration.kind(typeDeclaration.modifiers)) {
            case TypeDeclaration.CLASS_DECL:
                if (typeDeclaration.binding.isAnonymousType()) {
                    lines.add("ANONYMOUS_CLASS\t" + typeName);
                } else if (typeDeclaration.binding.isLocalType()) {
                    lines.add("LOCAL_CLASS\t" + typeName);
                } else if (typeDeclaration.binding.isMemberType()) {
                    lines.add("MEMBER_CLASS\t" + typeName);
                } else {
                    lines.add("CLASS\t" + typeName);
                }
                break;
            case TypeDeclaration.INTERFACE_DECL:
                lines.add("INTERFACE\t" + typeName);
                break;
            case TypeDeclaration.ENUM_DECL:
                lines.add("ENUM\t" + typeName);
                break;
            case TypeDeclaration.ANNOTATION_TYPE_DECL:
                lines.add("ANNOTATION_TYPE\t" + typeName);
                break;
        }

        if ((typeDeclaration.superclass != null) || (typeDeclaration.binding.isAnonymousType())) {
            lines.add("EXTENDS\t" + typeName + "\t" + NameUtil.getTypeName(typeDeclaration.binding.superclass));
        }

        if (typeDeclaration.superInterfaces != null) {
            for (TypeReference reference : typeDeclaration.superInterfaces) {
                lines.add("IMPLEMENTS\t" + typeName + "\t" + new String(reference.resolvedType.readableName()));
            }
        }

        if (typeDeclaration.binding.isMemberType()) {
            lines.add("CONTAINS\t" + NameUtil.getTypeName(typeDeclaration.binding.enclosingType()) + "\t" + typeName);
        }


        //initializers
        if (typeDeclaration.fields != null) {
            for (FieldDeclaration field : typeDeclaration.fields) {
                if (field.getKind() == FieldDeclaration.INITIALIZER) {
                    handleInitializer(typeName, field, field.isStatic());
                }
            }
        }

        handleModifiers(typeName, typeDeclaration.modifiers, typeDeclaration.annotations);
        typeDeclaration.traverse(new TypeUseVisitor(typeName, lines, true), (ClassScope) null);
    }

    /*
     * Handle a field declaration.
     */
    @Override
    public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
        if(NameUtil.isUnreachable(fieldDeclaration)){
            return false;
        }
        
        String typeName = NameUtil.getTypeName(fieldDeclaration.binding.declaringClass);
        String fieldName = typeName + "#" + new String(fieldDeclaration.name);
        if (fieldDeclaration.getKind() == FieldDeclaration.ENUM_CONSTANT) {
            lines.add("ENUM_CONSTANT\t" + fieldName);
        } else {
            lines.add("FIELD\t" + fieldName);
            lines.add("IS_OF_TYPE\t" + fieldName + "\t" + NameUtil.getTypeName(fieldDeclaration.type.resolvedType));
        }
        lines.add("HAS\t" + typeName + "\t" + fieldName);

        handleModifiers(fieldName, fieldDeclaration.modifiers, fieldDeclaration.annotations);

        if (fieldDeclaration.initialization != null) {
            handleInitializer(typeName, fieldDeclaration, fieldDeclaration.isStatic());
        }

        return true;
    }

    /*
     * Handle instance initializer, static initializer or field initialization.
     */
    private void handleInitializer(String typeName, FieldDeclaration fieldDeclaration, boolean isStatic) {
        String blockName = typeName + "#";
        if (isStatic) {
            blockName += "<staticInit>";
            lines.add("STATIC_INITIALIZER\t" + blockName);
        } else {
            blockName += "<instanceInit>";
            lines.add("INSTANCE_INITIALIZER\t" + blockName);
        }
        lines.add("HAS\t" + typeName + "\t" + blockName);
        fieldDeclaration.traverse(new AccessVisitor(blockName, lines), null);
        fieldDeclaration.traverse(new TypeUseVisitor(blockName, lines, false), null);
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
        if(NameUtil.isUnreachable(methodDeclaration)){
            return false;
        }
        
        visitMethodDeclaration(methodDeclaration);
        return true;
    }

    @Override
    public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
        if(NameUtil.isUnreachable(constructorDeclaration)){
            return false;
        }
        
        visitMethodDeclaration(constructorDeclaration);
        return true;
    }

    @Override
    public boolean visit(AnnotationMethodDeclaration annotationTypeDeclaration, ClassScope classScope) {
        if(NameUtil.isUnreachable(annotationTypeDeclaration)){
            return false;
        }
        
        visitMethodDeclaration(annotationTypeDeclaration);
        return true;
    }

    /*
     * Handle a method call.
     */
    private void visitMethodDeclaration(AbstractMethodDeclaration methodDeclaration) {
        String methodName = NameUtil.getMethodName(methodDeclaration.binding);
        if (methodDeclaration.isConstructor()) {
            lines.add("CONSTRUCTOR\t" + methodName);
        } else if (methodDeclaration.isAnnotationMethod()) {
            lines.add("ANNOTATION_METHOD\t" + methodName);
        } else {
            lines.add("METHOD\t" + methodName);
        }

        String typeName = NameUtil.getTypeName(methodDeclaration.binding.declaringClass);
        lines.add("HAS\t" + typeName + "\t" + methodName);

        if (!methodDeclaration.isConstructor()) {
            lines.add("IS_OF_TYPE\t" + methodName + "\t" + NameUtil.getTypeName(methodDeclaration.binding.returnType));
        }

        if (methodDeclaration.thrownExceptions != null) {
            for (TypeReference exceptionReference : methodDeclaration.thrownExceptions) {
                lines.add("THROWS\t" + methodName + "\t" + NameUtil.getTypeName(exceptionReference.resolvedType));
            }
        }

        handleModifiers(methodName, methodDeclaration.modifiers, methodDeclaration.annotations);
        methodDeclaration.traverse(new AccessVisitor(methodName, lines), (ClassScope) null);
        methodDeclaration.traverse(new TypeUseVisitor(methodName, lines, false), (ClassScope) null);
    }

    /*
     * Handle modifiers and annotations.
     */
    private void handleModifiers(String modifiedName, int modifiers, Annotation[] annotations) {
        if ((modifiers & ClassFileConstants.AccPublic) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "public");
        }
        if ((modifiers & ClassFileConstants.AccProtected) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "protected");
        }
        if ((modifiers & ClassFileConstants.AccPrivate) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "private");
        }
        if ((modifiers & ClassFileConstants.AccAbstract) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "abstract");
        }
        if ((modifiers & ClassFileConstants.AccStatic) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "static");
        }
        if ((modifiers & ClassFileConstants.AccFinal) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "final");
        }
        if ((modifiers & ClassFileConstants.AccSynchronized) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "synchronized");
        }
        if ((modifiers & ClassFileConstants.AccNative) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "native");
        }
        if ((modifiers & ClassFileConstants.AccStrictfp) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "strictfp");
        }
        if ((modifiers & ClassFileConstants.AccTransient) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "transient");
        }
        if ((modifiers & ClassFileConstants.AccVolatile) != 0) {
            lines.add("IS\t" + modifiedName + "\t" + "volatile");
        }

        if (annotations == null) {
            return;
        }
        for (Annotation annotation : annotations) {
            String annotationName = NameUtil.getTypeName(annotation.resolvedType);
            lines.add("HAS_ANNOTATION\t" + modifiedName + "\t" + annotationName);
        }
    }
}
