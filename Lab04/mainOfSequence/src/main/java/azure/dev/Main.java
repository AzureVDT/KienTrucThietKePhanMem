package azure.dev;

/**
 * @author VoDinhThong
 * @description Main class to run JDepend and generate report
 * @update 4/8/2024
 * @since 4/8/2024
 */

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;

import java.io.*;
import java.util.Collection;


public class Main {
    public static void main(String[] args) throws IOException {
        JDepend jDepend = new JDepend();
        jDepend.addDirectory("D:\\KienTrucThietKePhanMem\\Lab04\\mainOfSequence");
        Collection<JavaPackage> analyzedPackages = jDepend.analyze();

        try (PrintWriter printWriter = new PrintWriter(new FileOutputStream("D:\\KienTrucThietKePhanMem\\Lab04\\report\\report.xml"))) {
            printWriter.println("<JDepend>");
            printWriter.println("<Packages>");

            for (JavaPackage javaPackage : analyzedPackages) {
                printWriter.println("<Package>");
                printWriter.println("<@_attrs>");
                printWriter.println("<@_name>" + javaPackage.getName() + "</@_name>");
                // You can add more information about javaPackage here
                printWriter.println("</@_attrs>");
                printWriter.println("</Package>");
            }

            printWriter.println("</Packages>");
            printWriter.println("</JDepend>");
        }

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"D:\\KienTrucThietKePhanMem\\Lab04\\jdepend-ui\" && npm run jdepend-ui \"D:\\KienTrucThietKePhanMem\\Lab04\\report\\report.xml\" \"azure.dev\"");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }
}