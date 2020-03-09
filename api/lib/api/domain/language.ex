defmodule Api.Language do
  use Ecto.Schema
  import Ecto.Changeset

  @moduledoc """
  Keeps track of languages. Pretty much an enumeration.
  """

  schema "languages" do
    field :name, :string

    timestamps()
  end

  @doc false
  def changeset(language, attrs) do
    language
    |> cast(attrs, [:name])
    |> validate_required([:name])
    |> unique_constraint(:name)
  end
end
