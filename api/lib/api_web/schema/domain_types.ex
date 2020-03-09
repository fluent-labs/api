defmodule ApiWeb.Schema.DomainTypes do
  @moduledoc """
  The schema for our GraphQL endpoint
  """
  use Absinthe.Schema.Notation

  object :user do
    field :id, non_null(:id)
    field :email, non_null(:string)
    field :vocabulary, %Absinthe.Type.List{of_type: non_null(:vocabulary)}
  end

  object :vocabulary do
    field :id, non_null(:id)
    field :user, non_null(:user)
    field :word, non_null(:word)
    field :added, :string
  end
end
