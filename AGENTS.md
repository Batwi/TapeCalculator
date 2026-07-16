# Pokyny pro práci na projektu

## Komunikace

- S vlastníkem projektu komunikuj česky a vysvětluj postup po malých, ověřitelných krocích.
- Neopakuj již provedené kroky. Nejdřív zkontroluj aktuální stav repozitáře a soubor `docs/PROJECT_STATUS.md`.
- Pokud uživatel pošle snímek obrazovky, vycházej z přesně viditelného stavu.

## Cíl aplikace

TapeCalculator je jednoduchá kalkulačka pro Android inspirovaná stolní kalkulačkou s páskou historie.

Požadované základní funkce:

- sčítání, odčítání, násobení, dělení a mocnění,
- interaktivní páska výpočtů,
- klepnutí na libovolné číslo na pásce jej přenese do aktuálního výpočtu,
- pět trvalých pamětí M1–M5,
- české formátování čísel,
- jednoduché ovládání bez zbytečných vědeckých funkcí.

## Technická pravidla

- Nativní Android aplikace v Javě.
- Package: `cz.batwi.tapecalculator`.
- Minimální Android: API 23.
- Zachovej soukromí: žádné reklamy, analytika, účty, síťový přístup ani nepotřebná oprávnění.
- Nevkládej do Gitu `local.properties`, sestavené APK, build adresáře, místní SDK cesty, přihlašovací údaje ani jiné tajné údaje.
- Upřednostni jednoduché, čitelné řešení a malé změny, které lze otestovat.

## Pracovní postup mezi počítači

1. Před zahájením práce stáhni poslední stav z GitHubu.
2. Přečti `README.md` a `docs/PROJECT_STATUS.md`.
3. Po významné změně aktualizuj `docs/PROJECT_STATUS.md`.
4. Proveď odpovídající testy a sestavení, pokud to prostředí dovolí.
5. Změny commitni a pushni na GitHub, aby byly dostupné na ostatních počítačích.

Doporučené ověření ve Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```
