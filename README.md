# KillStreaks 1.0

Jednoduchý plugin pro Paper 1.21.11.

## Požadavky
- Java 21
- Maven
- Paper 1.21.11

## Build
Ve složce projektu spusť:

```bash
mvn clean package
```

Výsledný plugin najdeš v `target/killstreaks-1.0.0.jar`.

## Instalace
1. Zkopíruj JAR do složky `plugins` na serveru.
2. Restartuj server.
3. Nastavení najdeš v `plugins/KillStreaks/config.yml`.
4. Statistiky se ukládají do `plugins/KillStreaks/stats.yml`.

## Příkazy
- `/streak` – zobrazí vlastní aktuální streak.
- `/streak <hráč>` – zobrazí streak jiného hráče (vyžaduje `killstreaks.admin`).

Alias: `/ks`, `/killstreak`

## Chování
- Kill hráče zvýší killerovi streak o 1.
- Smrt způsobená jiným hráčem resetuje streak oběti.
- Smrt bez PvP killu streak neresetuje.
- Milníky jsou ve výchozím nastavení 3, 5, 10, 15 a 20.
