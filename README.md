# Snitch

Player reporting where a report only counts as much as the reporter's track record.
## Why
- **Discord reports are slow.** Cheaters should be caught while they're still online.
- **Triage is tedious.** Staff shouldn't have to sort a pile of reports by hand.
- **Players lie.** A report by a trusted reporter should be checked faster. Someone with a bad track record has to wait.
## How it works
1. **Report.** A player runs `/reportcheating <player> [reason]`. Done.
2. **Weigh.** A suspect has a case, and all reports against them are added to it. Each report adds the reporter's *trust score* to the  
   *risk score* of that case. A report made within X seconds of the previous one multiplies the risk by a configurable factor. Thus, someone being reported in quick succession leads to their case being investigated faster.
3. **Triage.** `/snitch menu` lists open cases, **always sorted by highest risk**.
4. **Resolve.** `/snitch resolvecase <player> <cheating|legit>` resolves a case by staff. Every reporter gains trust if  
   they were right, or loses it if they were wrong. The next report against that player opens a new case.

Reliable reporters end up carrying more weight; dishonest players are deprioritised without ever knowing it.

## Limitations & Original Purpose

The system isn't very customizable because it wasn't meant to be.   
This was a tailored solution for "VanillaX" before it shut down, and that I felt might make sense to open-source.

Originally, this system was meant to be used as a tool to identify players that should be screenshared, and that's how you'll benefit from it most.   
Even if you don't perform screenshares, however, this system should help you identify players to investigate, and eliminate a lot of tedious   
triage-work.

## Commands

| Command                                          | Permission           | Default  |     |
| ------------------------------------------------ | -------------------- | -------- | --- |
| `/reportcheating <player> [reason]`              | `snitch.report`      | everyone |     |
| `/snitch menu`                                   | `snitch.menu`        | op       |     |
| `/snitch resolvecase <player> <cheating\|legit>` | `snitch.resolvecase` | op       |     |
| `/snitch shadowban <add\|remove> <player>`       | `snitch.shadowban`   | op       |     |

`snitch.command` grants all `/snitch` subcommands.

## Configuration

**`report.yml`**

| Key                       | Default | Effect                                                      |  
|---------------------------|---------|----------------------------------------------------------   |  
| `baseTrustScore`          | `10.0`  | Trust every player starts with                              |  
| `successionWindowSeconds` | `30`    | Reports closer together than this trigger a risk-multiplier |  
| `successionMultiplier`    | `1.2`   | Applied to the case's whole risk score                      |  
| `positiveTrustReward`     | `1.0`   | Added to each reporter on a `cheating` verdict              |  
| `negativeTrustFactor`     | `0.8`   | Multiplied into each reporter on a `legit` verdict          |  

**`database.yml`**: `SQLITE` by default (no setup needed). `POSTGRES` (recommended), `MYSQL`, `MARIADB` and `H2` are  
also supported.

But honestly: please just use postgres.

## Design Decisions

1. No hot-reloading
Snitch does not support hot-reloading via command. This is a deliberate decision. I view hot-reloading as an anti-pattern that, in modern containerized environments, is useless.
2. Limited customization
Some things are customizable, but the core behavior is not. This plugin isn't trying to be anything more than a triaging system.

## To-Do

- Notifications?
- Custom Categories?
- History Viewer?

## Development & Contributing
This was a private project, and I haven't touched it for a long time. I felt better open-sourcing it than just letting it rot.
Depending on if people actually find the idea interesting, I may or may not maintain it.

If you have any contributions to make, please feel free to open a PR :) - Just don't break my entire code-style please, thanks!

## Credit

Thanks to @mcmdev for providing some code-style insight & suggesting improvements to the initial version!!

## License

[AGPL-3.0](LICENSE)
