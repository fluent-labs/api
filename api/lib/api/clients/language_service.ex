defmodule Api.Clients.LanguageService do
  use Tesla

  @moduledoc """
  A client to connect to the language service
  """

  plug(Tesla.Middleware.BaseUrl, language_service_base_url())
  plug(Tesla.Middleware.Headers, [{"Authorization", auth_token()}])
  plug(Tesla.Middleware.JSON)

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

  defp atomize(my_map) when is_list(my_map) do
    Enum.map(my_map, &atomize/1)
  end

  defp atomize(my_map) do
    for {key, val} <- my_map, into: %{}, do: {String.to_atom(key), val}
  end

  defp log_request(url, request_body) do
    {:ok, request_log} = Jason.encode(request_body)
    IO.puts("Calling " <> url <> " with body: " <> request_log)
  end

  defp log_request(url) do
    IO.puts("Calling " <> url)
  end

  def tag(language, text) do
    url = "/v1/tagging/" <> serialize_language(language) <> "/document"
    request_body =  %{text: text}

    log_request(url, request_body)

    case post(url, request_body) do
      {:ok, %{body: body}} -> {:ok, Enum.map(body, &atomize/1)}
      _ -> :error
    end
  end

  def definition(language, word) do
    url = "/v1/definition/" <> serialize_language(language) <> "/" <> word
    log_request(url)

    case get(url) do
      {:ok, %{body: body}} -> {:ok, atomize(body)}
      _ -> :error
    end
  end

  def definitions(language, words) do
    url = "/v1/definitions/" <> serialize_language(language) <> "/"
    request_body = %{words: words}

    log_request(url, request_body)

    response =
      case post(url, request_body) do
        {:ok, %{body: body}} -> body
        _ -> :error
      end

    mapped = Enum.map(response, fn {word, definition} -> {word, atomize(definition)} end)

    # Bit of an ugly hack because we can't just map over dictionary
    {:ok, Enum.into(mapped, %{})}
  end
end
