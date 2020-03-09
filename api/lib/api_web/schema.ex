defmodule ApiWeb.Schema do
  @moduledoc """
  Provides the main entrypoints for our graphql schema
  """
  use Absinthe.Schema
  import_types(ApiWeb.Schema.DefinitionTypes)
  import_types(ApiWeb.Schema.DomainTypes)
  import_types(ApiWeb.Schema.WordTypes)

  alias ApiWeb.Resolvers

  query do
    @desc "get words in text"
    field :words_in_text, non_null(list_of(non_null(:word))) do
      arg(:language, :language)
      arg(:text, :string)
      resolve(&Resolvers.Domain.get_words_in_text/3)
    end
  end
end
