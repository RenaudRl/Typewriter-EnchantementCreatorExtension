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

