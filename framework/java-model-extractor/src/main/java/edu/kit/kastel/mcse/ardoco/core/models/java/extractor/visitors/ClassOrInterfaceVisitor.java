/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java.extractor.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import edu.kit.kastel.mcse.ardoco.core.models.java.JavaClassOrInterface;
import edu.kit.kastel.mcse.ardoco.core.models.java.JavaProject;

public class ClassOrInterfaceVisitor extends VoidVisitorAdapter<JavaProject> {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, JavaProject collector) {
        super.visit(n, collector);
        var visitedCoI = new JavaClassOrInterface(n);
        collector.addClassOrInterface(visitedCoI);
    }
}
