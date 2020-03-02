defmodule Api.Clients.LanguageService do
  use Tesla
  @moduledoc """
  A client to connect to the language service
  """

  plug Tesla.Middleware.BaseUrl, language_service_base_url()
  plug Tesla.Middleware.Headers, [{"Authorization", auth_token()}]
  plug Tesla.Middleware.JSON

  @app :api

  defp language_service_base_url do
    Application.load(@app)
    Application.fetch_env!(@app, :language_service_url)
  end

  defp auth_token do
    Application.load(@app)
    Application.fetch_env!(@app, :auth_token)
  end

  def tag(language, text) do
    url = "/v1/tagging/" <> language <> "/document"
    body = %{text: text}
    post(url, body)
  end

  def get_vocab(language, word) do
    url = "/v1/vocabulary/" <> language <> "/" <> word
    get(url)
  end
end
