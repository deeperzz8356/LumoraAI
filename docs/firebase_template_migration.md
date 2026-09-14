# Firebase template migration

1. Enable Cloud Storage for Firebase in the project. New default buckets require the Blaze plan.
2. Merge `firebase/firestore.rules` and `firebase/storage.rules` with the existing project rules, then deploy them. The catalog is public-read and administrator-write.
3. Run `python scripts/migrate_templates.py --dry-run` to validate the catalog and report its size.
4. Run `python scripts/migrate_templates.py --publish` to upload media and write `template_catalogs/v1` after read-back verification.
5. Run `python scripts/migrate_templates.py --publish --package` to preserve an archive and reduce the APK to four WebP image previews.

The script refuses unsafe filenames, duplicate IDs, failed uploads, mismatched hashes, and a starter set over 1 MB. It never removes bundled media until remote verification succeeds.

Import `docs/tool_remote_config_template.json` into Remote Config. The backend refreshes the same parameters every 45 seconds, while the Android client refreshes at startup and listens for real-time changes. Existing ad parameters are preserved.
