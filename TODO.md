# TODO

- Settings screen
  - Themes
  - Notifications?
- Periodic health checks to determine connection
- Cloud backups?
- Use proper error messages for server URL screen

- Screens
  - bootstrap
  - Add new budget
  - Import

- auto change theme based on time of day

- support multiple LoginMethods on LoginScreen

- Nav to about screen, licenses, etc

- handle token-expired reason on responses

- merge api modules into one?

- Fix `v_schedules` in database, specifically JSON_EXTRACT
  - is it a limitation with sqldelight, or sql 3.38?
  - should be included with that dialect: https://www.sqlite.org/json1.html:
    - *The JSON functions and operators are built into SQLite by default, as of SQLite version 3.38.0 (2022-02-22).*

- log to file

- check server version on about screen
  - next to app version?
  - link to release notes
