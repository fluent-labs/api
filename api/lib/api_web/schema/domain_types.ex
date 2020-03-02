defmodule ApiWeb.Schema.DomainTypes do
  use Absinthe.Schema.Notation

  enum :language, values: [:chinese, :english, :spanish]

  interface :word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :text, non_null(:string)
    field :part_of_speech, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)}

    resolve_type fn
      %{pinyin: _}, _ -> :chinese_word
      _, _ -> :generic_word
    end
  end

  object :generic_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :text, non_null(:string)
    field :part_of_speech, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)}
  end

  object :chinese_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :text, non_null(:string)
    field :part_of_speech, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :hsk, :integer
    field :pinyin, %Absinthe.Type.List{of_type: non_null(:string)}
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
