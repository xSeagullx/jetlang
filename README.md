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

# __(WIP)__
