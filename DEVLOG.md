# DEVLOG — Cluttered (Unofficial Fabric Port), Gameoverse fork

## 2026-09-18: Fixed a real crash-on-boot bug in `CustomJukeboxBlock`

Wanted to add this mod for its building/decoration/passive-playstyle
content (2 new wood types, chairs, tables, cabinets, lamps, decorative
knick-knacks — see the guide page in `docs/current-state.md` and the
"Building & Decoration" Oracle Index category for the full rundown).
Installing the stock 3.0.3-26.1.x-0.2.3-fabric-unofficial build crashed
the server on startup:

```
java.lang.IllegalArgumentException: Block{[unregistered]} has duplicate property: facing
  at StateDefinition$Builder.validateProperty
  at StateDefinition$Builder.add
  at CustomJukeboxBlock.createBlockStateDefinition
  at TraditionalRadioBlock.<init>
```

**Root cause**: `CustomJukeboxBlock extends JukeboxBlock` (vanilla).
Vanilla `JukeboxBlock` in this project's Minecraft version (26.1.2)
already registers `FACING` in its own `createBlockStateDefinition`
(jukeboxes are directional now — a real vanilla change this port
predates). `CustomJukeboxBlock.createBlockStateDefinition` called
`super.createBlockStateDefinition(pBuilder)` and then added the same
`BlockStateProperties.HORIZONTAL_FACING` property a second time,
tripping Minecraft's own duplicate-property guard the moment any block
using this class (here, `TraditionalRadioBlock`) gets instantiated.

**Fix**: removed the redundant `pBuilder.add(FACING);` call in
`CustomJukeboxBlock.createBlockStateDefinition` — `FACING` is still on
the block's state definition via the inherited vanilla registration, so
nothing downstream (including `TraditionalRadioBlock`'s
`pState.getValue(FACING)` shape lookup) needed to change. One-line fix,
see the commit for the exact diff.

**Audited the rest of the codebase for the same pattern** before calling
this done, not just patching the one reported crash: grepped every
`createBlockStateDefinition` override that adds `FACING` after a `super`
call. All the others (`CustomHorizontalBlock`, `CustomHorizontalWoodBlock`,
`DirectionalPillarBlock`, `CustomHorizontalGlassBlock`, and their
subclasses) sit on top of either the mod's own custom base classes or
vanilla's abstract `HorizontalDirectionalBlock`/`DirectionalBlock` helper
classes — those do **not** auto-register `FACING` themselves in real
Minecraft (subclasses are expected to add it explicitly), so those calls
are the correct, standard pattern, not bugs. `JukeboxBlock` is the only
concrete vanilla block among all of them whose own `FACING` handling
changed, which is exactly why only `CustomJukeboxBlock`/
`TraditionalRadioBlock` broke.

**Verified**: built with `./gradlew build` (`BUILD SUCCESSFUL`), boot-
tested the resulting jar on the local Gameoverse test server alongside
330+ other mods — clean boot, no new errors beyond this project's
existing pre-documented noise (`endrem` advancement issue, tick-scheduling
warnings).

Built jar deployed to `fabric 26.1/mods/` on the Gameoverse project as
`cluttered-3.0.3-26.1.x-0.2.3-fabric-unofficial.jar` (same filename as
upstream — only the fix differs, not the version).

**Upstreamed**: opened as a clean, fix-only PR (no Gameoverse-specific
DEVLOG content) at https://github.com/BoyAxl/Cluttered-Fabric-Port/pull/3,
from a fork at https://github.com/WasabiIceCream/Cluttered-Fabric-Port,
branch `fix/jukebox-facing-duplicate-property` off `port/fabric-26.1.x`.
