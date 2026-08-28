# TypeWriter-EnchantmentCreator

Extension Typewriter pour créer des enchantements vanilla ou entièrement
custom, avec mécaniques modulaires, slots actifs, niveaux, loot, échanges et
actions compatibles avec le moteur Typewriter.

## Compatibilité de l’artefact public

| Élément | Cible |
| --- | --- |
| Moteur | Typewriter officiel |
| Serveur | Paper 1.21.x |
| Runtime | Java 21 minimum/garanti |
| Compilation | JDK 25 autorisé, bytecode Java 21 |
| Dépendance | BasicExtension officiel |
| Folia/BTC-CORE/BTC Velocity | Hors périmètre de cet artefact |

La variante BTC custom se trouve dans le dépôt BORNTOCRAFT-Typewriter. Elle
utilise les APIs BTC-CORE/BTC Velocity, Java 25 et les schedulers régionaux.
Les deux variantes partagent le même modèle de données Typewriter ; leurs
adapters de plateforme restent séparés afin d’éviter toute dépendance BTC dans
l’extension publique.

Les clés d’enchantement sont désormais basées sur `id`. L’ancien `name` reste
chargé comme alias afin de préserver les pages et objets existants.


---

## 📜 Licence

**GNU General Public License v3.0 or later** — [LICENSE](LICENSE) — with a
**linking exception** for the Typewriter engine — [LICENSE-EXCEPTION.md](LICENSE-EXCEPTION.md).

| | |
|---|---|
| You may | Run it anywhere, **including on a monetised server**. Study it, modify it, use it as a base, and redistribute it — **even for a fee**. GPLv3 §4 explicitly allows charging for a copy. |
| You must | Publish the complete corresponding source of your version under GPLv3, preserve the copyright notices, and **state that you modified it and when** (§5(a)). |
| You may not | Ship a closed-source or proprietary version, relicense under stricter terms, or strip the attribution and present this work as your own — §8 terminates your rights automatically. |
| Marks | **"Born To Craft"** and **"BTC Studio"** are **not** covered by the GPL. Fork it freely, sell your fork if you like — but **rebrand it**. |

> Reselling this code is legally allowed and practically pointless: whoever buys a
> copy from you receives, under the GPL, the right to redistribute it for free.
> That is the protection — not a clause forbidding sale, which the GPL does not
> permit us to add.

### About Typewriter

This is a **third-party extension**. It uses the public extension API of the
[Typewriter](https://github.com/gabber235/Typewriter) engine by gabber235 and
contains none of its source. Born To Craft Studio is not affiliated with or
endorsed by the Typewriter project.

The engine itself is **not** free software — its licence forbids redistributing
it. **Get it from the Typewriter project, and never redistribute it**, including
inside a fork of this repository.

Full attribution, the statement of modifications required by §5(a), and the
trademark reservation are in **[NOTICE.md](NOTICE.md)**. Read it before
redistributing.

© 2026 Born To Craft Studio.
