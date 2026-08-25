# Getting an APK from this project

This project (an Android/Kotlin app exported from Google AI Studio) was missing
a few files needed to build: a debug signing keystore and a CI build workflow.
Both have been added:

- `debug.keystore` — a standard Android debug keystore (password `android`,
  alias `androiddebugkey`) so `assembleDebug` can produce an installable APK.
- `.github/workflows/build-apk.yml` — builds the APK automatically on GitHub.

## Steps

1. Create a new (empty) repo on GitHub.
2. Push this project's contents to it:
   ```bash
   cd Clock.F-main
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. Go to the **Actions** tab on GitHub. The "Build APK" workflow will run
   automatically on push (or trigger it manually with "Run workflow").
4. When it finishes (a few minutes):
   - Download the APK from the workflow run's **Artifacts** section, or
   - Grab it from the **Releases** page — the workflow also publishes a
     release (tagged `build-<run number>`) with the APK attached.
5. Install the APK on an Android device (enable "Install unknown apps" for
   your browser/file manager first).

## Notes

- This builds the **debug** variant, signed with the included debug
  keystore — fine for installing and testing on your own device, but not
  for the Play Store.
- The app references a Gemini API key (`GEMINI_API_KEY` in `.env.example`).
  It's commented out, so the app builds fine without it, but any Gemini-AI
  powered features won't work until you add a real key to a `.env` file
  (kept out of git via `.gitignore`) — see the AI Studio project's "Secrets"
  panel for the key.
- To build a proper **release** APK for distribution, you'd generate a real
  upload keystore and set `KEYSTORE_PATH`, `STORE_PASSWORD`, and
  `KEY_PASSWORD` as GitHub Actions secrets, then run `assembleRelease`
  instead of `assembleDebug` in the workflow.
