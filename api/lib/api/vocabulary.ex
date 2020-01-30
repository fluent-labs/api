defmodule Api.Vocabulary do
  use Ecto.Schema
  import Ecto.Changeset

  schema "vocabulary" do
    field :user, :id
    field :word, :id

    timestamps()
  end

  @doc false
  def changeset(vocabulary, attrs) do
    vocabulary
    |> cast(attrs, [:added])
    |> validate_required([:added])
  end
end
