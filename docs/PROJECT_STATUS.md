# Stav projektu TapeCalculator

Aktualizováno: 17. července 2026

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
- instalace a spuštění na fyzickém telefonu Samsung Galaxy S22 Ultra (SM-S908B, Android 16 / API 36) přes bezdrátové ladění,
- první úprava rozhraní podle testu na telefonu: větší páska, menší klávesnice a panel výsledku, zalamování dlouhých výrazů a čtyři trvale uložené barevné palety,
- anglické popisky rozhraní, označení autora `Autor: MiK` v horní liště a stručná nápověda nad výsledkem pro opětovné použití hodnot z pásky,
- vlastní barevný picker bez externích závislostí: libovolnou barvu lze trvale nastavit zvlášť pro čísla, operace, rovnítko, paměti, hodnoty a výsledky na pásce a horní tlačítka; barva textu se automaticky přizpůsobuje kontrastu.

## Vývojové prostředí

Projekt byl otevřen a synchronizován v Android Studiu Quail 2 (2026.1.2) se SDK Android 36.1.

Na pracovním počítači bylo ověřeno skutečné zařízení Samsung Galaxy S22 Ultra. Běžné ADB příkazy přes USB fungovaly, ale přenos APK opakovaně resetoval USB spojení. Telefon byl proto spárován přes **Wireless debugging**; následná instalace i automatické spuštění aplikace z Android Studia proběhly úspěšně. Při dalším testování je vhodné používat stejné bezdrátové připojení a USB problém znovu neřešit.

Dne 17. července 2026 bylo na stejném telefonu vizuálně ověřeno a vlastníkem schváleno nové rozložení, anglické popisky, zalamování pásky, rychlé palety i vlastní výběr libovolných barev pro jednotlivé části rozhraní.

Domácí pracovní kopie je připravená v běžné místní složce mimo OneDrive. Dne 16. července 2026 zde prošly příkazy `test` i `assembleDebug` s JDK 17 dodávaným v Android Studiu. V terminálu je potřeba pro běh Gradlu použít `C:\Program Files\Android\Android Studio\jbr`, protože systémová proměnná `JAVA_HOME` zatím není nastavená.

Domácí počítač má procesor Intel Core i7-860. Virtualizace i VT-d jsou v BIOSu zapnuté a ve Windows jsou zapnuté Hyper-V, Platforma virtuálního počítače i Windows Hypervisor Platform. Windows hlásí přítomný hypervizor, ale Android Emulator 36.6.11 přesto vrací při `-accel-check` kód 6 a při vynuceném WHPX `WHvGetCapability ... HypervisorPresent? 0`. AVD API 30 x86_64 proto na tomto počítači nelze spustit s hardwarovou akcelerací.

Pro ověření softwarové emulace byly na domácím počítači místně nainstalovány 32bitové obrazy AOSP API 28 a API 23 a samostatná archivní verze Android Emulatoru 33.1.1 (build 9529220). Archiv byl stažen z oficiálního Android Emulator Archive a jeho SHA-256 odpovídá `c1105d85046a3a5a43aa97bd88604fa8ca6e4d1b9b0bab71602a17ba085b607d`. Tyto SDK soubory a AVD nejsou součástí Gitu.

Výsledek místních pokusů:

- `TapeCalculator_API_28_x86` se v Emulatoru 33.1.1 s `-accel off` spustí, ale Android 9 v pomalém TCG opakovaně zabíjí `system_server` watchdogem zablokovaným v `GnssGeofenceProvider`. Vypnutí GPS v AVD problém neodstranilo; tento obraz není použitelný pro instalaci aplikace.
- `TapeCalculator_API_23_x86` používá Android 6 / API 23, jedno jádro, 2 GB RAM, vypnutou GPS a SwiftShader. Přes Emulator 33.1.1 se spustil jako 32bitové `qemu-system-i386`; ADB hlásilo `device`, `sys.boot_completed=1` a `Service package: found`.
- TapeCalculator do AVD API 23 zatím nebyl nainstalován ani vizuálně otestován, protože práce byla na žádost vlastníka v tomto bodě ukončena.

Místní cesta staršího emulátoru je `C:\Users\milan\AppData\Local\Android\Sdk\emulator-33.1.1-test\emulator.exe`. Na domácím počítači lze AVD API 23 spustit například takto:

```powershell
& 'C:\Users\milan\AppData\Local\Android\Sdk\emulator-33.1.1-test\emulator.exe' -avd TapeCalculator_API_23_x86 -accel off -gpu swiftshader_indirect -no-snapshot -no-passive-gps
```

První start bez hardwarové akcelerace je velmi pomalý. Na pracovním nebo novějším počítači je stále vhodnější běžný hardwarově akcelerovaný emulátor nebo fyzický telefon.

## Nejbližší postup

1. Commitnout a pushnout schválenou úpravu rozhraní na GitHub.
2. V dalším vývojovém kroku doplnit testy nalezených okrajových případů a rozhodnout o rozsahu verze `0.2.0`.
3. Před veřejným vydáním připravit ikonu, snímky obrazovky, podepsané APK/AAB a stručný anglický i český popis aplikace.

## Jak začít nový úkol Codexu

Po otevření naklonované složky vlož do nového úkolu:

> Přečti AGENTS.md, README.md a docs/PROJECT_STATUS.md. Pokračuj ve vývoji TapeCalculatoru od uvedeného nejbližšího kroku. Komunikuj se mnou česky.
