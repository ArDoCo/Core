package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code.java.finder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;

public class EnumDeclarationFinder extends ASTVisitor {

    private Set<EnumDeclaration> enumDeclarations = new HashSet<>();

    public static Set<EnumDeclaration> find(ASTNode node) {
        EnumDeclarationFinder finder = new EnumDeclarationFinder();
        node.accept(finder);
        return finder.enumDeclarations;
    }

    @Override
    public boolean visit(EnumDeclaration enumDeclaration) {
        enumDeclarations.add(enumDeclaration);
        return super.visit(enumDeclaration);
    }
}
