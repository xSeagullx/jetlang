> For a bit more elaborated, free-text explaination of work / thoughts process, and reasons behind core decisions. see [below](#motivation-and-roadmap-devstory).

# Build and run
```
# From project root
./gradlew install # will place application distributive in `editor/build/install/editor`.
#Application should be started from it's distributive folder
cd editor/build/install/editor
./bin/editor
```

## Shortcuts:
1. __(CMD/Ctrl)+O__ - open file.
2. __(CMD/Ctrl)+S__ - save current file.
3. __(CMD/Ctrl)+W__ - close current file w/o saving (open new file).
4. __(CMD/Ctrl)+Q__ - quit editor application.
5. __(CMD/Ctrl)+Enter__ - run JetLang program.
5. __(CMD/Ctrl)+T__ - toggle slow mode (adds 100 ms delay to every step of JetLang program). Active only on the next run.
6. __(CMD/Ctrl)+I__ - toggle display of threads in out panel. Active only on the next run.
7. __(CMD/Ctrl)+J__ - toggle between "stack" or "JVM bytecode" based compiler. Active only on the next run.
8. __Esc__ - stops currently running JetLang program.

## Assumptions:
1. All files are handled in `UTF-8`.
2. Only one instance of JetLang program can be running at any time. (but multiple highlight tasks can be run in parallel).
3. `map` and `reduce` are elements of a language itself (and not a functions of standard library)
4. `map` and `reduce` will be executed in parallel if number of their elements is bigger than 1000. It'll use ForkJoin pool with 4 threads in it.
5. All integer operations are performed with `java.lang.Integer`, all floating-point calculations - in `java.lang.Double`.
6. `1 / 2` will yield `0`, as it's an integer division. `1.0 / 2` or `1 / 2.0` will give `0.5`.
7. `config.json` shall be valid or non-existent.
8. Distributive must be started from it's root folder. (just because config file is there). If you run it from IDEA, specify a working directory `editor/src/dist/`.
9. JVM bytecode compiler is added as a *pure experiment*, and it's not pretending to be complete. Error reporting there is lacking (there are no line numbers, for example). Also there is no parallel execution (although will be easy to add).

# Architecture
`core` project - jetLang compiler and runtime.
`editor` project - editor for jetLang. Knows about `core`.

## Layers:

Architecture layers in order of ascending visibility. i.e. `core` knows nothing. `ui` knows about `services` and `core`, but don't know about `editor`.

1. core (Lexer, Parser, Runtime)
2. editor service level: com.xseagullx.jetlang.services (ActionManager, HighlightingService, RunService, StyleManager, TaskManager)
3. editor ui level: com.xseagullx.jetlang.ui (EditPanel, OutPanel, MiscPanel, FileManagingComponent)
4. editor main classes (Editor)


# Screenshots
![Running with error](/misc/Runnning%20with%20error.png)
![Cancelled](/misc/Cancelled.png)
![Success](/misc/Success.png)

# Motivation and roadmap. (devstory)
0. __Project setup.__ I've gradle and maven experience, but gradle is way less verbose, and have a wrapper.
0. __Java 8.__ I decided, that we don't need Java 7 support. Java 8 language features make me write code faster and more comprehensible.
1. __Parsing.__ Parsing programming language is a step-by-step process, and it consists of lexer -> parser -> AST -> instruction-selection. It can be more complex, but I'll keep it simple here.
For first two steps antlr4 was an obvious choice for me. It gives me almost instant results for first to steps, transforming text to CST.
I know it as I was rewriting [groovy grammar](https://github.com/xSeagullx/antlrv4_groovy_grammar/) to it, as my GSoC project (3 years ago).
It allows me to have parser quickly, iterate over it. Also it has an IntelliJ IDEA plugin.
2. __AST.__ I've opted out from building AST, as it feels a lot like an over-engineering for this project. I'm building both runtimes (stack-based and bytecode) from CST. I also can use CST for edit-r-specific highlighting (like matching pair braces / parenthesises). AST is cool for transformations. I'm a big fan of groovy AST transformations, and wrote couple of them before, but I feel, like introducing AST here will lead to unnecessary bloated code.
3. I went for stack based runtime first. It was easy to implement, I had no prior experience with `asm` and java bytecode generation, so I went for low-hungin fruit first. [Make it work](http://wiki.c2.com/?MakeItWorkMakeItRightMakeItFast), you know.
That runtime has an entry point, which is receiving so-called ExecutionContext. It's basically my thread model (it has frames, and local variables). It also can allow nice things, like REPL - style execution, when context of a program is preserved between calls.
4. I've got a decently working grammar and stack-based runtime in ~5 hours.
5. Then I postponed work on `core` module and started to work on editors. I wanted to evaluate existing text editor components. I went for vanilla swing, as it was enough for my goals, as I know it (although last time I was working on desktop GUI app was 3-4 years ago). There are some gotcha's of Swing you have to take care, but otherwise it's a pretty solid choice for such a simple app. IMHO. Spent some time, to prove, that I can write an editor myself, without external libraries and without tons of auxiliary code.
6. Then it was a sequence of MakeItWork - MakeItRights phases for editor, when I was putting it all together, and then refactoring, to split it up. I end up getting base services extracted: TaskManager, styles manager, etc, sometimes using each other, and my UI using services, and communicating with them via callbacks. 
7. I haven't tried to make my UI components thread safe, I forces system accessing it to care of it.
8. For parallel map / reduce, the most obvious solution: `parallelStream` was ditched out, as it is not flexible. It uses common fork-join pool, and I wanted more control over execution. I prototyped ForkJoin and java5 ExecutorService solutions for `map`, but kept the former one, as it was a bit more comprehensible.
9. Had some fun with cancellation of task and exception propagation. For every thread I'm copying an ExecutionContext, thus code in map/reduce work same way, as if my context was sequential.
10. My programs are running in separate thread (which can spawn other threads, if map/reduce operations are parallelised), but I can run only one instance of programm at given time. It's just because I didn't want to create many tabs for editor output panel. On the other hand, my highlighter can be run in parallel as many times as needed, to provide fastest possible response time to editor. Sometimes, highlighter results can be discarded: if there were changes in program text since highlighter task was started, so there should be no inconsistency in UI.
11. Last `ast`-based  bytecode runtime is technically an experiment, I've done for fun. I wanted to try it out. It was planned from the very beginning, that's why `Program` is an interface, and `StackBasedProgram` was extending it from very beginning, although I've done some refactoring anf unification, when my program starts to "compile" to 2 different runtimes.
