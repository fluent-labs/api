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

  defp serialize_language(language) do
    case language do
      :chinese -> "CHINESE"
      :english -> "ENGLISH"
      :spanish -> "SPANISH"
    end
  end

  def tag(language, text) do
    url = "/v1/tagging/" <> serialize_language(language) <> "/document"

    case post(url, %{text: text}) do
      {:ok, %{body: body}} -> {:ok, body}
      _ -> :error
    end
  end

  def definition(language, word) do
    url = "/v1/definition/" <> serialize_language(language) <> "/" <> word
    case get(url) do
      {:ok, %{body: body}} -> {:ok, resolve_definition_in_english(body)}
      _ -> :error
    end
  end

  # We will eventually resolve definitions in more than one base language
  defp resolve_definition_in_english(response_body) do
    response_body
    |> Enum.filter( fn word -> Map.has_key?(word, "definitions") end)
    |> Enum.flat_map( fn word -> Map.fetch!(word, "definitions") end)
    |> Enum.filter( fn definition -> Map.has_key?(definition, "text") end)
    |> Enum.flat_map( fn definition -> Map.fetch!(definition, "text") end)
  end
end
