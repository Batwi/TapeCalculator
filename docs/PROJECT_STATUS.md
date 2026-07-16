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

Současný Android Emulator vyžaduje modernější procesorové virtualizační funkce. Na starším počítači proto použij fyzický telefon nebo emulátor na novějším počítači. Opakované zapínání Windows Hypervisor Platform na nepodporovaném procesoru problém nevyřeší.

## Nejbližší postup

1. Naklonovat repozitář na domácí počítač do běžné místní složky mimo OneDrive.
2. Otevřít projekt v Android Studiu a dokončit Gradle synchronizaci s místním Android SDK.
3. Vytvořit emulátor Pixel na novějším procesoru, případně připojit telefon přes USB debugging.
4. Spustit aplikaci a pořídit snímek skutečného rozhraní.
5. Podle testu doladit rozložení, velikosti tlačítek, pásku a ovládání pamětí.
6. Ověřit klepání na operandy i výsledky v historii a doplnit testy nalezených okrajových případů.

## Jak začít nový úkol Codexu

Po otevření naklonované složky vlož do nového úkolu:

> Přečti AGENTS.md, README.md a docs/PROJECT_STATUS.md. Pokračuj ve vývoji TapeCalculatoru od uvedeného nejbližšího kroku. Komunikuj se mnou česky.
