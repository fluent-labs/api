import Config

auth_token =
  System.get_env("AUTH_TOKEN") ||
    raise """
    environment variable AUTH_TOKEN is missing.
    This is required to communicate between services
    """

database_url =
  System.get_env("DATABASE_URL") ||
    raise """
    environment variable DATABASE_URL is missing.
    For example: ecto://USER:PASS@HOST/DATABASE
    """

language_service_url =
  System.get_env("LANGUAGE_SERVICE_URL") ||
    raise """
    environment variable LANGUAGE_SERVICE_URL is missing.
    This is required to handle text processing
    """

secret_key_base =
  System.get_env("SECRET_KEY_BASE") ||
    raise """
    environment variable SECRET_KEY_BASE is missing.
    You can generate one by calling: mix phx.gen.secret
    """

config :api,
  auth_token: auth_token,
  language_service_url: language_service_url

config :api, Api.Repo,
  # ssl: true,
  url: database_url,
  pool_size: String.to_integer(System.get_env("POOL_SIZE") || "10")

config :api, ApiWeb.Endpoint,
  http: [
    port: String.to_integer(System.get_env("PORT") || "4000"),
    transport_options: [socket_opts: [:inet6]]
  ],
  secret_key_base: secret_key_base,
  server: true,
  url: [host: "www.foreignlanguagereader.com", port: 80]

# Do not print debug messages in production
config :logger, level: :info
