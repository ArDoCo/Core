/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.finder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclarationFinder extends ASTVisitor {

    private Set<TypeDeclaration> typeDeclarations = new HashSet<>();

    public static Set<TypeDeclaration> find(ASTNode node) {
        TypeDeclarationFinder finder = new TypeDeclarationFinder();
        node.accept(finder);
        return finder.typeDeclarations;
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration) {
        typeDeclarations.add(typeDeclaration);
        return super.visit(typeDeclaration);
    }
}
