# smpCurrency

A small Minecraft (Paper/Spigot) plugin that provides custom currency items and share/stock items for ThrowbackSMP.

This README covers installation, configuration, usage, and development notes.

---

## Features

- Custom coin item(s) defined via config
- Share/stock certificate items with configurable display/stack size
- Built-in lightweight resource-pack hosting (optional)
- Commands for giving/listing currency and managing companies / shares
- Permission nodes for fine-grained control

---

## Plugin Data

- Name: `smpCurrency`
- Target API version: `1.21`

Commands (see `plugin.yml`):
- `/currency` — usage: `/currency <give|list|texturepack|reload>`
  - aliases: `curr`, `money`
- `/company` — usage: `/company <create|list|info|issue|adddirector>`

Common permission nodes:
- `smpcurrency.*` (grants all plugin permissions)
- `smpcurrency.give` — allow giving coins
- `smpcurrency.list` — allow listing coin types
- `smpcurrency.reload` — allow reloading config
- `smpcurrency.company.create` — allow creating companies
- `smpcurrency.company.issue` — allow issuing shares and managing directors

---

## Installation (quick)

1. Build the plugin using the included Gradle wrapper:

   ./gradlew build

   The built JAR will appear in `build/libs/` (e.g. `smpCurrency-1.0-SNAPSHOT.jar`).

2. Copy the JAR into your server's `plugins/` directory.
3. Start the server once to generate the default config (`plugins/smpCurrency/config.yml`).
4. (Optional) Configure your coins, shares, and resource-pack settings in `config.yml`.
5. Restart or reload the server.

---

## Resource pack hosting

smpCurrency can host the resource pack locally or use an external URL. Relevant `config.yml` keys:

- `resource-pack.host-locally` (boolean): if true, the plugin will attempt to serve the pack from the server.
- `resource-pack.port` (number): preferred base port (default `8080`).
- `resource-pack.max-port-attempts` (number): number of times to try incrementing the port if the chosen port is in use.
- `resource-pack.url` (string): external URL to use instead of local hosting.
- `resource-pack.sha1` (string): SHA1 checksum of the pack (recommended for security).

Notes:
- If you enable local hosting, ensure the server/network allows inbound connections on the chosen port and that your public IP/DNS resolves correctly for players.
- If you provide an external `url`, make sure it points to a raw ZIP/pack file and (optionally) provide the matching `sha1`.

---

## Configuration reference (defaults/examples)

Example coin definition (from `config.yml`):

coins:
  coin:
    name: "&6Coin"
    material: PAPER
    lore:
      - "&7W Currency"
      - "&7for ThrowbackSMP"

Share / stock display and stack settings:

shares:
  stack-size: 64
  display:
    name: "&b%ticker% Share"
    lore:
      - "&7Company: &f%name%"
      - "&7Ticker: &b%ticker%"

Messages (common keys):

messages:
  no-permission: "&cYou don't have permission to use this command."
  player-not-found: "&cPlayer not found: {player}"
  invalid-amount: "&cInvalid amount: {amount}"
  coin-given: "&aGave {amount} {coin} coins to {player}"
  coin-received: "&aYou received {amount} {coin} coins!"

Tip: the plugin uses legacy color codes (e.g. `&a`) in the config; you can replace them with Adventure components in future improvements.

---

## Commands & usage

Available top-level commands (see `plugin.yml` for usage):

- /currency give <player> <coinType> <amount> — give coin items to a player (requires `smpcurrency.give`)
- /currency list — list configured coin types (requires `smpcurrency.list`)
- /currency texturepack — show resource pack / texture information
- /currency reload — reload plugin config (requires `smpcurrency.reload`)

- /company create <name> <ticker> — create a company (requires `smpcurrency.company.create`)
- /company list — list companies
- /company info <ticker|name> — show details about a company
- /company issue <ticker> <amount> — issue shares to a company (requires `smpcurrency.company.issue`)
- /company adddirector <ticker> <player> — grant director rights

Note: exact subcommand syntaxes (flags/optional args) should be confirmed by checking the command implementations in `src/main/java/me/shreyjain/smpCurrency/commands/`.

---

## Development

Project layout (important files):

- `src/main/java/...` — plugin source code
- `src/main/resources/plugin.yml` — plugin metadata and command/permission definitions
- `src/main/resources/config.yml` — default config and examples
- `examplepack/` — example resource pack and assets

Common developer tasks:

- Build: `./gradlew build`
- Run unit or integration tests (if added): `./gradlew test`
- Create a snapshot release: tag & push; CI handles releases based on tags and snapshot workflow.

CI/CD notes:
- The repository includes GitHub Actions workflows that build on pushes and publish snapshot/latest artifacts. See `.github/workflows/` for details.

---

## Contribution & ideas

Contributions are welcome. Small, focused PRs are easiest to review. Suggestions / TODO ideas from the project:

- Generate dynamic resource pack content per coin from `config.yml` entries
- Replace legacy color codes with Adventure text components
- Add config parsing and item-creation unit tests
- Add a `/currency packinfo` command and checksum validation
- Automatic semantic-release style versioning and changelog generation

If you want to contribute, open an issue or submit a PR with a description of the change and tests where appropriate.

---

## License

This project is licensed under the MIT License — see the `LICENSE` file for details.

---
