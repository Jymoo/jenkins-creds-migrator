# jenkins-creds-migrator
Groovy scripts to export and import Jenkins credentials for seamless migration between Jenkins instances

This project helps migrate Jenkins credentials from one Jenkins instance to another via Groovy scripts. It supports multiple credential types:

- Username/Password
- Secret Text
- SSH Private Keys
- AWS Credentials

## 🔧 Files

- `export-creds.groovy` — Exports credentials to a `.txt` file
- `import-creds.groovy` — Imports credentials from the `.txt` file
- `creds-export.txt` — Example or generated file with exported credentials

## ⚠️ Warning

Do **NOT** commit real secrets to Git. Use example/test credentials in versioned files.

## ✅ Supported Credential Types

- `user/password`
- `secret text`
- `ssh priv key`
- `aws`

## 🔁 Migration Steps

1. **Export credentials** from the source Jenkins:
   - Open **Manage Jenkins → Script Console**
   - Run `export-creds.groovy`
   - Save the `creds-export.txt` output to a secure location

2. **Transfer** `creds-export.txt` securely to the destination Jenkins server

3. **Import credentials** on the new Jenkins:
   - Open **Manage Jenkins → Script Console**
   - Place `creds-export.txt` under a known path (e.g. `/var/jenkins_home`)
   - Run `import-creds.groovy`

4. ✅ Done! Credentials are now available in the new Jenkins instance.

## 👥 Contributors

- Livingstone @Jymoo


