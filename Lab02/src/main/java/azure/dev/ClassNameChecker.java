package azure.dev;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VoDinhThong
 * @description doing task
 * @update 3/5/2024
 * @since 3/5/2024
 */
public class ClassNameChecker {
    private static TokenizerME tokenizer;
    private static POSTaggerME posTagger;

    static {
        try {
            InputStream tokenModelIn = ClassNameChecker.class.getResourceAsStream("/models/en-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            tokenizer = new TokenizerME(tokenModel);

            InputStream posModelIn = ClassNameChecker.class.getResourceAsStream("/models/en-pos-maxent.bin");
            POSModel posModel = new POSModel(posModelIn);
            posTagger = new POSTaggerME(posModel);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        DirExplorer explorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java"),
                (level, path, file) -> checkClassName(file.toPath())
        );

        explorer.explore(new File("src/main/java/com/companyname"));
    }

    private static void checkClassName(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            ParseResult<CompilationUnit> result = new JavaParser().parse(content);

            if (result.isSuccessful()) {
                result.getResult().ifPresent(cu -> {
                    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> {
                        String className = c.getNameAsString();
                        if (Character.isLowerCase(className.charAt(0)) || !isNounPhrase(className)) {
                            System.out.printf("Incorrect class name '%s' in file %s at line %s%n", className, javaFile,  c.getBegin().get().line);
                        }

                        // Check for class comment
                        if (c.getComment().isEmpty()) {
                            System.out.printf("Class '%s' in file %s does not have a comment%n", className, javaFile);
                        } else {
                            String comment = c.getComment().get().getContent();
                            if (!comment.contains("@Create-Date") || !comment.contains("@author")) {
                                System.out.printf("Class comment for '%s' in file %s at line %s does not contain @Date or @author%n", className, javaFile,  c.getBegin().get().line);
                            }
                        }

                        // Check fields
                        c.getFields().forEach(f -> {
                            String fieldName = f.getVariable(0).getNameAsString();
                            if (Character.isUpperCase(fieldName.charAt(0)) || !isNounPhrase(fieldName)) {
                                System.out.printf("Incorrect field name '%s' in class '%s' in file %s at line %s%n", fieldName, className, javaFile,  c.getBegin().get().line);
                            }
                        });

                        // Check constants in interfaces
                        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(i -> {
                            i.getFields().forEach(f -> {
                                String constantName = f.getVariable(0).getNameAsString();
                                if (!constantName.equals(constantName.toUpperCase())) {
                                    System.out.printf("Constant '%s' in interface '%s' in file %s at line %s is not uppercase%n", constantName, i.getNameAsString(), javaFile,  c.getBegin().get().line);
                                }
                            });
                        });

                        // Check methods
                        c.getMethods().forEach(m -> {
                            String methodName = m.getNameAsString();
                            if (Character.isUpperCase(methodName.charAt(0)) || !startsWithVerb(methodName)) {
                                System.out.printf("Incorrect method name '%s' in class '%s' in file %s at line %s%n", methodName, className, javaFile,  c.getBegin().get().line);
                            }

                            // Check method comments
                            if (!methodName.equals(className) && !methodName.startsWith("get") && !methodName.startsWith("set") && !methodName.equals("hashCode") && !methodName.equals("equals") && !methodName.equals("toString")) {
                                if (m.getComment().isEmpty()) {
                                    System.out.printf("Method '%s' in class '%s' in file %s at line %s does not have a comment%n", methodName, className, javaFile,  c.getBegin().get().line);
                                }
                            }
                        });
                    });
                });
            } else {
                System.out.printf("Failed to parse file %s%n", javaFile);
            }
        } catch (IOException e) {
            System.out.printf("Failed to read file %s%n", javaFile);
        }
    }

    private static boolean startsWithVerb(String methodName) {
        String[] tokens = tokenizer.tokenize(methodName);
        String[] tags = posTagger.tag(tokens);

        return tags[0].startsWith("VB"); // VB is the POS tag for verb
    }

    private static boolean isNounPhrase(String className) {
        String[] tokens = tokenizer.tokenize(className);
        String[] tags = posTagger.tag(tokens);

        for (String tag : tags) {
            if (!tag.startsWith("NN")) { // NN is the POS tag for noun
                return false;
            }
        }

        return true;
    }
}
