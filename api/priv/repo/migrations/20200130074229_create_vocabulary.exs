defmodule Api.Repo.Migrations.CreateVocabulary do
  use Ecto.Migration

  def change do
    create table(:vocabulary) do
      add :user, references(:users, on_delete: :nothing)
      add :word, references(:words, on_delete: :nothing)

      timestamps()
    end

    create index(:vocabulary, [:user])
    create index(:vocabulary, [:word])
  end
end
