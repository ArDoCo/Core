/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.dom.*;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.*;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.finder.EnumDeclarationFinder;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.finder.MethodDeclarationFinder;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.finder.TypeDeclarationFinder;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.finder.TypeFinder;

public final class JavaModel {

    private final CodeItemRepository codeItemRepository;
    private Set<JavaType> javaTypes;
    private Set<JavaClassifier> javaClassifiers;
    private Set<JavaInterface> javaInterfaces;
    private CodeModel codeModel;

    public JavaModel(CodeItemRepository codeItemRepository, Map<String, CompilationUnit> compUnitMap) {
        this.codeItemRepository = codeItemRepository;
        javaTypes = new HashSet<>();
        javaClassifiers = new HashSet<>();
        javaInterfaces = new HashSet<>();
        initModel(compUnitMap);
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    private void addType(Datatype codeType, AbstractTypeDeclaration abstractTypeDeclaration) {
        ITypeBinding binding = abstractTypeDeclaration.resolveBinding();
        if (null == binding) {
            return;
        }
        javaTypes.add(new JavaType(codeType, binding, getReferencedBindings(abstractTypeDeclaration)));
    }

    private void addClassifier(ClassUnit codeClassifier, AbstractTypeDeclaration abstractTypeDeclaration) {
        ITypeBinding binding = abstractTypeDeclaration.resolveBinding();
        if (null == binding) {
            return;
        }
        javaClassifiers.add(new JavaClassifier(codeClassifier, binding));
        addType(codeClassifier, abstractTypeDeclaration);
    }

    private void addInterface(InterfaceUnit codeInterface, TypeDeclaration typeDeclaration) {
        ITypeBinding binding = typeDeclaration.resolveBinding();
        if (null == binding) {
            return;
        }
        javaInterfaces.add(new JavaInterface(codeInterface, binding));
        addType(codeInterface, typeDeclaration);
    }

    private record JavaType(Datatype codeType, ITypeBinding binding, List<ITypeBinding> referencedBindings) {
    }

    private record JavaClassifier(ClassUnit codeClassifier, ITypeBinding binding) {
    }

    private record JavaInterface(InterfaceUnit codeInterface, ITypeBinding binding) {
    }

    //

    private void initImplementedInterfaces() {
        for (JavaClassifier javaClassifier : javaClassifiers) {
            ITypeBinding binding = javaClassifier.binding();
            ITypeBinding[] implInterfacesBindings = binding.getInterfaces();
            List<JavaInterface> javaImplInterfaces = Arrays.stream(implInterfacesBindings)
                    .map(implInterfaceBinding -> javaInterfaces.stream()
                            .filter(javaInterface -> javaInterface.binding().getErasure().isEqualTo(implInterfaceBinding.getErasure()))
                            .findFirst()
                            .orElseThrow())
                    .toList();
            Set<Datatype> codeImplInterfaces = new HashSet<>();
            javaImplInterfaces.forEach(javaImplInterface -> codeImplInterfaces.add(javaImplInterface.codeInterface()));
            javaClassifier.codeClassifier().setImplementedTypes(codeImplInterfaces);
        }
    }

    private void initExtendedInterfaces() {
        for (JavaInterface javaInterface : javaInterfaces) {
            ITypeBinding binding = javaInterface.binding();
            ITypeBinding[] extendedInterfacesBindings = binding.getInterfaces();
            List<JavaInterface> javaExtendedInterfaces = Arrays.stream(extendedInterfacesBindings)
                    .map(extendedInterfaceBinding -> javaInterfaces.stream()
                            .filter(otherJavaInterface -> otherJavaInterface.binding().getErasure().isEqualTo(extendedInterfaceBinding.getErasure()))
                            .findFirst()
                            .orElseThrow())
                    .toList();
            Set<Datatype> codeExtendedInterfaces = new HashSet<>();
            javaExtendedInterfaces.forEach(javaExtendedInterface -> codeExtendedInterfaces.add(javaExtendedInterface.codeInterface()));
            javaInterface.codeInterface().setExtendedTypes(codeExtendedInterfaces);
        }
    }

    private void initSuperclasses() {
        for (JavaClassifier javaClassifier : javaClassifiers) {
            ITypeBinding binding = javaClassifier.binding();
            ITypeBinding superclassBinding = binding.getSuperclass();
            if (null == superclassBinding) {
                continue;
            }
            JavaClassifier javaSuperclass = javaClassifiers.stream()
                    .filter(otherJavaClass -> otherJavaClass.binding().getErasure().isEqualTo(superclassBinding.getErasure()))
                    .findFirst()
                    .orElseThrow();
            Set<Datatype> superclasses = new HashSet<>();
            superclasses.add(javaSuperclass.codeClassifier());
            javaClassifier.codeClassifier().setExtendedTypes(superclasses);
        }
    }

    private static List<ITypeBinding> getReferencedBindings(AbstractTypeDeclaration abstractTypeDeclaration) {
        @SuppressWarnings("unchecked") List<BodyDeclaration> bodyDeclarations = abstractTypeDeclaration.bodyDeclarations();
        List<Type> referencedTypes = new ArrayList<>();
        bodyDeclarations.forEach(bodyDeclaration -> referencedTypes.addAll(TypeFinder.find(bodyDeclaration)));
        List<ITypeBinding> referencedBindings = new ArrayList<>();
        for (Type referencedType : referencedTypes) {
            ITypeBinding referencedBinding = referencedType.resolveBinding();
            if (null == referencedBinding) {
                continue;
            }
            referencedBindings.add(referencedBinding);
        }
        return referencedBindings;
    }

    //

    private void initModel(Map<String, CompilationUnit> compUnitMap) {
        Set<CodeItem> modelContent = new HashSet<>();
        Set<CodePackage> codePackages = new HashSet<>();
        Set<CodeCompilationUnit> codeCompilationUnits = new HashSet<>();

        for (var entry : compUnitMap.entrySet()) {
            CompilationUnit compilationUnit = entry.getValue();
            PackageDeclaration packageDeclaration = compilationUnit.getPackage();
            Path path = Path.of(entry.getKey());
            String fileName = path.getFileName().toString();
            String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            List<String> pathElements = new ArrayList<>();
            for (int i = 0; i < path.getNameCount() - 1; i++) {
                pathElements.add(path.getName(i).toString());
            }
            List<String> packageNames = new ArrayList<>();
            if (null != packageDeclaration) {
                Name fullName = packageDeclaration.getName();
                packageNames = getPackageNames(fullName);
            }
            CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit(codeItemRepository, fileNameWithoutExtension, new HashSet<>(), pathElements,
                    extension, ProgrammingLanguage.JAVA);
            codeCompilationUnits.add(codeCompilationUnit);
            if (null != packageDeclaration) {
                CodePackage codePackage = getPackage(packageNames, codeCompilationUnit);
                codePackages.add(codePackage);
            } else {
                modelContent.add(codeCompilationUnit);
            }
            List<Datatype> types = extractTypes(compilationUnit);
            types.forEach(t -> t.setCompilationUnit(codeCompilationUnit));
            codeCompilationUnit.setContent(types);
        }

        Set<CodePackage> mergedCodePackages = mergePackages(codePackages);
        initImplementedInterfaces();
        initExtendedInterfaces();
        initSuperclasses();

        modelContent.addAll(mergedCodePackages);

        codeModel = new CodeModel(codeItemRepository, modelContent);
    }

    private List<Datatype> extractTypes(CompilationUnit compilationUnit) {
        List<Datatype> codeTypes = new ArrayList<>();
        Set<TypeDeclaration> typeDeclarations = TypeDeclarationFinder.find(compilationUnit);
        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            codeTypes.add(processTypeDeclaration(typeDeclaration));
        }
        Set<EnumDeclaration> enumDeclarations = EnumDeclarationFinder.find(compilationUnit);
        for (EnumDeclaration enumDeclaration : enumDeclarations) {
            codeTypes.add(processEnumDeclaration(enumDeclaration));
        }
        return codeTypes;
    }

