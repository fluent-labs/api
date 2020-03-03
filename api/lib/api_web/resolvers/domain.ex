defmodule ApiWeb.Resolvers.Domain do
  @moduledoc """
  Resolves queries and types for our schema
  """
  alias Api.Clients

  def get_words_in_text(_parent, %{language: language, text: text}, _resolution) do
    {:ok, words} = Clients.LanguageService.tag(language, text)
    tokens = Enum.map(words, fn word -> Map.fetch!(word, :token) end)
    {:ok, definitions} = Clients.LanguageService.definitions(language, tokens)

    processed_words = words
    |> Enum.map(fn word -> Map.put(word, :language, language) end)
    |> Enum.map(fn word -> Map.put(word, :definitions, Map.get(definitions, Map.fetch!(word, :token))) end)

    {:ok, processed_words}
  end

end
