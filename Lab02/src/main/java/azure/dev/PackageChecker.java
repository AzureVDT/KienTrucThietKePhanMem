package azure.dev;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class PackageChecker {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^com\\.companyname\\..*");

    public static void main(String[] args) {
        DirExplorer explorer = new DirExplorer(
                (level, path, file) -> file.getName().endsWith(".java"),
                (level, path, file) -> checkPackage(file.toPath())
        );

        explorer.explore(new File("src/main/java"));
    }

    private static void checkPackage(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            ParseResult<CompilationUnit> result = new JavaParser().parse(content);

            if (result.isSuccessful()) {
                result.getResult().ifPresent(cu -> {
                    cu.getPackageDeclaration().ifPresent(pd -> {
                        String packageName = pd.getNameAsString();
                        if (!PACKAGE_PATTERN.matcher(packageName).matches()) {
                            System.out.printf("Incorrect package name '%s' in file %s%n", packageName, javaFile);
                        }
                    });
                });
            } else {
                System.out.printf("Failed to parse file %s%n", javaFile);
            }
        } catch (IOException e) {
            System.out.printf("Failed to read file %s%n", javaFile);
        }
    }
}