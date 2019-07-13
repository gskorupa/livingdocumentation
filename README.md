# Living Documentation Doclet

LivingDocDoclet is a Javadoc Doclet inspired by concepts from the "Living Documentation" book by Cyrille Martraire.

## Introduction

## Documenting code

## Usage

### Command line

    $ javadoc -language pl -category architecture \
      -doclet org.cricketmsf.livingdocumentation.LivingDocDoclet \
      -docletpath ./lib/LivingDocumentation.jar \
      -classpath ./lib/cricket-1.3.0-alpha.12.jar \
      ./src/java/com/myapp/*.java

### Ant

LivingDocDoclet may be used via a doclet element in Ant javadoc task:

    <javadoc destdir="target/javadoc"
             sourcepath="src"
             overview="src/overview.md">
      <doclet name="org.cricketmsf.livingdocumentation.LivingDocDoclet" 
              pathref="livingdocdoclet.classpath">
        <param name="--base-dir" value="${basedir}"/>
        <param name="--attribute" value="-file=glossary.md"/>
        <param name="--attribute" value="-category=glossary"/>
        <param name="--attribute" value="-syntax=markdown"/>
      </doclet>
    </javadoc>

### Doclet options

**-file &lt;file&gt;**
  
Sets the output file location. If `<file>` is a relative path name, it is assumed to be relative to the `--base-dir` directory.

**-category &lt;doccategory&gt;**

Sets the document category. It can be `glossary` or `architecture`. The default is `glossary`.

**-syntax &lt;syntax&gt;**

Sets the syntax usedto generate the document. It can be `markdown` or `asciidoc`. The default is `markdown`.

**-context &lt;context&gt;**

Includes only classes with `BoundedContext` annotation named `<context>`. With this option all Bounded Contexts will be included.

**-type &lt;type&gt;**

Only classes annotated with selected building block type will be included. It can be one of `event`, `entity`, `feature`, `service`. 
Without this option, all types will be included.


