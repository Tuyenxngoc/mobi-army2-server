# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
mvn clean package          # Compile and build fat JAR (target/mobi-army2-server.jar)
mvn clean compile          # Compile only
run.bat                    # Launch server (JVM -server, -Xmx2g, UTF-8, port 19149)
```

No test suite exists yet (JUnit 5 is on the classpath but has no tests). There is no lint step.

## Configuration

- `src/main/resources/config/army2.properties` — server port (19149), debug flag, resource cache versions, event dates
- `src/main/resources/config/database.properties` — MySQL connection (default: localhost:3306, db `army2`, user `root`)
- HikariCP pool: 5–10 connections, 30 s timeout

## Architecture

This is a **Netty TCP game server** for a turn-based multiplayer mobile game (Mobi Army 2). The stack is Java 21 with virtual threads, HikariCP, SLF4J/Logback, and a hand-rolled binary protocol.

### Package Layout

All code lives under `com.teamobi.mobiarmy2`:

| Package | Contents |
|---|---|
| `app` | Bootstrap: `MobiArmy2`, `BeanRegistry`, `ApplicationContext` |
| `constant` | `Cmd` (~80 command IDs), `UserState`, `AccountStatus`, `GameString` |
| `dao` | 19 DAO classes (MySQL via HikariCP, no ORM) |
| `dto` | Data transfer objects between layers |
| `entity` | Domain models: `User`, `Character`, `Equipment`, `Room`, `ArmyMap`, … |
| `fight` | Combat engine: `FightManager`, `TrainingManager`, `Player`, `Boss` subclasses, `Bullet` subclasses |
| `network` | Netty layer: `Session`, `Message`, `MessageRouter`, codecs, 16 handler classes |
| `server` | Stateful singletons: `ServerManager`, `RoomManager`, `CharacterManager`, `EquipmentManager`, … |
| `service` | `GameDataService`, `LeaderboardService`, `ClanService`, `LoginRateLimiterService` |
| `ui` | JavaFX admin panel (not part of game runtime) |
| `util` | Utilities: `RandomUtil`, `Utils`, `MapTileExporter` |

### Startup & Dependency Wiring

`MobiArmy2` → `BeanRegistry.registerBeans()` manually instantiates every singleton in order (configs → DB → DAOs → services → managers → `ServerManager`) → `ApplicationContext` acts as a service locator via `getBean(Class)`. There is no DI framework; all wiring is explicit in `BeanRegistry.java`.

`ServerManager` bootstraps the Netty `ServerBootstrap`, holds two `ConcurrentHashMap`s (session ID → Session, user ID → Session) used everywhere for broadcast and lookups.

### Network Layer

Each TCP connection becomes a `Session`. After a Diffie-Hellman key exchange the pipeline swaps from `PlainMessageDecoder/Encoder` to the encrypted codec. `Session` uses a virtual-thread-per-task executor to process its inbound message queue sequentially per connection.

`MessageRouter` dispatches on `Message.command` (byte) to one of 16 `BaseMessageHandler` subclasses (Auth, Fight, Shop, Clan, Leaderboard, …). Only a fixed whitelist of commands (`GET_KEY`, `LOGIN`, `REGISTER_2`, `SET_PROVIDER`, `VERSION_CODE`, `GET_STRING`) are accepted before authentication; all others are dropped. Helper methods `us()`, `fw()`, `fm()` on the base handler resolve the current User, FightWait, and FightManager from the session.

`Message` wraps a command byte + `DataInputStream`/`DataOutputStream` — all game communication is compact binary, not JSON. Handler methods follow the convention: `handleXxx(Message ms)` reads from `ms.reader()` (inbound), `sendXxx(Message ms)` writes to `ms.writer()` (outbound). Handlers validate `us().getState()` against the expected `UserState` before processing.

Command IDs are in `Cmd.java` (~80 constants).

### Game Loop

**Matchmaking:** `FightWait` / `RoomManager` — 7 room types (Newbie, Intermediate, VIP, Arena, Freedom, Boss, Clan). Rooms enforce bet rules and team size before handing off to `FightManager`.

**Combat:** `FightManager` (implements `IFightManager extends IFightBase`) runs a turn-based loop (max 8 players, 30 s per turn before auto-skip). Each turn: receive aim/fire command → `BulletFactory` creates one of 50+ `Bullet` subclasses → physics + collision resolved server-side → results broadcast to all players. Wind, terrain collision, and special-item effects (shields, nukes, portals) are all computed here. Boss AI is encoded in 19 `Boss` subclasses that override `getNextAction()`.

**Training mode:** `TrainingManager` (implements `IFightBase`) is a per-user single-player instance stored in `User.trainingManager`. It uses the same bullet/physics engine as `FightManager` but with a simpler contract — no team management, leaderboard updates, or clan rewards. Entry point: `FightManagerMessageHandler.startTraining()`.

**Player in-fight state** lives in `Player.java` (position, HP, inventory snapshot). Persistent player state lives in `User.java` (characters owned, equipment, gold, XP, friends, missions).

### Data Layer

19 DAO classes talk to MySQL through HikariCP. All DAOs receive `HikariCPManager` via constructor injection (wired in `BeanRegistry`). Transactions with rollback are done manually via `HikariCPManager.transaction(connection -> { … })`. Passwords are hashed with BCrypt (jBCrypt 0.4). `GameDataService` preloads all reference data (characters, equipment, maps, captions, XP curves) at startup and serialises them into binary cache files sent to clients on first connect (`valuesdata2`, `equipdata2`, `playerdata2`, `icondata2`, `levelCData2`).

### Key Manager Singletons

| Class | Responsibility |
|---|---|
| `CharacterManager` | 30+ playable/boss character templates |
| `EquipmentManager` | 100+ weapon/armour stats |
| `MapManager` | 40+ maps (terrain, background, collision data) |
| `BulletManager` / `BulletFactory` | 50+ projectile types with physics |
| `RoomManager` | Room lifecycle, 10 boss-challenge variants |
| `LeaderboardService` | Top-player rankings with bonus rewards |
| `ClanService` | Guild management and clan shop |
| `LoginRateLimiterService` | Brute-force login protection |

### Concurrency Model

- One virtual-thread-per-task executor **per Session** — messages within a session are processed sequentially, avoiding per-session locking.
- `FightManager` uses a scheduled `fightLoop` submitted to a shared `ScheduledExecutorService`; all mutations inside the loop are single-threaded per fight.
- Cross-session broadcasts iterate `ServerManager`'s `ConcurrentHashMap` and write directly to each session's channel.
