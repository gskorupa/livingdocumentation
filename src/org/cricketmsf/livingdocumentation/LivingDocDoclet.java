/*
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package org.cricketmsf.livingdocumentation;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class LivingDocDoclet {

    static String DEFAULT_FILE = "glossary.txt";
    static String[] LANGUAGES = new String[]{"en", "pl"};
    static String[] CATEGORIES = new String[]{"glossary", "architecture"};
    static String[] SYNTAXES = new String[]{"markdown", "asciidoc"};
    static String[] TYPES = new String[]{"event", "service", "entity", "feature"};

    static String H1;
    static String H2;
    static String H3;
    static String HR;
    static String LIST_ITEM;
    static String ITALIC;

    static HashMap<String, String> options;
    static PrintWriter writer;

    // doclet entry point
    public static boolean start(RootDoc root) {
        String fileName = DEFAULT_FILE;
        String documentCategory;
        String language;
        String syntax;
        String componentType;
        String context;

        options = readOptions(root.options());
        documentCategory = options.getOrDefault("-category", "glossary");
        syntax = options.getOrDefault("-syntax", "markdown");
        fileName = options.getOrDefault("-file", documentCategory + "." + ("markdown".equals(syntax) ? "md" : "adoc"));
        language = options.getOrDefault("-language", "en");
        componentType = options.getOrDefault("-type", "");
        context = options.getOrDefault("-context", "");
        switch (syntax) {
            case "markdown":
                H1 = "# ";
                H2 = "## ";
                H3 = "### ";
                HR = "---";
                LIST_ITEM = "* ";
                ITALIC = "*";
                break;
            case "asciidoc":
                H1 = "= ";
                H2 = "== ";
                H3 = "== ";
                HR = "---";
                LIST_ITEM = "* ";
                ITALIC = "_";
                break;
        }
        try {
            writer = new PrintWriter(fileName);
            String title = H1 + getTranslation(documentCategory, language);
            if ("glossary".equals(documentCategory)) {
                if (!context.isEmpty()) {
                    title = title + ": " + getTranslation(context, language);
                }
                if (!componentType.isEmpty()) {
                    title = title + ": " + getTranslation(componentType, language);
                }
            }
            writer.println(title);
            process(root, documentCategory, context, componentType);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void process(RootDoc root, String fileType, String context, String componentType) {
        final ClassDoc[] classes = root.classes();
        for (ClassDoc clss : classes) {
            if (isBusinessMeaningful(clss, fileType, context, componentType)) {
                process(clss);
            }
        }
    }

    private static boolean isBusinessMeaningful(ProgramElementDoc doc, String fileType, String context, String componentType) {
        final AnnotationDesc[] annotations = doc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isBusinessMeaningful(annotation, fileType, context, componentType)) {
                if ("glossary".equals(fileType)) {
                    if (componentType.isEmpty()) {
                        return true;
                    } else {
                        return isTypeMeaningful(annotations, componentType);
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTypeMeaningful(AnnotationDesc[] annotations, String componentType) {
        for (AnnotationDesc annotation : annotations) {
            if (componentType.equalsIgnoreCase(annotation.annotationType().name())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBusinessMeaningful(AnnotationDesc annotation, String fileType, String context, String componentTypes) {
        if ("glossary".equals(fileType)) {
            if (!annotation.annotationType().qualifiedTypeName()
                    .startsWith("org.cricketmsf.livingdocumentation.design.")) {
                return false;
            }
            if (!context.isEmpty()) {
                if ("BoundedContext".equals(annotation.annotationType().name())) {
                    ElementValuePair[] pairs = annotation.elementValues();
                    for (ElementValuePair pair : pairs) {
                        if ("name".equals(pair.element().name()) && context.equals(pair.value().value())) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            }
        } else if ("architecture".equals(fileType)) {
            if (!annotation.annotationType().qualifiedTypeName()
                    .startsWith("org.cricketmsf.livingdocumentation.architecture.")) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    protected static void process(ClassDoc clss) {
        writer.println("");
        writer.println(H2 + ITALIC + clss.simpleTypeName() + ITALIC);
        writer.println(clss.commentText());
        writer.println("");
        writer.println();
        writer.println(HR);
        writer.println();
        printContext(clss);
        printDddBlocks(clss);
        printFeatures(clss);
        if (clss.isEnum()) {
            printEnumConstants(clss.enumConstants());
            printMethods(clss.methods(false));
        } else if (clss.isInterface()) {
            // todo
        } else {
            printFields(clss.fields(false));
            printMethods(clss.methods(false));
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
                    if ("name".equals(pair.element().name())) {
                        writer.println(": " + pair.value().value());
                    }
                }
            }
        }
        if (found) {
            printSeparator();
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
            printSeparator();
        }
    }

    private static void printContext(ClassDoc doc) {
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
            printSeparator();
        }
    }

    private static void printMethods(MethodDoc[] methods) {
        for (MethodDoc m : methods) {
            if (!m.isPublic() || !hasComment(m)) {
                return;
            }
            final String signature = m.name() + m.flatSignature()
                    + ": " + m.returnType().simpleTypeName();
            writer.println(LIST_ITEM + signature);
            writer.println("");
            writer.println("    " + m.commentText());
        }
    }

    private static void printFields(FieldDoc[] fields) {
        boolean found = false;
        for (FieldDoc field : fields) {
            if (field.isPublic() && hasComment(field)) {
                writer.println(LIST_ITEM + field.name() + ": "
                        + field.type().qualifiedTypeName() + " "
                        + field.commentText());
                found = true;
            }
        }
        if (found) {
            printSeparator();
        }
    }

    private static void printEnumConstants(FieldDoc[] enumConstants) {
        boolean found = false;
        for (FieldDoc field : enumConstants) {
            if (field.isPublic() && hasComment(field)) {
                // todo
                // found = true;
            }
        }
        if (found) {
            printSeparator();
        }
    }

    private static boolean hasComment(ProgramElementDoc doc) {
        return doc.commentText().trim().length() > 0;
    }

    private static HashMap<String, String> readOptions(String[][] options) {
        HashMap<String, String> tmp = new HashMap<>();
        for (String[] opt : options) {
            tmp.put(opt[0], opt[1]);
        }
        return tmp;
    }

    public static int optionLength(String option) {
        if (option.equals("-file")
                || option.equals("-context")
                || option.equals("-category")
                || option.equals("-language")
                || option.equals("-syntax")
                || option.equals("-type")) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][],
            DocErrorReporter reporter) {
        boolean validNumberOfValues = true;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            switch (opt[0]) {
                case "-file":
                    validNumberOfValues = opt.length == 2;
                    break;
                case "-context":
                    validNumberOfValues = opt.length == 2;
                    break;
                case "-category":
                    validNumberOfValues = opt.length == 2 && Arrays.asList(CATEGORIES).contains(opt[1]);
                    break;
                case "-language":
                    validNumberOfValues = opt.length == 2 && Arrays.asList(LANGUAGES).contains(opt[1]);
                    break;
                case "-syntax":
                    validNumberOfValues = opt.length == 2 && Arrays.asList(LANGUAGES).contains(opt[1]);
                    break;
                case "-type":
                    validNumberOfValues = opt.length == 2 && Arrays.asList(TYPES).contains(opt[1]);
                    break;
                default:
                    break;
            }
        }
        if (!validNumberOfValues) {
            reporter.printError("Usage: javadoc -file <file> -context <context> -category <category> -type <type> -language <language> -syntax <syntax> -doclet LivingDocDoclet ...");
            return false;
        }
        return true;
    }

    private static String getTranslation(String key, String language) {
        String[] en = new String[]{"Glossary", "Architecture", "Services", "Events", "Entities"};
        String[] pl = new String[]{"Słownik pojęć", "Architektura", "Serwisy", "Zdarzenia", "Encje"};
        String[] texts = en;
        if ("pl".equals(language)) {
            texts = pl;
        }
        switch (key.toLowerCase()) {
            case "glossary":
                return texts[0];
            case "architecture":
                return texts[1];
            case "service":
                return texts[2];
            case "event":
                return texts[3];
            case "entity":
                return texts[4];
        }
        return key;

    }

    private static void printSeparator() {
        writer.println();
        writer.println(HR);
        writer.println();
    }
}
