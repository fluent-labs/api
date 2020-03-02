defmodule ApiWeb.Resolvers.Domain do
  alias Api.Clients

  def get_words_in_text(_parent, %{language: language, text: text}, _resolution) do
    IO.puts "Resolver was called"
    IO.puts Clients.LanguageService.tag(language, text)
    case Clients.LanguageService.tag(language, text) do
      {:ok, response} -> {:ok, response}
      _ -> {:error, "Error getting text"}
    end
  end

end
