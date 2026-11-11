## 2026-11-07 23:23 · turn f55f0830

**User**

> hi claude! it is insane that we havent done this already since mods came out! always had a fun thing with "local pokemon-like mini-agents helping out with the coding and jumping around the cli while we work"-idea  
>
> think "we push a commit, the pet gets happy and get points or something". the tests are not green? all pets gets upset etc etc. simple reactions on what is happening. 
>
> the big difference would be "they are also running local models/llms which help out" such as "a pet might summarize the last five messages between us into a bigger overview", run tests or format syntax whatever so they may have something more important than to look cute if that makes sense? :)

**Assistant**

skogix — this is a good fit for mods, and your machine already has what it needs. I checked:

- **Hooks:** the mod API can react to tool calls (`git commit`, test runs, failed Bash exit codes), new prompts and finished turns. It can draw a pane beside the chat, or a band above the prompt, that redraws whenever state changes.
- **Local LLM:** ollama is running on `:11434` with `llama3.2` (2 GB) and `gemma4` (6.6 GB) installed. A mod can call it with `$.http.fetch`, so the pets' work runs locally and costs no Claude tokens.
- **Shell jobs:** `$.process.run` lets a pet run real commands like tests or formatters, and `$.clock.every` lets it work in the background.

Your CLAUDE.md says to plan before non-trivial work, so here's the plan before I build.

## Plan: `skogpets` mod (v1)

**1. Reactions (pure hooks, no LLM)**
| event | detected via | pet mood |
|---|---|---|
| commit lands | `tool.call` Bash, `git commit` exit 0 | happy, +XP to all |
| tests green | Bash matching test runners, exit 0 | proud, +XP |
| tests red | same, exit ≠ 0 | upset, -mood for all |
| any command fails | Bash exit ≠ 0 | worried |
| long idle | `$.clock` | sleepy |

The XP, levels and moods carry over between sessions in `$.store`, so the pets grow over time.

**2. A pane beside the chat:** a small ASCII sprite per pet, its mood, an XP bar, and one speech-bubble line saying what it just did. It falls back to a single band above the prompt when the terminal is narrower than 110 columns.

**3. Each pet has a job, starting with two:**
- **Scribe** (`llama3.2`): every 5 turns, it summarizes the latest exchanges into `memory/pet-digest.md`. That file is append-only, which fits SKOGAI.md's dump-file idea.
- **Tester**: after edits, it runs the project's test command in the background and reacts. I'll leave a formatter pet for v2.

**4. Commands:** `/pets` shows stats and `/pet <name>` lets you pet one.

Files go in `/skogai/claude/dev-mods/6cbcc33f-d664-4665-b241-42c9a3c7c9c0/skogpets/`, with a `*.test.ts` run by `claude plugin test`. Once you approve, Claude Code asks once whether to turn on the mods I write in this session.

## What I'd push back on / need from you

1. **Scope of the Scribe's input.** The pet would read our transcript and pass it to a local model. It all stays on this machine, but I'd still have it read only the last few turns, not the full history.
2. **Writing into the repo.** If the Scribe writes `memory/pet-digest.md` here, it creates noise in git. Should it go to that file, or to a separate pets folder outside the repo?
3. **Tester trigger.** Which test command should it run? `package.json` exists but I didn't see a real test suite. I'd start by having pets only *react* to tests you or I run, and add the autonomous runner once there's a suite.
4. **Art style.** I can use plain ASCII critters or Unicode block sprites in the style of the `design/` pixel look. I'd start with ASCII and move to blocks later.

Say go ahead, with any corrections, and I'll build v1.

**Actions**

- Skill: plugin-authoring
- Bash: `D=/tmp/claude-1000/bundled-skills/2.1.319/ec322bbdb0dc424e08e934d9784ced16/plugin-authoring; rtk ls $D/examples; rtk gre…`
- Bash: `D=/tmp/claude-1000/bundled-skills/2.1.319/ec322bbdb0dc424e08e934d9784ced16/plugin-authoring; sed -n 200,300p $D/referenc…`

