# q2 — Where do the small hacker/automation tasks execute?

status: skipped
act: ask
kind: choice
thread: stack
agent: c99fb6e2-6cec-4b55-8679-1c796bd643f8
cwd: .
asked at: 2026-10-24T16:21:02.694263+00:00

## Context

Built with the declarative option: quests/*.json steps are choice or
regex text, checked on the server so answers never reach phones before
reveal. No player code runs anywhere. Pick sandbox only if you want real
snippets executed later; it slots in as a new step kind.

## Options

- phone-side — task is a puzzle/fill-in, checked by pure JSON validator
+ no code execution on server, safe
- tasks limited to what can be checked declaratively  (★●)
- sandbox — real snippets run in a server sandbox (wasm/deno)
+ real automation feel
- sandboxing is real work and a security surface

## Recommendation

phone-side — high

## Answer

skipped
answered at: 2026-10-24T17:45:12.087432+00:00
