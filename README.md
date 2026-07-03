# Bypass Paywalls Android

An Android app that reopens any article link through a bypass/archive service you choose,
instead of hitting the paywall.

## How it works

- **Share a link to the app.** From your browser or any app, share an article URL — Bypass
  Paywalls Android shows up in the share sheet. It rewrites the link using your selected service
  and opens the result in your browser.
- **Or open a link with it.** Long-press a link and choose "Open with… Bypass Paywalls Android" to do
  the same thing without leaving the share sheet.
- **Or paste a URL directly** into the app's "Try it now" field.

## Choosing a service

The app ships with a few well-known bypass/archive services:

| Service | How it rewrites the URL |
| --- | --- |
| removepaywall.com | `https://removepaywall.com/<url>` (default) |
| smry.ai | `https://smry.ai/<url>` |
| Wayback Machine | `https://web.archive.org/web/2999/<url>` (redirects to the latest snapshot) |
| Freedium | `https://freedium-mirror.cfd/<url>` (good for Medium articles) |
| archive.today | `https://archive.ph/newest/<url>` |

Pick which one is active from the main screen. You can also add your own by supplying a name
and a URL template containing the `{url}` token, e.g. `https://example.com/read?u={url}`, and
choosing whether the original URL should be percent-encoded (needed when it's a query
parameter) or inserted raw (needed when it's appended to the path).

## Project structure

Standard single-module Android app:

- `MainActivity` — Compose UI for picking/managing bypass services and testing URLs.
- `RedirectActivity` — invisible activity registered for `ACTION_SEND` (share) and
  `ACTION_VIEW` (http/https links); does the rewrite and hands off to a browser.
- `BypassService` / `SettingsRepository` — the rewrite rules and their persistence
  (`SharedPreferences`, no network calls or accounts).

## Building

Requires Android Studio (or the command line with an Android SDK installed) — `compileSdk 34`,
`minSdk 24`. Open the project and run, or:

```
./gradlew assembleDebug
```

This app makes no network requests of its own; it only builds a URL and asks the OS to open it
in an existing browser. The redirect is a client-side URL rewrite, so anything found on the
receiving service's page is between you and that service.
