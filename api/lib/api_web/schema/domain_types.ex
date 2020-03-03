defmodule ApiWeb.Schema.DomainTypes do
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

  interface :definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}

    resolve_type fn
      %{pinyin: _}, _ -> :chinese_definition
      _, _ -> :generic_definition
    end
  end

  object :generic_definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}

    interface :definition
  end

  object :chinese_definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}
    field :traditional, :string
    field :simplified, :string
    field :pinyin, :string

    interface :definition
  end

  object :user do
    field :id, non_null(:id)
    field :email, non_null(:string)
    field :definitions, %Absinthe.Type.List{of_type: non_null(:vocabulary)}
  end

  object :vocabulary do
    field :id, non_null(:id)
    field :user, non_null(:user)
    field :word, non_null(:word)
    field :added, :string
  end

end
