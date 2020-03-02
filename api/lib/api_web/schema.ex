defmodule ApiWeb.Schema do
  use Absinthe.Schema
  import_types ApiWeb.Schema.DomainTypes

  alias ApiWeb.Resolvers

  query do

    @desc "get words in text"
    field :words_in_text, non_null(list_of(non_null(:word))) do
      arg :language, :language
      arg :text, :string
      resolve &Resolvers.Domain.get_words_in_text/3
    end

  end

end
