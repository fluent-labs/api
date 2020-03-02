defmodule ApiWeb.Schema.DomainTypes do
  @moduledoc """
  The schema for our GraphQL endpoint
  """
  use Absinthe.Schema.Notation

  alias Api.Clients

  enum :language, values: [:chinese, :english, :spanish]

  interface :word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)}

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
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)}

    interface :word
  end

  object :chinese_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string

    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)} do
      resolve fn word, _, _ ->
        case Clients.LanguageService.definition(:chinese, word.token) do
          {:ok, response} ->
            {:ok, response}
          _ -> {:error, "Error getting text"}
        end
      end
    end

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
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)} do
      resolve fn word, _, _ ->
        case Clients.LanguageService.definition(:english, word.token) do
          {:ok, response} ->
            {:ok, response}
          _ -> {:error, "Error getting text"}
        end
      end
    end

    interface :word
  end

  object :spanish_word do
    field :id, non_null(:id)
    field :language, non_null(:language)
    field :token, non_null(:string)
    field :tag, :string
    field :lemma, :string
    field :definitions, %Absinthe.Type.List{of_type: non_null(:string)} do
      resolve fn word, _, _ ->
        case Clients.LanguageService.definition(:spanish, word.token) do
          {:ok, response} ->
            {:ok, response}
          _ -> {:error, "Error getting text"}
        end
      end
    end

    interface :word
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
