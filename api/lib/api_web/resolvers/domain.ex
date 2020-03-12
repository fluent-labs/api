defmodule ApiWeb.Resolvers.Domain do
  @moduledoc """
  Resolves queries and types for our schema
  """
  alias Api.Clients

  defp get_definitions(language, tokens) do
    case Clients.LanguageService.definitions(language, tokens) do
      {:ok, definitions} -> definitions
      _ -> :error
    end
  end

  def get_words_in_text(_parent, %{language: language, text: text}, _resolution) do
    {:ok, words} = Clients.LanguageService.tag(language, text)

    # Request definitions in batches of 10
    # Too many words in a request will still cause problems
    definitions =
      Enum.map(words, fn word -> Map.fetch!(word, :token) end)
      |> Enum.chunk_every(10)
      |> Enum.flat_map(fn tokens -> get_definitions(language, tokens) end)
      |> Map.new

    # Join everything together.
    processed_words =
      words
      |> Enum.map(fn word -> Map.put(word, :language, language) end)
      |> Enum.map(fn word ->
        Map.put(word, :definitions, Map.get(definitions, Map.fetch!(word, :token)))
      end)

    {:ok, processed_words}
  end
end
