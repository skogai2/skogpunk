# Skogai Arcade Design System

A pixel-art, neon-arcade design language for the Skogai product family. The system is dark-canvas-first, with sparse saturated neon accents, square-edged pixel UI, and three sub-brand "cabinets" sharing one chassis.

## Quick start

```html
<link rel="stylesheet" href="colors_and_type.css" />
<link rel="stylesheet" href="components.css" />
```

Use semantic CSS variables (`--sk-primary`, `--sk-fg`, `--sk-card`) before raw palette values (`--sk-neon-magenta`, `#b26bff`).

## Files

| File | Purpose |
|---|---|
| `colors_and_type.css` | Tokens: palette, semantic roles, type scale, spacing, radius, shadows, glow utilities. |
| `components.css`      | Buttons, badges, plaques, quest strip, cards, inputs, terminal block. |
| `DESIGN.md`           | Source brief — atmosphere, layout principles, do's & don'ts. |
| `preview/*.html`      | Design-system reference cards (Colors, Type, Spacing, Components, Brand). |
| `assets/`             | Reference scenes for the three cabinets. |

---

## CONTENT FUNDAMENTALS

### Voice & tone

Skogai writes like an arcade marquee that knows it's also a developer tool. Confident, terse, playful — never corporate, never twee.

- **Use the verb of the moment.** `START`, `DEPLOY`, `PATCH`, `RUN`, `INSERT COIN`. Verbs go first; CTAs are commands, not invitations.
- **Pixel labels are short.** Display headlines: 1–4 words. Button labels: 1–3 words. Long sentences live in `VT323` body, never in `Press Start 2P`.
- **Game metaphors are house style** — quests, bosses, ranks (S+, A, B), loot, HP, XP. Use them where they help users feel progress; never to obscure what something is.
- **No emoji unless arcade-coded** — `▶ ▣ ★ ✓ ✕ ⚡ ▸` are in. Modern emoji like 🚀 are out.
- **Never UPPERCASE BODY COPY.** Display type is uppercase by tradition; body stays sentence case.

### Numbers & data

- Pad scores: `012,750` not `12750`.
- Use monospace for any value: time `02:18`, version `v0.9.7`, paths `/quests/03`.
- Ranks are single glyph plus modifier: `S+`, `A`, `B`. Never spell out.

---

## VISUAL FOUNDATIONS

### Atmosphere

Arcade, neon, pixel-art, playful, retro, celebratory — but **disciplined**. The cabinet is dark and quiet; the neon is rare and earned. Every screen feels like the title plate of a game you want to play, not a stadium of flashing signs.

### Surface ladder (dark-canvas)

| Level | Token | Use |
|---|---|---|
| Void / canvas | `--sk-bg` (#0b0716) | Outside the cabinet, page background. |
| Shell | `--sk-shell` (#1a1030) | App frame, persistent chrome. |
| Panel | `--sk-bg-2` (#140a26) | Embedded module backdrops. |
| Card | `--sk-card` (#211640) | Contained group, plaque body. |
| Raised | `--sk-card-2` (#2c1d54) | Hover, selected, dialog cluster. |

Step by **tone first, shadow last**. Shadows are reserved for overlays (popovers, modals).

### Color discipline

- **One primary per screen.** Magenta is the loudest voice; if everything is loud, nothing is.
- **Cabinet accents don't mix.** A Sudo Hero screen is violet; a Wi-Fi Wizards screen is cyan; a Honkers screen is yellow. Pick one cabinet per surface.
- **Status colors stay status.** Green means OK, red means failed, yellow means score/coin. Never use them as decorative fills.
- **Neon glows are interactive only.** Glow on a CTA, focus ring, or active plaque — never on body copy or large background regions.

### Typography

- **Display** (`Press Start 2P`) — marquee, page titles, scores. 16–60px.
- **UI / chrome** (`Silkscreen`) — buttons, labels, badges, nav. 10–16px.
- **Body** (`VT323`) — paragraphs, descriptions, dialog. 18–24px floor.
- **Mono** (`JetBrains Mono`) — code, paths, technical values.

Body copy never goes below 18px; chrome labels never above 16px. **Disable font smoothing** (`-webkit-font-smoothing: none`) so pixel fonts stay crisp.

### Spacing rhythm

Snap to `2 / 4 / 8 / 12 / 16 / 24 / 32 / 48 / 64`. Interior component spacing (8/12) is always smaller than section spacing (24/32). Pixel UIs breathe through repetition, not through whitespace.

### Radius

Square is the default. `0` for pixel-perfect plates, `2px` for controls, `4px` for cards, `6px` for overlays. Never round buttons into pills.

---

## ICONOGRAPHY

The Skogai icon system is **glyph-led, not illustration-led**. Use unicode arcade glyphs and short pixel ideograms before reaching for a custom SVG.

| Role | Glyph | Use |
|---|---|---|
| Start / play | `▶` | CTAs that launch a quest, deploy, or run. |
| Stop / cancel | `✕` | Destructive close, abort. |
| Confirm | `✓` | Quest done, validation passed. |
| Active / energized | `⚡` | Boss spawn, live state, urgency. |
| Locked | `🔒` (or `▣` outlined) | Gated content. |
| Reward / featured | `★` | New, highlighted, daily. |
| Forward / arrow | `▸` | Inline emphasis before a label. |
| Status dot | `▣ / ▢` | Online / offline pill prefix. |

When a custom icon is needed, draw it on an **8×8 or 16×16 pixel grid**, single weight, no anti-aliasing. Match the active neon (`currentColor` + glow shadow). Keep the silhouette readable at 16px.

---

## COMPONENTS (at a glance)

See `preview/components.html` for live examples. Class prefix: `.sk-`.

- **Buttons** — `.sk-btn` + `.sk-btn-primary | -secondary | -outline | -ghost | -success | -danger`
- **Badges** — `.sk-badge` + color modifier
- **Plaques (HUD)** — `.sk-plaque` + `.sk-plaque-violet | -cyan | -green`
- **Quest strip** — `.sk-quest-strip > .sk-quest.sk-quest-{done,active,locked}`
- **Cards** — `.sk-card.sk-card-{default,raised}` with `.sk-card-eyebrow`, `-title`, `-body`, `-footer`
- **Inputs** — `.sk-field` wrapping `.sk-input` + `.sk-field-label` + `.sk-field-help`
- **Terminal** — `.sk-terminal-block` (CRT green on near-black)

---

## BRAND CABINETS

Skogai is the parent arcade. Three cabinets sit on the same chassis:

| Cabinet | Accent | Use it for |
|---|---|---|
| **Sudo Hero** | violet `#b26bff` | Solo quests, terminal-led flows, personal dashboards. |
| **Wi-Fi Wizards** | cyan `#4ee2ff` | Co-op multiplayer, network/sync, party UI. |
| **Honkers & Hackers** | yellow `#ffd23f` | Time-attack, casual chaos, daily mini-games. |

Switch cabinets by overriding `--sk-primary` on the page root; never blend two on one screen.

---

## DON'T LIST

- ❌ No gradients on backgrounds. Flat tone steps only.
- ❌ No tracked-uppercase body copy. Display only.
- ❌ No rounded pill buttons. Square edges, 2px radius max.
- ❌ No drop-shadow on body text. Glow is reserved for interactive neon.
- ❌ No "AI sparkle" iconography (✨, gradient orbs, etc.). Pixel glyphs only.
- ❌ No mixing two cabinet accents. One primary per screen.
- ❌ No font smoothing. Pixel must stay pixel.
- ❌ No body copy below 18px or above 24px.
