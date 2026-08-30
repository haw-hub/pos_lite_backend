# Free deployment

## Database (TiDB Cloud Starter)

Create a Starter instance with spending limit `0`, create a database named
`pos_lite_myanmar`, and copy the public connection values. TiDB requires TLS.
Use the JDBC URL shown by TiDB Cloud (or adapt its Java/MySQL connection
example) as `DB_URL`.

Required Render secrets:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SUPER_ADMIN_USERNAME`
- `SUPER_ADMIN_PASSWORD`

`JWT_SECRET` is generated automatically by the Render blueprint.

## Backend (Render)

Create a Blueprint from this GitHub repository. Render reads `render.yaml`,
builds the Docker image, and checks `/api/test/ping` for health.

After deployment, the API base URL is:

`https://<render-service-name>.onrender.com/api`

Use that value as `EXPO_PUBLIC_API_URL` when building the mobile app.

## Important storage limitation

Render Free uses an ephemeral filesystem. Payment screenshots stored under
`uploads/` can disappear after a restart or deploy. Do not depend on screenshot
persistence until this service is migrated to object storage such as S3 or R2.
