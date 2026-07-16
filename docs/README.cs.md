# Pásková kalkulačka pro Android

Jednoduchá kalkulačka, ve které historie není jen pasivní text. Každé číslo na výpočetní pásce je tlačítko a lze ho jedním klepnutím vložit zpět do právě rozepsaného výpočtu.

## Co umí první verze

- sčítání, odčítání, násobení, dělení a mocninu (`^`),
- trvale uloženou pásku až se 100 výpočty,
- opětovné vložení kteréhokoliv operandu nebo výsledku z pásky,
- pět trvalých pamětí M1–M5,
- české zobrazení čísel: desetinnou čárku a mezery mezi tisíci,
- běžné výpočty s přesností 20 platných číslic,
- práci zcela bez internetu, reklam, účtu a analytiky.

## Paměti

- Krátké klepnutí na M1–M5 vloží uloženou hodnotu.
- Dlouhé podržení uloží právě zobrazené číslo do dané paměti.

## Sestavení

1. Otevři kořenovou složku projektu v Android Studiu.
2. Nech proběhnout Gradle Sync.
3. Spusť konfiguraci `app` na telefonu nebo emulátoru s Androidem 6.0 či novějším.

Projekt se kompiluje proti Android SDK 36.1 a cílí na API 36.

První verze je záměrně malá a bez externích knihoven. Po otestování na skutečném telefonu doladíme rozměry, barvy a konkrétní způsob práce s páskou a pamětmi.
