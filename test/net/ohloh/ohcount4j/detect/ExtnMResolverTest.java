package net.ohloh.ohcount4j.detect;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import net.ohloh.ohcount4j.Language;
import net.ohloh.ohcount4j.SourceFile;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ExtnMResolverTest {

    private ExtnMResolver r;

    @BeforeTest()
    public void setup() {
        r = new ExtnMResolver();
    }

    @Test
    public void canResolvetest() {
        assertFalse(r.canResolve(Language.RUBY));
        assertTrue(r.canResolve(Language.LIMBO));
        assertTrue(r.canResolve(Language.MATLAB));
        assertTrue(r.canResolve(Language.OBJECTIVE_C));
        assertTrue(r.canResolve(Language.OCTAVE));
    }

    @Test
    public void countLimboTest() throws IOException {
        assertEquals(0, r.countLimbo(new SourceFile("main.m", "")));
        assertEquals(0, r.countLimbo(new SourceFile("main.m", "// Not a module")));
        assertEquals(0, r.countLimbo(new SourceFile("main.m", "#include is not a Limbo comment")));

        assertEquals(1, r.countLimbo(new SourceFile("main.m", "Foo: module {}")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "Foo:\tmodule {}")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "Foo:\tmodule {}")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "Foo: adt {}")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "foo: fn ()")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "foo: fn( )")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", "PATH: con \"foo\"")));
        assertEquals(1, r.countLimbo(new SourceFile("main.m", " # comment")));

        assertEquals(3, r.countLimbo(new SourceFile("main.m",
                "Foo: module {\n" +
                        "\tPATH:\tcon \"foo\"\n" +
                        "\n" +
                        "\tinit: fn ();\n" +
                        "}")));
    }

    @Test
    public void countOctaveTest() throws IOException {
        assertEquals(0, r.countOctave(new SourceFile("main.m", "")));
        assertEquals(0, r.countOctave(new SourceFile("main.m", "not_endfunction")));

        assertEquals(1, r.countOctave(new SourceFile("main.m", "end_try_catch")));
        assertEquals(1, r.countOctave(new SourceFile("main.m", "end_unwind_protect")));
        assertEquals(1, r.countOctave(new SourceFile("main.m", "endfunction")));
        assertEquals(1, r.countOctave(new SourceFile("main.m", "endwhile")));
    }

    @Test
    public void countObjectiveCTest() throws IOException {
        assertEquals(0, r.countObjectiveC(new SourceFile("Main.m", "")));
        assertEquals(0, r.countObjectiveC(new SourceFile("Main.m", "Foo: module {}")));

        assertEquals(1, r.countObjectiveC(new SourceFile("Main.m", "// a comment")));
        assertEquals(1, r.countObjectiveC(new SourceFile("Main.m", "- (void)method:(Type)arg")));
        assertEquals(1, r.countObjectiveC(new SourceFile("Main.m", "@interface Foo : NSObject <Bar> {")));
        assertEquals(1, r.countObjectiveC(new SourceFile("Main.m", "@implementation Foo")));
        assertEquals(1, r.countObjectiveC(new SourceFile("Main.m", "#import \"foo.h\"")));
    }

    @Test
    public void countMatlabTest() throws IOException {
        assertEquals(0, r.countMatlab(new SourceFile("main.m", "")));
        assertEquals(0, r.countMatlab(new SourceFile("main.m", "// an Objective-C comment")));

        assertEquals(1, r.countMatlab(new SourceFile("main.m", "% Matlab comment")));
        assertEquals(1, r.countMatlab(new SourceFile("main.m", "function foo(x,y)")));
    }

    @Test
    public void resolveObjectiveCExample() throws IOException {
        SourceFile s = new SourceFile("foo.m",
                "#include <stdio.h>\n" +
                        "#include \"Foo.h\"\n" +
                        "\n" +
                        "@implementation Foo : Object\n" +
                        "+ test: (int)n\n" +
                        "{\n" +
                        "  return [[self alloc] init: n];" +
                        "}\n" +
                        "@end");
        assertEquals(0, r.countMatlab(s));
        assertEquals(0, r.countLimbo(s));
        assertEquals(1, r.countObjectiveC(s));
        assertEquals(Language.OBJECTIVE_C, r.resolve(s));
    }

    @Test
    public void resolveOctaveExample() throws IOException {
        SourceFile s = new SourceFile("foo.m",
                "function foo\n" +
                        "  # line comment\n" +
                        "endfunction\n");
        assertEquals(Language.OCTAVE, r.resolve(s));
    }

    @Test
    public void resolveMatlabExample() throws IOException {
        SourceFile s = new SourceFile("foo.m",
                "function foo\n" +
                        "  % line comment\n" +
                        "end\n");
        assertEquals(Language.MATLAB, r.resolve(s));
    }

    @Test
    public void resolveLimboExmaple() throws IOException {
        SourceFile s = new SourceFile("foo.m",
                "Foo: module {\n" +
                        "\tinit:\tfn();\n" +
                        "};\n");
        assertEquals(Language.LIMBO, r.resolve(s));
    }
}
