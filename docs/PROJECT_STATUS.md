# Stav projektu TapeCalculator

Aktualizováno: 16. července 2026

## Zdroj pravdy

Veřejný repozitář: https://github.com/Batwi/TapeCalculator

Kód a tento stavový dokument jsou přenosným kontextem mezi pracovními počítači. Historie místních konverzací Codexu nemusí být na jiném počítači dostupná.

## Aktuální stav

Verze `0.1.0` je časný funkční prototyp.

Hotovo:

- základní Android projekt v Javě,
- výpočty `+`, `−`, `×`, `÷` a mocnění,
- interaktivní historie výpočtů s maximálně 100 položkami,
- možnost znovu použít číslo z historie,
- pět trvalých pamětí M1–M5,
- české texty a formátování čísel,
- jednotkové testy výpočetního jádra,
- Gradle konfigurace a wrapper,
- MIT licence, README, bezpečnostní a přispěvatelské dokumenty,
- ověřené sestavení debug APK.

## Vývojové prostředí

Projekt byl otevřen a synchronizován v Android Studiu Quail 2 (2026.1.2) se SDK Android 36.1.

Domácí pracovní kopie je připravená v běžné místní složce mimo OneDrive. Dne 16. července 2026 zde prošly příkazy `test` i `assembleDebug` s JDK 17 dodávaným v Android Studiu. V terminálu je potřeba pro běh Gradlu použít `C:\Program Files\Android\Android Studio\jbr`, protože systémová proměnná `JAVA_HOME` zatím není nastavená.

Na domácím počítači zatím není vytvořené AVD ani nainstalovaný systémový obraz Androidu a přes ADB není připojený žádný telefon.

Současný Android Emulator vyžaduje modernější procesorové virtualizační funkce. Na starším počítači proto použij fyzický telefon nebo emulátor na novějším počítači. Opakované zapínání Windows Hypervisor Platform na nepodporovaném procesoru problém nevyřeší.

## Nejbližší postup

1. V Android Studiu otevřít Device Manager, doinstalovat systémový obraz a vytvořit emulátor Pixel; případně připojit telefon přes USB debugging.
2. Spustit aplikaci a pořídit snímek skutečného rozhraní.
3. Podle testu doladit rozložení, velikosti tlačítek, pásku a ovládání pamětí.
4. Ověřit klepání na operandy i výsledky v historii a doplnit testy nalezených okrajových případů.

## Jak začít nový úkol Codexu

Po otevření naklonované složky vlož do nového úkolu:

> Přečti AGENTS.md, README.md a docs/PROJECT_STATUS.md. Pokračuj ve vývoji TapeCalculatoru od uvedeného nejbližšího kroku. Komunikuj se mnou česky.
