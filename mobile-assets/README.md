# MINEARENA 1.0 — Mobile UI & Asset Specification

This document provides the complete UI layout, alignment, dimensions, styling properties, and asset mappings for building the mobile/Android client of **MINEARENA 1.0**.

All 58 asset files have been extracted and organized in the local [`mobile-assets/`](file:///home/mattmurdock/reactive-ether/mobile-assets) directory.

---

## 1. Asset Directory Index (`mobile-assets/`)

```text
mobile-assets/
├── textures/
│   ├── bedrock.png
│   ├── book_bg.png
│   ├── chain_strip.png
│   ├── chain_strip_rotated.png
│   ├── deepslate.png
│   ├── dirt.png
│   ├── grass.png
│   ├── minecart.png
│   ├── oak_log.png
│   ├── oak_sign.png
│   ├── rail_strip.png
│   ├── sculk_still.png
│   ├── stone.png
│   └── wooden_seam.png
├── backgrounds/
│   ├── about_bg.png
│   ├── about_left.png
│   ├── about_right.png
│   ├── faq_bg.png
│   ├── faq_left.png
│   ├── faq_right.png
│   ├── prev_bg.png
│   ├── prev_left.png
│   ├── prev_right.png
│   ├── SPONSOR_BG.png
│   ├── SPONSOR_BG_LEFT.png
│   ├── SPONSOR_BG_RIGHT.png
│   ├── timeline_bg.png
│   ├── timeline_left.png
│   ├── timeline_right.png
│   ├── tracks_bg.png
│   ├── tracks_left.png
│   └── tracks_right.png
├── icons/
│   ├── copper_stone.png
│   ├── deepslate-iron-ore.png
│   ├── diamond_deepslate.png
│   ├── gold_nether.png
│   ├── item-coal.png
│   ├── item-diamond.png
│   ├── item-ender-eye.png
│   ├── item-firework.png
│   ├── item-glowstone.png
│   ├── item-lapis.png
│   ├── item-porkchop.png
│   ├── item-redstone.png
│   └── netherite-ore.png
├── prizes/
│   ├── crystal.webp
│   ├── pillar-1.png
│   ├── pillar-2.png
│   ├── pillar-3.png
│   ├── pillar-4.png
│   ├── plaque-1.png
│   ├── plaque-2.png
│   ├── plaque-3.png
│   ├── plaque-4.png
│   └── podium.png
├── sponsors/
│   ├── cleo.png
│   ├── elevenlabs.jpeg
│   ├── inube.jpg
│   ├── logic-brackets.png
│   ├── namana.jpeg
│   ├── prolim.png
│   └── rakuten.png
└── branding/
    ├── minecraft-hello.gif
    └── navbar-logo.PNG
```

---

## 2. Design System Tokens & Typography

* **Typography**:
  * **Pixel Headings**: `Press Start 2P` (Uppercase, pixelated retro gaming font).
  * **Body / Monospace**: `VT323` (Terminal retro monospace font).
* **Color Palette**:
  * App Background: `#1a1206` (deep dark earthy brown)
  * Card Surface: `#241608`
  * Foreground Text: `#f5f5f0` (off-white)
  * Primary / Gold Accent: `#dba213` / `#fbbf24`
  * Secondary Dirt: `#3a2a17`
  * Redstone Red (Rules): `#ff5555`
  * Creeper Green: `#41c65b`
  * Borders: `#493606`, `#3a2615`, `#1f2e14`
* **Pixel Rendering**:
  * Textures and pixel art should be rendered with Nearest-Neighbor filtering (`FilterQuality.None` in Jetpack Compose).

---

## 3. Element Properties & Asset References

| Component | Element | Asset File (`mobile-assets/`) | Visual & Layout Properties |
| :--- | :--- | :--- | :--- |
| **Top Bar** | Grass Edge | `textures/grass.png` | Height: `10dp`, repeat `16dp x 16dp`, border `2dp` `#1f2e14`, top offset `-9dp` |
| | Dirt Body | `textures/dirt.png` | Repeat `32dp x 32dp`, border `2dp solid #1f2e14`, radius `bottom 14dp`, shadow `0 10dp 28dp rgba(0,0,0,0.4)` |
| | Logo | `branding/navbar-logo.PNG` | Height: `28dp`, drop-shadow: `0 2dp 4dp rgba(0,0,0,0.6)` |
| | Register Button | — | Bg: `#fbbf24`, border: `2dp solid #7a4f05`, text: `#1a1206`, font: `Press Start 2P` (`8sp`), shadow: `inset 2dp 2dp 0 rgba(255,255,255,0.3), inset -2dp -2dp 0 rgba(0,0,0,0.3)` |
| **Hero** | Background Sequence | `Main-bg-frames/frames_0001.jpg`–`0504.jpg` | 504 frames, max 600vh scroll on mobile |
| | Logo | `branding/navbar-logo.PNG` | Max width: `320dp` (`w-[92%]`), drop-shadow `0 6dp 14dp rgba(0,0,0,0.7)` |
| | Prize Badge | — | Border: `2dp solid rgba(219,162,19,0.6)`, bg: `rgba(219,162,19,0.15)`, font: `Press Start 2P` (`10sp`), text: `#ffd875` |
| | Venue & Date | — | Venue: `VT323` (`18sp`, `#e0dacb`), Reg Date: `VT323` (`16sp`, `#9ae6b4`) |
| | Primary CTA | — | Bg: `#dba213`, border: `2dp solid #1a1206`, text: `#1a1206`, font: `Press Start 2P` (`11sp`), shadow: `0 4dp 0 #1a1206` |
| | Secondary CTA | — | Bg: `rgba(0,0,0,0.4)`, border: `2dp solid #dba213`, text: `#dba213`, font: `Press Start 2P` (`11sp`) |
| **Dividers** | Wooden Seam | `textures/wooden_seam.png` | Aspect ratio: `1266 / 110`, width: `100%`, centered on section boundaries |
| **About** | Background & Flanks | `backgrounds/about_bg.png`<br>`backgrounds/about_left.png`<br>`backgrounds/about_right.png` | Center cover, flanks pinned to bottom edges, max width `46vw` |
| | Book Parchment | `textures/book_bg.png` | Background size: `100% 100%`, scaleX: `-1` for left page |
| | Stats HUD | — | Bg: `#e9e0ca/90`, border: `2dp solid #3a2a17`, inner dividers: `2dp solid #3a2a17/40`, numbers: `Press Start 2P` (`12sp` / `10sp`), text `#3a2a17` |
| | Notice Board | `textures/book_bg.png` | Heading: `Press Start 2P` (`11sp`), dashed divider `2dp #3a2a17/50` |
| | Social: WhatsApp | — | Bg: `#25D366`, border: `2dp solid #128C7E` |
| | Social: Instagram | — | Bg: `#e1306c`, border: `2dp solid #7a1238` |
| | Social: LinkedIn | — | Bg: `#0a66c2`, border: `2dp solid #053a70` |
| **Tracks** | Background & Flanks | `backgrounds/tracks_bg.png`<br>`backgrounds/tracks_left.png`<br>`backgrounds/tracks_right.png` | Dark tint: `#0c0c0e/30`, flanks pinned top-to-bottom edge strips |
| | Header Log Beam | `textures/oak_log.png` | Height: `10dp`, repeat: `16dp x 16dp`, shadow: `inset 0 2dp 0 rgba(255,255,255,0.16), inset 0 -2dp 0 rgba(0,0,0,0.4)` |
| | Dual Chains | — | Width: `4dp`, height: `12dp`, bg: `#6e6e6e`, borders: left `2dp #8f8f8f`, right `2dp #2e2e2e` |
| | Signboard Face | `textures/oak_sign.png` | Border: `3dp solid #4b3418`, tint: `rgba(233,207,157,0.65)`, shadow: `0 10dp 24dp rgba(0,0,0,0.45)` |
| | Track 1 Ore & Bar | `icons/copper_stone.png` | Bar: `2dp solid #41a9ff` (Agentic AI) |
| | Track 2 Ore & Bar | `icons/diamond_deepslate.png` | Bar: `2dp solid #ff66ff` (GenAI for Enterprise) |
| | Track 3 Ore & Bar | `icons/gold_nether.png` | Bar: `2dp solid #f7c948` (FinTech) |
| | Track 4 Ore & Bar | `textures/grass.png` | Bar: `2dp solid #ff4d4d` (HealthTech) |
| | Track 5 Ore & Bar | `icons/deepslate-iron-ore.png` | Bar: `2dp solid #36e6b8` (Data Engineering) |
| | Track 6 Ore & Bar | `icons/netherite-ore.png` | Bar: `2dp solid #7ef9a5` (ClimateTech) |
| **Timeline** | Center Chain Rail | `textures/chain_strip.png` | Width: `6dp`, repeat Y, glow: `drop-shadow(0 0 2dp rgba(233,240,255,0.85))` |
| | Horizontal Rails | `textures/rail_strip.png` | Height: `24dp`, repeat X |
| | Minecart | `textures/minecart.png` | Width: `40dp`, height: `23dp`, hitch bar: `3dp` `#6e6e6e` |
| | Card Top Chain | `textures/chain_strip_rotated.png`| Height: `6dp`, repeat X |
| | Milestone Card | — | Bg: `#39418a`, border: `2dp solid #212752`, inner border: `2dp solid #ced6ff3d`, date: `Press Start 2P` (`7sp`), title: `Press Start 2P` (`8sp`) |
| **Rules** | Card Surface | `textures/dirt.png` | Repeat `32dp x 32dp`, tint `rgba(26,18,6,0.88)`, border `3dp solid #3a2615`, inner shadow `#120b03` |
| | Rule Title | — | Font: `Press Start 2P` (`24sp`), color: `#ff5555`, shadow: `0 0 10px rgba(255,85,85,0.5), 2dp 2dp 0 #3a0000` |
| | List Text | — | Numbers: `Press Start 2P` (`16sp`, `#ff5555`), Body: `VT323` (`20sp`, `#f5f5f0`) |
| **Prizes** | Background | `textures/sculk_still.png` | Repeat `32dp x 32dp` |
| | 1st Place Column | `prizes/plaque-1.png`<br>`prizes/pillar-1.png`<br>`prizes/podium.png` | Glow: `radial-gradient(rgba(252,220,95,0.45), transparent 70%)`, pillar aspect: `0.45` |
| | 2nd Place Column | `prizes/plaque-2.png`<br>`prizes/pillar-2.png`<br>`prizes/podium.png` | Glow: `radial-gradient(rgba(255,110,230,0.5), transparent 70%)`, pillar aspect: `0.45` |
| | Special Recognition | `prizes/plaque-3.png`<br>`prizes/pillar-3.png`<br>`prizes/podium.png` | Glow: `radial-gradient(rgba(255,110,230,0.5), transparent 70%)`, pillar aspect: `0.49` |
| | Consolation Prize | `prizes/plaque-4.png`<br>`prizes/pillar-4.png`<br>`prizes/podium.png` | Glow: `radial-gradient(rgba(255,110,230,0.5), transparent 70%)`, pillar aspect: `0.49` |
| | End Crystal | `prizes/crystal.webp` | Animation: bob `-6dp`, wobble `-4deg` to `4deg`, width `24%`–`28%` |
| **Sponsors** | Advancement Frame | — | Bg: `#c6c6c6`, border: `3dp solid #1a1a1a`, corner cutouts: `6dp`, shadow: `inset 4dp 4dp 0 #f7f7f7, inset -4dp -4dp 0 #737373` |
| | Title Inset Slot | — | Bg: `#353535`, shadow: `inset 3dp 3dp 0 #1f1f1f`, font: `Press Start 2P` (`12sp`) |
| | Gold Frame | — | Bg: `#9e7a2b`, border: `3dp solid #1a1a1a`, shadow: `inset 3dp 3dp 0 #bf993b` |
| | Sponsor Logos | `sponsors/cleo.png`<br>`sponsors/namana.jpeg`<br>`sponsors/inube.jpg`<br>`sponsors/rakuten.png`<br>`sponsors/prolim.png`<br>`sponsors/logic-brackets.png`<br>`sponsors/elevenlabs.jpeg` | White surface tile, aspect `2.6 / 1`, logo max height `80dp`, fit `contain` |
| | Item Icons | `icons/item-diamond.png`<br>`icons/item-lapis.png`<br>`icons/item-porkchop.png`<br>`icons/item-glowstone.png`<br>`icons/item-ender-eye.png`<br>`icons/item-firework.png`<br>`icons/item-redstone.png`<br>`icons/item-coal.png` | Box size: `44dp x 44dp`, icon size: `28dp x 28dp` |
| **FAQs** | Question Header | `textures/stone.png` | Repeat `32dp x 32dp`, tint `rgba(12,12,14,0.4)`, border `2dp solid #1f1f1f`, font: `Press Start 2P` (`12sp`, `#ffffff`) |
| | Answer Body | `textures/deepslate.png` | Repeat `32dp x 32dp`, tint `rgba(12,12,14,0.82)`, border-top `1dp solid #1f1f1f`, font: `VT323` (`24sp`, `white/85`) |
| **Footer** | Background | `textures/bedrock.png` | Repeat `64dp x 64dp`, tint `rgba(18,11,3,0.95)`, base `#0a0a0a` |
| | Advancement Banner | `textures/bedrock.png` (icon 48dp) | Bg: `#1a1206`, border: `3dp solid #3a2615`, title: `Press Start 2P` (`10sp`, `#ffff00`), sub: `Press Start 2P` (`10sp`, `#ffffff`) |

---

## 4. Mobile-Specific Layout & Alignment Rules

### Top Bar / Navbar
* **Position**: Fixed `8dp` horizontal margin (`inset-x-2`), `16dp` from top.
* **Visibility**: Desktop navigation links are **hidden**.
* **Actions**: Shows `28dp` logo on left, compact Register button (`8sp`, label: `"REGISTER"`), and a `36dp x 36dp` hamburger menu button on right.
* **Dropdown**: Expands as a vertical column with `8dp` top margin on dirt background, items aligned left.

### Hero Section
* **Stack**: Content centered vertically and horizontally.
* **Buttons**: Stacked **vertically** (`flex-col gap-3`), centered.
* **Logo Size**: Constrained to `92%` screen width (max `320dp`).

### About Section
* **Width Constraint**: The card container is **constrained to `78%` width** (`w-[78%] mx-auto`) so the parchment does not cover the character flanks on screen edges.
* **Stack**: 1-column vertical stack (Left About page on top, Right Notice board underneath).
* **Stats HUD**: 3-column row compressed to `6dp` padding.
* **Social Cards**: Full-width stacked buttons (`flex-col gap-2.5`), icons scaled down to `24dp x 24dp`.

### Tracks Section
* **Grid**: **Single-column vertical stack** (`grid-cols-1`, gap `24dp`).
* **Card Width**: Centered and constrained to **`85%` width** (`w-[85%] mx-auto`).
* **Chains**: Dual chains spaced apart at `64%` of sign width, height `12dp`, width `4dp`.
* **Typography**: Ore icon `16dp x 16dp`, title `8sp`, body `16sp`.

### Timeline Section
* **Center Rail**: Vertical chain pinned down the center (`50%` horizontal, width `6dp`).
* **Milestone Grid**: 2 columns (`grid-cols-[1fr_1fr]`, gap `8dp`).
* **Alternation**:
  * Even milestones: Placed in Column 1 (Left), minecart attached on Right rail edge.
  * Odd milestones: Placed in Column 2 (Right), minecart attached on Left rail edge.
* **Minecart Size**: Scaled down to `40dp x 23dp`.
* **Important Visibility Rule**: Milestone card descriptions are **hidden** (`hidden md:block`) on mobile to preserve vertical rhythm. Only the Date (`7sp`) and Title (`8sp`) are shown.

### Rules Section
* **Layout**: Centered column, `16dp` horizontal padding.
* **List**: Single column vertical stack (`gap-5`), rule numbers aligned top-left in red pixel font (`16sp`).

### Prizes Section
* **Grid**: **12-column layout split into 2 horizontal rows**:
  1. **Top Row (Main Prizes)**: 2 columns side-by-side (`col-span-6` each, max width `176dp`). 1st Place left, 2nd Place right.
  2. **Bottom Row (Other Prizes)**: 3 columns side-by-side (`col-span-4` each, max width `112dp`). Special Recognition 1 & 2, Consolation.
* **Pillar Aspect Ratio**: Top row `0.45`, bottom row `0.49`.
* **Overlap**: Crystal overlaps pillar by `+3%` margin-top; podium overlaps pillar bottom by `-10%` (Top row) and `-8%` (Bottom row).

### Sponsors Section
* **Grid**: Single column vertical stack (`gap: 12dp`).
* **Special Mobile Scale Rule**: Cards are constrained to **`66%` width** (`w-[66%] mx-auto`) and scaled down with **`zoom: 0.6`** (scale factor `0.6`) to prevent overflowing the background flanks.

### FAQ Section
* **Layout**: Single column vertical stack. Full width accordion items with `12dp` vertical gap.
* **Accordion Header**: Question text on left (`12sp`), dropdown arrow on right (`16sp`).

### Footer Section
* **Layout**: Single column vertical stack (`flex-col items-center gap-12`).
* **Alignment**: All blocks, text, and headings are **centered horizontally**.
* **Map**: Aspect ratio `4:3`, full width up to `600dp`.
* **Advancement Banner**: Centered horizontally, max width `420dp`.

### Cursor Follower (Mascot)
* **Mobile Rule**: Completely disabled / hidden on mobile touch devices.
