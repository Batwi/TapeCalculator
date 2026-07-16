# Tape Calculator for Android

A focused, privacy-friendly calculator with a reusable calculation tape. Every number on the tape is a button: tap any operand or result to bring it back into the current calculation.

> **Status:** early prototype (`0.1.0`). The core workflow is implemented; visual and interaction tuning will follow real-device testing.

## Why this exists

Many calculator apps show history, but treat it as static text. Tape Calculator keeps the complete working trail interactive, inspired by desktop adding-machine tape calculators.

## Features

- Addition, subtraction, multiplication, division, and exponentiation
- Calculation tape with up to 100 persisted entries
- Tap any number on the tape to reuse it as the current operand
- Five persistent memory slots (M1–M5)
- Czech number formatting with decimal commas and grouped thousands
- 20 significant digits for regular arithmetic
- No ads, analytics, accounts, network access, or unnecessary permissions
- Android 6.0 (API 23) and newer

## Memory controls

- **Tap M1–M5:** recall the stored value.
- **Long-press M1–M5:** store the currently displayed value.

## Build

Open the project root in Android Studio and run the `app` configuration.

- Android Gradle Plugin 9.3.0
- Gradle 9.5.0
- JDK 17
- Compile SDK 36.1, target SDK 36

The application deliberately uses the Android platform UI toolkit without Compose or runtime dependencies. JUnit is used only for local tests.

## Project direction

Planned next steps include real-device layout tuning, an English UI locale, export/copy actions, optional individual tape-row deletion, accessibility review, screenshots, and signed release builds.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Bug reports and focused pull requests are welcome once the public repository is published.

## License

[MIT](LICENSE) © 2026 Milan (Batwi)

---

Český popis a návod je v souboru [docs/README.cs.md](docs/README.cs.md).
