# Docs

How the app's calculations work, written for humans. Each page explains one piece of logic with pseudo-code and a worked example.

- Pages describe how things work **today**. Plans for new features live in GitHub issues.
- Examples use made-up numbers only. Never copy figures from the database into these pages.
- When you change logic described here, update its page in the same commit.

## VGBL

- [Income calculation](vgbl/income-calculation.md): how monthly income is computed for the VGBL Summary: pricing day, quotas held, net flow, forex.
- [Redemption dates](vgbl/redemption-dates.md): why a redemption is recorded on its valuation date and how the exit month is priced.

## Forex

- [Rate lookup](forex/rate-lookup.md): which date's rate is used, where it comes from and when it is cached.
