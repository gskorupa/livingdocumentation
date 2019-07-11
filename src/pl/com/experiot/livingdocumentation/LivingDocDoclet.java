/*
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package pl.com.experiot.livingdocumentation;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class LivingDocDoclet {

    static PrintWriter writer;
    static String DEFAULT_FILE = "glossary.txt";
    static HashMap<String, String> options;

    // doclet entry point
    public static boolean start(RootDoc root) {
        String fileName = DEFAULT_FILE;
        String fileType;
        String language;
        options = readOptions(root.options());
        fileName = options.getOrDefault("-file", DEFAULT_FILE);
        fileType = options.getOrDefault("-type", "glossary");
        language = options.getOrDefault("-language", "en");
        try {
            writer = new PrintWriter(fileName);
            writer.println("# " + getTranslation(fileType, language));
            process(root, fileType);
            writer.close();
        } catch (FileNotFoundException e) {
//...
        }
        return true;
    }

    private static void process(RootDoc root, String fileType) {
        final ClassDoc[] classes = root.classes();
        for (ClassDoc clss : classes) {
            if (isBusinessMeaningful(clss, fileType)) {
                process(clss);
            }
        }
    }

    private static boolean isBusinessMeaningful(ProgramElementDoc doc, String fileType) {
        final AnnotationDesc[] annotations = doc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isBusinessMeaningful(annotation.annotationType(), fileType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBusinessMeaningful(AnnotationTypeDoc annotationType, String fileType) {
        if ("glossary".equals(fileType)) {
            return annotationType.qualifiedTypeName()
                    .startsWith("pl.com.experiot.livingdocumentation.design.");
        } else if ("architecture".equals(fileType)) {
            return annotationType.qualifiedTypeName()
                    .startsWith("pl.com.experiot.livingdocumentation.architecture.");
        }
        return false;
    }

    protected static void process(ClassDoc clss) {
        writer.println("");
        writer.println("## *" + clss.simpleTypeName() + "*");
        writer.println(clss.commentText());
        writer.println("");
        writer.println();
        writer.println("---");
        writer.println();
        printContexts(clss);
        printDddBlocks(clss);
        printFeatures(clss);
        if (clss.isEnum()) {
            for (FieldDoc field : clss.enumConstants()) {
                printEnumConstant(field);
            }
            writer.println("");
            for (MethodDoc method : clss.methods(false)) {
                printMethod(method);
            }
        } else if (clss.isInterface()) {
            //for (ClassDoc subClass : subclasses(clss)) {
            //    printSubClass(subClass);
            //}
        } else {
            for (FieldDoc field : clss.fields(false)) {
                printField(field);
            }
            for (MethodDoc method : clss.methods(false)) {
                printMethod(method);
            }
        }
    }

    private static void printFeatures(ClassDoc doc) {
        final AnnotationDesc[] annotations = doc.annotations();
        boolean found = false;
        for (AnnotationDesc annotation : annotations) {
            if ("Feature".equals(annotation.annotationType().name())) {
                found = true;
                writer.print("" + annotation.annotationType().name());
                ElementValuePair[] pairs = annotation.elementValues();
                for (ElementValuePair pair : pairs) {
                    //writer.println(": " + pair.element().name() + "=" + pair.value().value());
                    if ("name".equals(pair.element().name())) {
                        writer.println(": " + pair.value().value());
                    }
                }
            }
        }
        if (found) {
            writer.println();
            writer.println("---");
            writer.println();
        }
    }

    private static void printDddBlocks(ClassDoc doc) {
        final AnnotationDesc[] annotations = doc.annotations();
        boolean found = false;
        for (AnnotationDesc annotation : annotations) {
            if ("Entity".equals(annotation.annotationType().name())
                    || "Event".equals(annotation.annotationType().name())
                    || "Service".equals(annotation.annotationType().name())) {
                found = true;
                writer.println("Type: " + annotation.annotationType().name());
            }
        }
        if (found) {
            writer.println();
            writer.println("---");
            writer.println();
        }
    }

    private static void printContexts(ClassDoc doc) {
        final AnnotationDesc[] annotations = doc.annotations();
        boolean found = false;
        for (AnnotationDesc annotation : annotations) {
            if ("BoundedContext".equals(annotation.annotationType().name())) {
                found = true;
                writer.print("" + annotation.annotationType().name());
                ElementValuePair[] pairs = annotation.elementValues();
                for (ElementValuePair pair : pairs) {
                    if ("name".equals(pair.element().name())) {
                        writer.println(": " + pair.value().value());
                    }
                }
            }
        }
        if (found) {
            writer.println();
            writer.println("---");
            writer.println();
        }
    }

    private static void printMethod(MethodDoc m) {
        if (!m.isPublic() || !hasComment(m)) {
            return;
        }
        final String signature = m.name() + m.flatSignature()
                + ": " + m.returnType().simpleTypeName();
        writer.println("- " + signature + " " + m.commentText());
    }

    private static boolean hasComment(ProgramElementDoc doc) {
        return doc.commentText().trim().length() > 0;
    }

    private static void printField(FieldDoc m) {
        if (!m.isPublic() || !hasComment(m)) {
            return;
        }
        // ...
    }

    private static void printEnumConstant(FieldDoc m) {
        if (!m.isPublic() || !hasComment(m)) {
            return;
        }
        // ...
    }

    private static void printSubClass(ClassDoc m) {
        if (!m.isPublic() || !hasComment(m)) {
            return;
        }
        // ...
    }

    // todo
    private static ClassDoc[] subclasses(ClassDoc cls) {
        return cls.innerClasses();
    }

    private static HashMap<String, String> readOptions(String[][] options) {
        HashMap<String, String> tmp = new HashMap<>();
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            tmp.put(opt[0], opt[1]);
        }
        return tmp;
    }

    public static int optionLength(String option) {
        if (option.equals("-file") || option.equals("-type") || option.equals("-language")) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][],
            DocErrorReporter reporter) {
        boolean typeValueNotAllowed = false;
        boolean wrongNumberOfValues = false;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            switch (opt[0]) {
                case "-file":
                    if (opt.length != 2) {
                        wrongNumberOfValues = true;
                    }
                    break;
                case "-type":
                    if (opt.length != 2) {
                        wrongNumberOfValues = true;
                    } else {
                        if (!("architecture".equals(opt[1]) || "glossary".equals(opt[1]))) {
                            typeValueNotAllowed = true;
                        }
                    }
                    break;
                case "-language":
                    if (opt.length != 2) {
                        wrongNumberOfValues = true;
                    } else {
                        if (!("pl".equals(opt[1]) || "en".equals(opt[1]))) {
                            typeValueNotAllowed = true;
                        }
                    }
                    break;
                default:
                    System.out.println(opt[0]);
                    //wrongNumberOfValues = true;
                    break;
            }
        }
        if (wrongNumberOfValues || typeValueNotAllowed) {
            reporter.printError("Usage: javadoc -file filename -type type -language lang -doclet AnnotationDoclet ...");
            /*"where:");
            reporter.printError("type is \"glossary\" or \"architecture\"");
            reporter.printError("lang is \"en\" or \"pl\"");*/
            return false;
        }
        return true;
    }

    private static String getTranslation(String key, String language) {
        if ("en".equals(language)) {
            switch (key) {
                case "glossary":
                    return "Glossary";
                case "architecture":
                    return "Architecture";
            }
        } else if ("pl".equals(language)) {
            switch (key) {
                case "glossary":
                    return "Słownik pojęć";
                case "architecture":
                    return "Architektura";
            }
        }
        return key;

    }
}
