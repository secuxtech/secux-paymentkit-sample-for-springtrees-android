# Credential handling

## Remaining payment credential cleanup

- Removed embedded merchant/operator passwords. Call `configureOperator(name, password)` before using the sample; missing credentials stop the demo operation. Removed committed build outputs and the old APK, which can retain previous credentials.

This cleanup removes the identified values/artifacts from the current branch. It does not revoke server accounts, rotate deployed device keys, rewrite Git history, remove old tags/releases, or automatically resolve scanner alerts. Treat previously published usable credentials as exposed and have their owners revoke or rotate them. Use new, narrowly scoped test credentials if development resumes.
