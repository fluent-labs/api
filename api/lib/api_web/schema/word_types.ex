defmodule ApiWeb.Schema.WordTypes do
  @moduledoc """
  The schema for our GraphQL endpoint
  """
  use Absinthe.Schema.Notation

  enum :language, values: [:chinese, :english, :spanish]

  interface :word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:definition)}

    resolve_type fn
      %{language: :chinese}, _ -> :chinese_word
      %{language: :english}, _ -> :english_word
      %{language: :spanish}, _ -> :spanish_word
      _, _ -> :generic_word
    end
  end

  object :generic_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:definition)}

    interface :word
  end

  object :chinese_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string

    field :definitions, %Absinthe.Type.List{of_type: non_null(:definition)}

    field :hsk, :integer
    field :pinyin, %Absinthe.Type.List{of_type: non_null(:string)}

    interface :word
  end

  object :english_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:definition)}

    interface :word
  end

  object :spanish_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:definition)}

    interface :word
  end

end
