# Skogai Arcade Design System — agent skill

When the user asks for a design that should look like part of the Skogai family (Sudo Hero, Wi-Fi Wizards, Honkers & Hackers, or anything described as "arcade", "pixel", "neon", "retro game"), use this design system.

## Setup

Always link both stylesheets in this order at the top of the file:

```html
<link rel="stylesheet" href="colors_and_type.css" />
<link rel="stylesheet" href="components.css" />
```

Adjust the relative path based on where the new HTML file lives.

## Decision flow

1. **Pick a cabinet.** Sudo Hero (violet) for solo / coding / dashboard surfaces. Wi-Fi Wizards (cyan) for co-op / network / live surfaces. Honkers & Hackers (yellow) for casual / time-attack / playful surfaces. If unclear, ask.
2. **Use semantic vars first** — `var(--sk-primary)`, `var(--sk-fg)`, `var(--sk-card)`. Reach for raw palette tokens (`--sk-neon-cyan`) only when building a cabinet variant.
3. **One primary per screen.** Reserve `--sk-primary` for the single hero CTA. Other actions use outline/ghost/secondary.
4. **Ladder before shadow.** Build depth by stepping `--sk-bg → --sk-bg-2 → --sk-card`. Use shadow only for overlays.
5. **Glow only on interactive neon** — primary buttons, focus rings, score plaques, marquee headlines. Never on body copy or large fills.

## Type rules (non-negotiable)

- Display headlines → `var(--sk-font-display)` (Press Start 2P), 16–60px, uppercase.
- UI / chrome (buttons, labels, badges) → `var(--sk-font-ui)` (Silkscreen), 10–16px.
- Body copy → `var(--sk-font-body)` (VT323), 18–24px, sentence case.
- Code / paths / scores → `var(--sk-font-mono)` (JetBrains Mono).
- Always set `-webkit-font-smoothing: none` on `body` (already in `colors_and_type.css`). Don't override it.

## Component cheat sheet

| Need | Use |
|---|---|
| Hero CTA | `<button class="sk-btn sk-btn-primary">▶ START</button>` |
| Utility action | `.sk-btn.sk-btn-outline` or `.sk-btn-ghost` |
| Live status pill | `<span class="sk-badge sk-badge-cyan">▣ ONLINE</span>` |
| Score / HP readout | `.sk-plaque.sk-plaque-violet` (label + value) |
| Bottom mission rail | `.sk-quest-strip` containing 2–4 `.sk-quest` items |
| Description card | `.sk-card.sk-card-default` with `.sk-card-eyebrow` + `-title` + `-body` |
| Terminal output | `<pre class="sk-terminal-block">…</pre>` |

## Layout principles

- **Single canvas shell.** App frame on the outside, content centered. No marketing-style hero blocks bolted on top.
- **Region order:** status header (avatar + brand) → content hero → quest strip → score footer. Adapt; don't dogmatically copy.
- **Snap spacing to 4 / 8 / 12 / 16 / 24 / 32.** Interior gaps smaller than section gaps.
- **Square edges.** Radius 0–6px max. No pill buttons, no rounded blob containers.

## Don't list (refuse these even if asked)

- No gradients on backgrounds.
- No tracked-uppercase body copy.
- No rounded pill buttons.
- No glow on body text.
- No "AI sparkle" iconography.
- No mixing two cabinet accents on one screen.
- No body copy below 18px.

## When asked for variations

Offer cabinet switches first (Sudo / Wizards / Honkers), then density (compact vs roomy), then mascot vignette on/off. Keep the chassis identical across variations — only accent + ornament change.
