# wallet to wallet transfer


Apply migrations+seed locally:
- export $(cat .env.local | xargs) && flyway -configFiles=flyway-local.conf migrate