# Changelog

## 0.8 — 2026-08-12

- `levelMode` on a mechanic compares `runOnLevel` as an exact level, a minimum (`AT_LEAST`) or a
  maximum (`AT_MOST`). Previously only an exact match was possible.
- `eventPhase` runs a mechanic's actions during the current Bukkit event, so cancelling the event
  or changing the damage actually takes effect. Set it to false for queued Typewriter
  interactions.
- `clientSideEffects` became client-aware, with a `client_particle_effect` action: the custom BTC
  artifact uses the PacketEvents-backed renderer when available, the public Paper artifact a
  player-scoped Bukkit fallback.
- Registry keys are derived from `id`. The previous `name` is kept as a legacy alias when loading
  existing pages and items, so nothing has to be re-authored.
- The public artifact deliberately carries no BTC-CORE or BTC Velocity dependency: it runs on
  Paper 1.21.x with Java 21, built from JDK 25 targeting Java 21 bytecode.
