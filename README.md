# Build and run
```
# From project root
./gradlew install # will place application distributive in `editor/build/install/editor`.
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
6. __Esc__ - stops currently running JetLang program.

Assumptions:
1. All files are handled in `UTF-8`.
2. Only one instance of JetLang program can be running at any time. (but multiple highlight tasks can be run in parallel).

# Architecture
`core` project - jetLang compiler and runtime.
`editor` project - editor for jetLang. Knows about `core`.


## Layers:

Architecture layers in order of ascending visibility. i.e. `core` knows nothing. `ui` knows about `services` and `core`, but don't know about `editor`.

1. core (Lexer, Parser, Runtime)
2. editor service level: com.xseagullx.jetlang.services (ActionManager, HighlightingService, RunService, StyleManager, TaskManager)
3. editor ui level: com.xseagullx.jetlang.ui (EditPanel, OutPanel, MiscPanel, FileManagingComponent)
4. editor main classes (Editor)

__(WIP)__