    private ClassUnit processEnumDeclaration(EnumDeclaration enumDeclaration) {
        String name = enumDeclaration.getName().getIdentifier();
        Set<ControlElement> declaredMethods = extractMethods(enumDeclaration);
        ClassUnit codeClassifier = new ClassUnit(codeItemRepository, name, declaredMethods);
        addClassifier(codeClassifier, enumDeclaration);
        return codeClassifier;
    }

    private Datatype processTypeDeclaration(TypeDeclaration typeDeclaration) {
        String name = typeDeclaration.getName().getIdentifier();
        Set<ControlElement> declaredMethods = extractMethods(typeDeclaration);
        Datatype codeType;
        if (typeDeclaration.isInterface()) {
            InterfaceUnit codeInterface = new InterfaceUnit(codeItemRepository, name, declaredMethods);
            addInterface(codeInterface, typeDeclaration);
            codeType = codeInterface;
        } else {
            ClassUnit classifier = new ClassUnit(codeItemRepository, name, declaredMethods);
            addClassifier(classifier, typeDeclaration);
            codeType = classifier;
        }
        return codeType;
    }

    private Set<ControlElement> extractMethods(ASTNode node) {
        Set<ControlElement> declaredMethods = new HashSet<>();
        Set<MethodDeclaration> methodDeclarations = MethodDeclarationFinder.find(node);
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            declaredMethods.add(extractMethod(methodDeclaration));
        }
        return declaredMethods;
    }

    private ControlElement extractMethod(MethodDeclaration methodDeclaration) {
        return new ControlElement(codeItemRepository, methodDeclaration.getName().getIdentifier());
    }

    private static List<String> getPackageNames(Name name) {
        List<String> packageNames = new ArrayList<>();
        if (name.isQualifiedName()) {
            QualifiedName qualifiedName = (QualifiedName) name;
            packageNames.addAll(getPackageNames(qualifiedName.getQualifier()));
            packageNames.addAll(getPackageNames(qualifiedName.getName()));
        } else if (name.isSimpleName()) {
            SimpleName simpleName = (SimpleName) name;
            packageNames.add(simpleName.getIdentifier());
        } else {
            throw new IllegalStateException("The name must be a qualified or a simple name");
        }
        return packageNames;
    }

    private CodePackage getPackage(List<String> packageNames, CodeCompilationUnit codeCompilationUnit) {
        if (packageNames.isEmpty()) {
            return null;
        }
        List<String> packageNamesCopy = new ArrayList<>(packageNames);
        String name = packageNamesCopy.remove(0);
        CodePackage codePackage = new CodePackage(codeItemRepository, name);
        CodePackage childCodePackage = getPackage(packageNamesCopy, codeCompilationUnit);
        if (null == childCodePackage) {
            codePackage.addContent(codeCompilationUnit);
        } else {
            codePackage.addContent(childCodePackage);
        }
        return codePackage;
    }

    private static Set<CodePackage> mergePackages(Set<CodePackage> packages) {
        Map<String, CodePackage> packageMap = new HashMap<>();
        List<CodePackage> packageList = new ArrayList<>(packages);
        for (CodePackage codePackage : packageList) {
            if (packageMap.containsKey(codePackage.getName())) {
                CodePackage existingCodePackage = packageMap.get(codePackage.getName());
                existingCodePackage.addContent(codePackage.getContent());
            } else {
                packageMap.put(codePackage.getName(), codePackage);
            }
        }
        for (CodePackage codePackage : packageMap.values()) {
            List<CodeModule> mergedPackageElements = new ArrayList<>();
            mergedPackageElements.addAll(codePackage.getCompilationUnits());
            mergedPackageElements.addAll(mergePackages(codePackage.getSubpackages()));
            mergedPackageElements.forEach(packageElement -> packageElement.setParent(codePackage));
            codePackage.setContent(mergedPackageElements);
        }
        return new HashSet<>(packageMap.values());
    }
}
