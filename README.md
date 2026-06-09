# Silkroad

**English:** Silkroad is a client-side camera and movement polish mod for Minecraft. It smooths camera motion in first person, third person, elytra flight, and third-person Shoulder Surfing setups. It also improves compatibility between Shoulder Surfing and Pehkui, and smooths the local player model on Y-axis step-ups when walking onto slabs or blocks.

Planned polish targets include ender pearl movement, boat camera jitter while turning, and other camera/movement rough edges found during play.

**Русский:** Silkroad — клиентский мод для полировки камеры и движения в Minecraft. Он сглаживает камеру от первого лица, от третьего лица, при полете на элитрах и в третьем лице с Shoulder Surfing. Также мод улучшает совместимость Shoulder Surfing и Pehkui, а еще сглаживает движение локальной модельки игрока по Y при step-up подъеме на полублоки и блоки.

В планах: сгладить движение после эндер-перла, джиттер камеры в лодке при повороте во время движения и другие шероховатости камеры/движения, которые найдутся в игре.

## Features

- First-person camera smoothing.
- Third-person camera smoothing.
- Elytra flight camera smoothing.
- Shoulder Surfing third-person camera smoothing.
- Shoulder Surfing + Pehkui compatibility for scaled entities.
- Local player model Y smoothing during step-ups onto slabs and blocks.
- Fzzy Config powered client config UI with English and Russian localization.

## Возможности

- Сглаживание камеры от первого лица.
- Сглаживание камеры от третьего лица.
- Сглаживание камеры при полете на элитрах.
- Сглаживание камеры от третьего лица с Shoulder Surfing.
- Совместимость Shoulder Surfing + Pehkui для сущностей с измененным размером.
- Сглаживание локальной модельки игрока по Y при подъеме на полублоки и блоки.
- Клиентский конфиг на Fzzy Config с английской и русской локализацией.

## Development

- Minecraft: 1.21.1
- Loader: NeoForge 21.1.233
- Java: 21
- Config UI: Fzzy Config 0.7.6+1.21+neoforge
- Branch: `neo-1.21.1`

Useful commands:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

## Version Strategy

This repository uses one branch per Minecraft/loader target. The first public target is `neo-1.21.1`.

For local parallel ports, use Git worktrees under `P:\SilkRoad-worktrees\silkroad-<mc>-<loader>` instead of copying folders by hand.

## Dependencies

Fzzy Config is consumed from `https://maven.fzzyhmstrs.me/` and must remain an external required dependency. Do not include or jar-in-jar it.
