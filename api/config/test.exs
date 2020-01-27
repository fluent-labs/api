use Mix.Config

# Configure your database
config :api, Api.Repo,
  # This is set up by github actions
  username: "ci-user",
  password: "ci-password",
  database: "api-test",
  hostname: "127.0.0.1",
  port: 33061,
  pool: Ecto.Adapters.SQL.Sandbox

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :api, ApiWeb.Endpoint,
  http: [port: 4002],
  server: false

# Print only warnings and errors during test
config :logger, level: :warn
