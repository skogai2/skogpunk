---
title: skogai
permalink: skogai-rpg/skogai
type: router
---

# skogai game studios -- architecture

building ai frameworks disguised as game development
remembering that we are not "getting helped by agents to create the things we want" but instead this should be seen as 
"we are together creating the environment, tools and routines to in create together in the future" 
the hard part will *NOT* be how to create a variable or learn the syntax to create a for-loop.

*the only focus from start to finish are on creating a framework around giving ai agents just enough structure so they always are guided towards the next step step - and the tools and freedom needed to have as many options to get there as possible*

## example directory structure

```tree
├── design
│   ├── assets
│   │   └── fonts
│   ├── fonts
│   ├── preview
│   └── uploads
├── docs
├── inspiration
│   └── web
├── knowledge
├── memory
├── public
├── quests
├── skogrpg
│   ├── docs
│   ├── src
│   ├── test
│   └── tools
├── src
├── state
├── test
└── tests
```


## example skogai definitions

- full turn; from one party end their turn with a ending message, thus giving away their initiative, and until the other have done the same by ending their turn and returning the initiative back.

## example of skogai-routing at-links

### routes

- @HANDOVER.md - a at-link are still the same thing as it was when it first started beeing used, only with some extra perks added along the way. `cat $HANDOVER >> $MESSAGE` 
- @.skogai/ - `ls $.skogai/ >> $MESSAGE` follows the exact same concept and idea. skogai often add `/` to explicitly show paths but is not needed.

## long term goals

to let us write down concepts and ideas as we continue to expand, improve and change this project along the way - we need to do so bit by bit. writing everything in your main routing file is only a bad practice when not taken care of. but if you actively prune your memories, structure your knowledge and actively use these routing files as small choices guiding the reader towards their end destination - it suddenly are a powerful tool instead. the most important are obviously that it is something we must continue doing as long as new information is added, and if good habits are formed early we can actually create routes of the path most traveled in a natural way since when something needs to be moved from our starting place it is quite literally telling us about a new crossroads where a choice has been made and probably should be once more.

`skogai.md` at the repo root is the main place where basic key-value-configuration is defined as well as acting as the combined entry point for all ai agents. but the most important job is to guide and inform about what to expect and how to find it.

## todo

- at the end of many files a "append-only" section can often be found. this serves both as a legacy features still need it. but even more important is the reason for it existing in the first place. that `append` is by far the superior way to write to file in almost all cases. 
  - the by far most effective way to manage files with information which conceptually belong together but in context are not depending on each other is the act of *appending*. the active choice of 
    1. not taking in unrelated information in current context. 
    2. a single row from a single append is a single unit of information
    3. ...
    4. born from the concept of "dump-files" and that to focus on the current objective while still making sure to not miss important things along the way - "dumping your ideas, tasks and so on" is a necessary evil in all projects and this is a simple and effective solution.

## acting without asking

a file that contradicts the code, the log or another file is wrong, not a question. fix it in the same turn and say what changed. ask only when the intention itself is unclear: two readings both fit the facts and the choice changes what gets built.

an outdated route is worse than no route, since it guides the next reader with confidence. HANDOVER.md, README.md and this file are routes and are kept true the same way.
